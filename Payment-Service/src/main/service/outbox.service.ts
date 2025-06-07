import { Repository } from 'typeorm';
import { KafkaService } from './kafka.service';
import {OutboxMessage} from "../model/entity/outbox-message.entity";
import {AppDataSource} from "../config/database.config";
import logger from "../util/logger";

export class OutboxService {
    private outboxRepository: Repository<OutboxMessage>;

    constructor() {
        this.outboxRepository = AppDataSource.getRepository(OutboxMessage);
    }

    async publishEvent(eventType: string, payload: any): Promise<void> {
        const message = new OutboxMessage();
        message.aggregateType = 'Payment';
        message.aggregateId = payload.paymentId?.toString() || payload.refundId || 'unknown';
        message.eventType = eventType;
        message.payload = payload;

        await this.outboxRepository.save(message);
        logger.info(`Event stored in outbox`, { eventType, aggregateId: message.aggregateId });
    }

    async processOutboxMessages(): Promise<void> {
        const unprocessedMessages = await this.outboxRepository.find({
            where: { processed: false },
            order: { createdAt: 'ASC' },
            take: 100
        });

        for (const message of unprocessedMessages) {
            try {
                const topic = this.getTopicFromEventType(message.eventType);
                await KafkaService.getInstance().publishEvent(
                    topic,
                    message.aggregateId,
                    message.payload
                );

                message.processed = true;
                message.processedAt = new Date();
                await this.outboxRepository.save(message);

                logger.info(`Outbox message processed`, {
                    messageId: message.id,
                    eventType: message.eventType
                });
            } catch (error) {
                message.retryCount += 1;
                message.errorMessage = error instanceof Error ? error.message : 'Unknown error';

                if (message.retryCount >= 5) {
                    logger.error(`Max retries reached for outbox message`, {
                        messageId: message.id,
                        error: message.errorMessage
                    });
                }

                await this.outboxRepository.save(message);
            }
        }
    }

    private getTopicFromEventType(eventType: string): string {
        switch (eventType) {
            case 'payment-events.completed':
                return 'payment-events.completed';
            case 'payment-events.failed':
                return 'payment-events.failed';
            case 'payment-events.refunded':
                return 'payment-events.refunded';
            default:
                return 'payment-events';
        }
    }
}