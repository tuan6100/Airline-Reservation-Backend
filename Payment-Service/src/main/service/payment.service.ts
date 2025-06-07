import { Repository } from 'typeorm';

import { OutboxService } from './outbox.service';
import { v4 as uuidv4 } from 'uuid';
import {Payment, PaymentMethodType, PaymentStatus} from "../model/entity/payment.entity";
import {PaymentTransaction, TransactionStatus, TransactionType} from "../model/entity/payment-transaction.entity";
import {Refund, RefundReason, RefundStatus} from "../model/entity/refund.entity";
import {AppDataSource} from "../config/database.config";
import logger from "../util/logger";

export interface CreatePaymentRequest {
    orderId: number;
    customerId: number;
    amount: number;
    currency: string;
    paymentMethod: PaymentMethodType;
}

export interface ProcessPaymentRequest {
    paymentId: number;
    cardNumber?: string;
    expiryMonth?: number;
    expiryYear?: number;
    cvv?: string;
    holderName?: string;
}

export interface RefundRequest {
    paymentId: number;
    amount?: number;
    reason: RefundReason;
    description?: string;
}

export class PaymentService {
    private paymentRepository: Repository<Payment>;
    private transactionRepository: Repository<PaymentTransaction>;
    private refundRepository: Repository<Refund>;
    private outboxService: OutboxService;

    constructor() {
        this.paymentRepository = AppDataSource.getRepository(Payment);
        this.transactionRepository = AppDataSource.getRepository(PaymentTransaction);
        this.refundRepository = AppDataSource.getRepository(Refund);
        this.outboxService = new OutboxService();
    }

    async createPayment(request: CreatePaymentRequest): Promise<Payment> {
        try {
            const payment = new Payment();
            payment.orderId = request.orderId;
            payment.customerId = request.customerId;
            payment.amount = request.amount;
            payment.currency = request.currency;
            payment.paymentMethod = request.paymentMethod;
            payment.status = PaymentStatus.PENDING;

            const savedPayment = await this.paymentRepository.save(payment);

            await this.createTransaction(
                savedPayment.paymentId,
                TransactionType.AUTHORIZATION,
                request.amount,
                request.currency,
                'Payment created and waiting for processing'
            );
            logger.info(`Payment created successfully`, { paymentId: savedPayment.paymentId });
            return savedPayment;
        } catch (error) {
            logger.error('Failed to create payment:', error);
            throw new Error('Failed to create payment');
        }
    }

    async processPayment(request: ProcessPaymentRequest): Promise<{ success: boolean; message: string; transactionId?: string }> {
        const payment = await this.paymentRepository.findOne({
            where: { paymentId: request.paymentId }
        });

        if (!payment) {
            throw new Error('Payment not found');
        }

        if (payment.status !== PaymentStatus.PENDING) {
            throw new Error('Payment is not in pending status');
        }
        payment.status = PaymentStatus.PROCESSING;
        await this.paymentRepository.save(payment);
        const isSuccess = Math.random() < 0.7;
        const transactionId = uuidv4();

        try {
            if (isSuccess) {
                await this.simulateSuccessfulPayment(payment, transactionId);
                await this.outboxService.publishEvent('payment-events.completed', {
                    paymentId: payment.paymentId,
                    orderId: payment.orderId,
                    customerId: payment.customerId,
                    amount: payment.amount,
                    currency: payment.currency,
                    status: PaymentStatus.SUCCEEDED,
                    processedAt: new Date()
                });

                return {
                    success: true,
                    message: 'Payment processed successfully',
                    transactionId
                };
            } else {
                await this.simulateFailedPayment(payment, transactionId);
                await this.outboxService.publishEvent('payment-events.failed', {
                    paymentId: payment.paymentId,
                    orderId: payment.orderId,
                    customerId: payment.customerId,
                    amount: payment.amount,
                    currency: payment.currency,
                    status: PaymentStatus.FAILED,
                    failureReason: payment.failureReason,
                    processedAt: new Date()
                });

                return {
                    success: false,
                    message: payment.failureReason || 'Payment processing failed',
                    transactionId
                };
            }
        } catch (error) {
            logger.error('Error processing payment:', error);
            payment.status = PaymentStatus.FAILED;
            payment.failureReason = 'System error during payment processing';
            await this.paymentRepository.save(payment);

            throw new Error('Payment processing failed due to system error');
        }
    }

    private async simulateSuccessfulPayment(payment: Payment, transactionId: string): Promise<void> {
        await this.delay(1000 + Math.random() * 2000);
        payment.status = PaymentStatus.SUCCEEDED;
        payment.paidAt = new Date();
        await this.paymentRepository.save(payment);
        await this.createTransaction(
            payment.paymentId,
            TransactionType.CAPTURE,
            payment.amount,
            payment.currency,
            'Payment processed successfully',
            TransactionStatus.SUCCESS,
            { transactionId, gateway: 'payment-simulator' }
        );

        logger.info(`Payment processed successfully`, {
            paymentId: payment.paymentId,
            transactionId
        });
    }

