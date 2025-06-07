import cron from 'node-cron';

import {OutboxService} from "../service/outbox.service";
import {AppDataSource} from "../config/database.config";
import {Payment, PaymentStatus} from "../model/entity/payment.entity";
import {PaymentTransaction, TransactionStatus} from "../model/entity/payment-transaction.entity";
import logger from "../util/logger";

class PaymentJobs {
    private outboxService: OutboxService;

    constructor() {
        this.outboxService = new OutboxService();
    }

    startOutboxProcessor(): void {
        cron.schedule('*/10 * * * * *', async () => {
            try {
                await this.outboxService.processOutboxMessages();
            } catch (error) {
                logger.error('Error processing outbox messages:', error);
            }
        });

        logger.info('Outbox processor job started');
    }

    startOutboxCleanup(): void {
        cron.schedule('0 2 * * *', async () => {
            try {
                await this.cleanupProcessedOutboxMessages();
            } catch (error) {
                logger.error('Error cleaning up outbox messages:', error);
            }
        });

        logger.info('Outbox cleanup job started');
    }

    startPaymentTimeoutHandler(): void {
        cron.schedule('*/5 * * * *', async () => {
            try {
                await this.handlePaymentTimeouts();
            } catch (error) {
                logger.error('Error handling payment timeouts:', error);
            }
        });

        logger.info('Payment timeout handler job started');
    }

    startFailedTransactionRetry(): void {
        cron.schedule('0 * * * *', async () => {
            try {
                await this.retryFailedTransactions();
            } catch (error) {
                logger.error('Error retrying failed transactions:', error);
            }
        });

        logger.info('Failed transaction retry job started');
    }

    startDailyReporting(): void {
        cron.schedule('0 1 * * *', async () => {
            try {
                await this.generateDailyReport();
            } catch (error) {
                logger.error('Error generating daily report:', error);
            }
        });
        logger.info('Daily reporting job started');
    }

    private async cleanupProcessedOutboxMessages(): Promise<void> {
        const repository = AppDataSource.getRepository('OutboxMessage');
        const cutoffDate = new Date();
        cutoffDate.setDate(cutoffDate.getDate() - 7);

        const result = await repository
            .createQueryBuilder()
            .delete()
            .where('processed = true AND processed_at < :cutoffDate', { cutoffDate })
            .execute();
        logger.info(`Cleaned up ${result.affected} processed outbox messages`);
    }

    private async handlePaymentTimeouts(): Promise<void> {
        const paymentRepository = AppDataSource.getRepository(Payment);
        const timeoutDate = new Date();
        timeoutDate.setMinutes(timeoutDate.getMinutes() - 30);
        const timedOutPayments = await paymentRepository
            .createQueryBuilder('payment')
            .where('payment.status = :status', { status: PaymentStatus.PENDING })
            .andWhere('payment.createdAt < :timeoutDate', { timeoutDate })
            .getMany();
        for (const payment of timedOutPayments) {
            payment.status = PaymentStatus.FAILED;
            payment.failureReason = 'Payment timeout - no action taken within 30 minutes';
            await paymentRepository.save(payment);
            await this.outboxService.publishEvent('payment-events.timeout', {
                paymentId: payment.paymentId,
                orderId: payment.orderId,
                customerId: payment.customerId,
                reason: 'timeout',
                timeoutAt: new Date()
            });
            logger.info(`Payment timed out`, { paymentId: payment.paymentId });
        }
    }

    private async retryFailedTransactions(): Promise<void> {
        const transactionRepository = AppDataSource.getRepository(PaymentTransaction);
        const retryDate = new Date();
        retryDate.setHours(retryDate.getHours() - 1);
        const failedTransactions = await transactionRepository
            .createQueryBuilder('transaction')
            .where('transaction.status = :status', { status: TransactionStatus.FAILED })
            .andWhere('transaction.createdAt < :retryDate', { retryDate })
            .andWhere('(transaction.metadata->>\'retryCount\')::int < 3')
            .getMany();
        for (const transaction of failedTransactions) {
            const retryCount = (transaction.metadata?.retryCount || 0) + 1;
            if (retryCount <= 3) {
                transaction.metadata = {
                    ...transaction.metadata,
                    retryCount,
                    lastRetryAt: new Date()
                };
                transaction.status = TransactionStatus.PENDING;
                await transactionRepository.save(transaction);
                logger.info(`Retrying failed transaction`, {
                    transactionId: transaction.id,
                    paymentId: transaction.paymentId,
                    retryCount
                });
            }
        }
    }

    private async generateDailyReport(): Promise<void> {
        const paymentRepository = AppDataSource.getRepository(Payment);
        const yesterday = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        yesterday.setHours(0, 0, 0, 0);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const dailyStats = await paymentRepository
            .createQueryBuilder('payment')
            .select([
                'payment.status as status',
                'payment.currency as currency',
                'COUNT(*) as count',
                'SUM(payment.amount) as total_amount'
            ])
            .where('payment.createdAt >= :yesterday', { yesterday })
            .andWhere('payment.createdAt < :today', { today })
            .groupBy('payment.status, payment.currency')
            .getRawMany();
        logger.info('Daily payment report generated', {
            date: yesterday.toISOString().split('T')[0],
            stats: dailyStats
        });
    }

    startAllJobs(): void {
        this.startOutboxProcessor();
        this.startOutboxCleanup();
        this.startPaymentTimeoutHandler();
        this.startFailedTransactionRetry();
        this.startDailyReporting();
        logger.info('All payment jobs started successfully');
    }
}
