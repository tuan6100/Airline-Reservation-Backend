import { Kafka, Consumer, Producer, KafkaMessage } from 'kafkajs';
import logger from "winston";

export class KafkaService {
    private static instance: KafkaService;
    private kafka: Kafka;
    private producer: Producer;
    private consumer: Consumer;

    private constructor() {
        this.kafka = new Kafka({
            clientId: process.env.KAFKA_CLIENT_ID || 'payment-service',
            brokers: (process.env.KAFKA_BROKERS || 'localhost:9092').split(','),
            retry: {
                initialRetryTime: 100,
                retries: 8,
            },
        });

        this.producer = this.kafka.producer({
            transactionTimeout: 30000,
        });

        this.consumer = this.kafka.consumer({
            groupId: process.env.KAFKA_GROUP_ID || 'payment-group',
            sessionTimeout: 30000,
            heartbeatInterval: 3000,
        });
    }

    public static getInstance(): KafkaService {
        if (!KafkaService.instance) {
            KafkaService.instance = new KafkaService();
        }
        return KafkaService.instance;
    }

    async connect(): Promise<void> {
        try {
            await this.producer.connect();
            await this.consumer.connect();

            // Subscribe to topics
            await this.consumer.subscribe({
                topics: [
                    'order-events.created',
                    'order-events.confirmed',
                    'order-events.cancelled',
                    'booking-events.confirmed',
                    'booking-events.cancelled'
                ],
                fromBeginning: false,
            });

            // Start consuming
            await this.consumer.run({
                eachMessage: async ({ topic, partition, message }) => {
                    await this.handleMessage(topic, message);
                },
            });

            logger.info('Kafka service connected and consuming messages');
        } catch (error) {
            logger.error('Failed to connect to Kafka:', error);
            throw error;
        }
    }

    async disconnect(): Promise<void> {
        try {
            await this.consumer.disconnect();
            await this.producer.disconnect();
            logger.info('Kafka service disconnected');
        } catch (error) {
            logger.error('Error disconnecting from Kafka:', error);
        }
    }

    async publishEvent(topic: string, key: string, value: any): Promise<void> {
        try {
            await this.producer.send({
                topic,
                messages: [{
                    key,
                    value: JSON.stringify(value),
                    timestamp: Date.now().toString(),
                }],
            });

            logger.info(`Published event to topic ${topic}`, { key, value });
        } catch (error) {
            logger.error(`Failed to publish event to topic ${topic}:`, error);
            throw error;
        }
    }

    private async handleMessage(topic: string, message: KafkaMessage): Promise<void> {
        try {
            const value = message.value?.toString();
            if (!value) return;
            const data = JSON.parse(value);
            logger.info(`Received message from topic ${topic}`, { data });
            const { MessageHandler } = await import('./message.handler');
            const handler = new MessageHandler();
            switch (topic) {
                case 'order-events.created':
                    await handler.handleOrderCreated(data);
                    break;
                case 'order-events.confirmed':
                    await handler.handleOrderConfirmed(data);
                    break;
                case 'order-events.cancelled':
                    await handler.handleOrderCancelled(data);
                    break;
                case 'booking-events.confirmed':
                    await handler.handleBookingConfirmed(data);
                    break;
                case 'booking-events.cancelled':
                    await handler.handleBookingCancelled(data);
                    break;
                default:
                    logger.warn(`Unhandled topic: ${topic}`);
            }
        } catch (error) {
            logger.error(`Error handling message from topic ${topic}:`, error);
        }
    }
}