    private async simulateFailedPayment(payment: Payment, transactionId: string): Promise<void> {
        await this.delay(500 + Math.random() * 1000);
        const failureReasons = [
            'Insufficient funds',
            'Card declined by issuer',
            'Invalid card details',
            'Card expired',
            'Transaction limit exceeded',
            'Suspected fraud'
        ];
        const randomReason = failureReasons[Math.floor(Math.random() * failureReasons.length)];
        payment.status = PaymentStatus.FAILED;
        payment.failureReason = randomReason;
        await this.paymentRepository.save(payment);
        await this.createTransaction(
            payment.paymentId,
            TransactionType.AUTHORIZATION,
            payment.amount,
            payment.currency,
            randomReason,
            TransactionStatus.FAILED,
            { transactionId, gateway: 'payment-simulator' }
        );

        logger.warn(`Payment failed`, {
            paymentId: payment.paymentId,
            reason: randomReason,
            transactionId
        });
    }

    async processRefund(request: RefundRequest): Promise<Refund> {
        const payment = await this.paymentRepository.findOne({
            where: { paymentId: request.paymentId }
        });
        if (!payment) {
            throw new Error('Payment not found');
        }
        if (payment.status !== PaymentStatus.SUCCEEDED) {
            throw new Error('Can only refund successful payments');
        }
        const refundAmount = request.amount || payment.amount;
        if (refundAmount > payment.amount) {
            throw new Error('Refund amount cannot exceed payment amount');
        }

        const refund = new Refund();
        refund.paymentId = payment.paymentId;
        refund.amount = refundAmount;
        refund.currency = payment.currency;
        refund.reason = request.reason;
        refund.description = request.description;
        refund.status = RefundStatus.PROCESSING;
        const savedRefund = await this.refundRepository.save(refund);
        const isSuccess = Math.random() < 0.8;

        try {
            if (isSuccess) {
                await this.simulateSuccessfulRefund(savedRefund, payment);
                await this.outboxService.publishEvent('payment-events.refunded', {
                    refundId: savedRefund.id,
                    paymentId: payment.paymentId,
                    orderId: payment.orderId,
                    customerId: payment.customerId,
                    amount: refundAmount,
                    currency: payment.currency,
                    status: RefundStatus.SUCCEEDED,
                    processedAt: new Date()
                });
            } else {
                await this.simulateFailedRefund(savedRefund);
            }
            return savedRefund;
        } catch (error) {
            logger.error('Error processing refund:', error);
            savedRefund.status = RefundStatus.FAILED;
            savedRefund.failureReason = 'System error during refund processing';
            await this.refundRepository.save(savedRefund);
            throw new Error('Refund processing failed');
        }
    }

    private async simulateSuccessfulRefund(refund: Refund, payment: Payment): Promise<void> {
        await this.delay(2000 + Math.random() * 3000);
        refund.status = RefundStatus.SUCCEEDED;
        await this.refundRepository.save(refund);
        await this.createTransaction(
            payment.paymentId,
            TransactionType.REFUND,
            refund.amount,
            refund.currency,
            'Refund processed successfully',
            TransactionStatus.SUCCESS,
            { refundId: refund.id, gateway: 'payment-simulator' }
        );

        logger.info(`Refund processed successfully`, {
            refundId: refund.id,
            paymentId: payment.paymentId
        });
    }

    private async simulateFailedRefund(refund: Refund): Promise<void> {
        await this.delay(1000);
        refund.status = RefundStatus.FAILED;
        refund.failureReason = 'Refund rejected by payment processor';
        await this.refundRepository.save(refund);
        logger.warn(`Refund failed`, { refundId: refund.id });
    }

    async getPayment(paymentId: number): Promise<Payment | null> {
        return await this.paymentRepository.findOne({
            where: { paymentId }
        });
    }

    async getPaymentsByOrder(orderId: number): Promise<Payment[]> {
        return await this.paymentRepository.find({
            where: { orderId }
        });
    }

    async getPaymentsByCustomer(customerId: number): Promise<Payment[]> {
        return await this.paymentRepository.find({
            where: { customerId },
            order: { createdAt: 'DESC' }
        });
    }

    async getPaymentTransactions(paymentId: number): Promise<PaymentTransaction[]> {
        return await this.transactionRepository.find({
            where: { paymentId },
            order: { createdAt: 'ASC' }
        });
    }

    async getRefunds(paymentId: number): Promise<Refund[]> {
        return await this.refundRepository.find({
            where: { paymentId },
            order: { createdAt: 'DESC' }
        });
    }

    private async createTransaction(
        paymentId: number,
        type: TransactionType,
        amount: number,
        currency: string,
        responseMessage: string,
        status: TransactionStatus = TransactionStatus.PENDING,
        metadata?: Record<string, any>
    ): Promise<PaymentTransaction> {
        const transaction = new PaymentTransaction();
        transaction.paymentId = paymentId;
        transaction.type = type;
        transaction.status = status;
        transaction.amount = amount;
        transaction.currency = currency;
        transaction.responseMessage = responseMessage;
        transaction.metadata = metadata;

        return await this.transactionRepository.save(transaction);
    }

    private delay(ms: number): Promise<void> {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    validateCardNumber(cardNumber: string): boolean {
        const digits = cardNumber.replace(/\D/g, '');
        let sum = 0;
        let isEven = false;
        for (let i = digits.length - 1; i >= 0; i--) {
            let digit = parseInt(digits[i]);
            if (isEven) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            isEven = !isEven;
        }
        return sum % 10 === 0 && digits.length >= 13 && digits.length <= 19;
    }

    validateExpiry(month: number, year: number): boolean {
        const now = new Date();
        const expiryDate = new Date(year, month - 1);
        return expiryDate > now;
    }
}



