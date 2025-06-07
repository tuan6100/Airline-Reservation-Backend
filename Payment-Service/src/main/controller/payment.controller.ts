import { Request, Response } from 'express';

import Joi from 'joi';
import {CreatePaymentRequest, PaymentService, ProcessPaymentRequest, RefundRequest} from "../service/payment.service";
import {PaymentMethodType} from "../model/entity/payment.entity";
import {RefundReason} from "../model/entity/refund.entity";
import logger from "../util/logger";

export class PaymentController {
    private paymentService: PaymentService;

    constructor() {
        this.paymentService = new PaymentService();
    }

    async createPayment(req: Request, res: Response): Promise<void> {
        try {
            const validation = this.validateCreatePaymentRequest(req.body);
            if (validation.error) {
                res.status(400).json({
                    success: false,
                    message: 'Invalid request data',
                    errors: validation.error.details.map(detail => detail.message)
                });
                return;
            }

            const request: CreatePaymentRequest = validation.value;
            const payment = await this.paymentService.createPayment(request);

            res.status(201).json({
                success: true,
                message: 'Payment created successfully',
                data: {
                    paymentId: payment.paymentId,
                    status: payment.status,
                    amount: payment.amount,
                    currency: payment.currency,
                    createdAt: payment.createdAt
                }
            });
        } catch (error) {
            logger.error('Error creating payment:', error);
            res.status(500).json({
                success: false,
                message: 'Failed to create payment',
                error: error instanceof Error ? error.message : 'Unknown error'
            });
        }
    }

    async processPayment(req: Request, res: Response): Promise<void> {
        try {
            const paymentId = parseInt(req.params.paymentId);
            if (!paymentId) {
                res.status(400).json({
                    success: false,
                    message: 'Invalid payment ID'
                });
                return;
            }

            const validation = this.validateProcessPaymentRequest(req.body);
            if (validation.error) {
                res.status(400).json({
                    success: false,
                    message: 'Invalid request data',
                    errors: validation.error.details.map(detail => detail.message)
                });
                return;
            }

            const request: ProcessPaymentRequest = {
                paymentId,
                ...validation.value
            };

            const result = await this.paymentService.processPayment(request);

            res.status(200).json({
                success: result.success,
                message: result.message,
                data: {
                    transactionId: result.transactionId,
                    processed: result.success
                }
            });
        } catch (error) {
            logger.error('Error processing payment:', error);
            res.status(500).json({
                success: false,
                message: 'Failed to process payment',
                error: error instanceof Error ? error.message : 'Unknown error'
            });
        }
    }

    async getPayment(req: Request, res: Response): Promise<void> {
        try {
            const paymentId = parseInt(req.params.paymentId);
            if (!paymentId) {
                res.status(400).json({
                    success: false,
                    message: 'Invalid payment ID'
                });
                return;
            }

            const payment = await this.paymentService.getPayment(paymentId);
            if (!payment) {
                res.status(404).json({
                    success: false,
                    message: 'Payment not found'
                });
                return;
            }

            const transactions = await this.paymentService.getPaymentTransactions(paymentId);
            const refunds = await this.paymentService.getRefunds(paymentId);

            res.status(200).json({
                success: true,
                data: {
                    payment,
                    transactions,
                    refunds
                }
            });
        } catch (error) {
            logger.error('Error getting payment:', error);
            res.status(500).json({
                success: false,
                message: 'Failed to get payment',
                error: error instanceof Error ? error.message : 'Unknown error'
            });
        }
    }

    async getPaymentsByOrder(req: Request, res: Response): Promise<void> {
        try {
            const orderId = parseInt(req.params.orderId);
            if (!orderId) {
                res.status(400).json({
                    success: false,
                    message: 'Invalid order ID'
                });
                return;
            }

            const payments = await this.paymentService.getPaymentsByOrder(orderId);

            res.status(200).json({
                success: true,
                data: payments
            });
        } catch (error) {
            logger.error('Error getting payments by order:', error);
            res.status(500).json({
                success: false,
                message: 'Failed to get payments',
                error: error instanceof Error ? error.message : 'Unknown error'
            });
        }
    }

    async getPaymentsByCustomer(req: Request, res: Response): Promise<void> {
        try {
            const customerId = parseInt(req.params.customerId);
            if (!customerId) {
                res.status(400).json({
                    success: false,
                    message: 'Invalid customer ID'
                });
                return;
            }

            const payments = await this.paymentService.getPaymentsByCustomer(customerId);

            res.status(200).json({
                success: true,
                data: payments
            });
        } catch (error) {
            logger.error('Error getting payments by customer:', error);
            res.status(500).json({
                success: false,
                message: 'Failed to get payments',
                error: error instanceof Error ? error.message : 'Unknown error'
            });
        }
    }

    async processRefund(req: Request, res: Response): Promise<void> {
        try {
            const paymentId = parseInt(req.params.paymentId);
            if (!paymentId) {
                res.status(400).json({
                    success: false,
                    message: 'Invalid payment ID'
                });
                return;
            }

            const validation = this.validateRefundRequest(req.body);
            if (validation.error) {
                res.status(400).json({
                    success: false,
                    message: 'Invalid request data',
                    errors: validation.error.details.map(detail => detail.message)
                });
                return;
            }

            const request: RefundRequest = {
                paymentId,
                ...validation.value
            };

            const refund = await this.paymentService.processRefund(request);

            res.status(200).json({
                success: true,
                message: 'Refund initiated successfully',
                data: {
                    refundId: refund.id,
                    status: refund.status,
                    amount: refund.amount,
                    currency: refund.currency
                }
            });
        } catch (error) {
            logger.error('Error processing refund:', error);
            res.status(500).json({
                success: false,
                message: 'Failed to process refund',
                error: error instanceof Error ? error.message : 'Unknown error'
            });
        }
    }

    private validateCreatePaymentRequest(data: any) {
        const schema = Joi.object({
            orderId: Joi.number().integer().positive().required(),
            customerId: Joi.number().integer().positive().required(),
            amount: Joi.number().positive().required(),
            currency: Joi.string().length(3).uppercase().default('VND'),
            paymentMethod: Joi.string().valid(...Object.values(PaymentMethodType)).required()
        });

        return schema.validate(data);
    }

    private validateProcessPaymentRequest(data: any) {
        const schema = Joi.object({
            cardNumber: Joi.string().pattern(/^\d{13,19}$/).when('paymentMethod', {
                is: Joi.exist(),
                then: Joi.required(),
                otherwise: Joi.optional()
            }),
            expiryMonth: Joi.number().integer().min(1).max(12).when('cardNumber', {
                is: Joi.exist(),
                then: Joi.required(),
                otherwise: Joi.optional()
            }),
            expiryYear: Joi.number().integer().min(new Date().getFullYear()).when('cardNumber', {
                is: Joi.exist(),
                then: Joi.required(),
                otherwise: Joi.optional()
            }),
            cvv: Joi.string().pattern(/^\d{3,4}$/).when('cardNumber', {
                is: Joi.exist(),
                then: Joi.required(),
                otherwise: Joi.optional()
            }),
            holderName: Joi.string().min(2).max(100).when('cardNumber', {
                is: Joi.exist(),
                then: Joi.required(),
                otherwise: Joi.optional()
            })
        });

        return schema.validate(data);
    }

    private validateRefundRequest(data: any) {
        const schema = Joi.object({
            amount: Joi.number().positive().optional(),
            reason: Joi.string().valid(...Object.values(RefundReason)).required(),
            description: Joi.string().max(500).optional()
        });

        return schema.validate(data);
    }
}