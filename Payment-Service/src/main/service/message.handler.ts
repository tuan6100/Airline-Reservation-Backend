import {PaymentService} from './payment.service';
import {RefundReason} from "../model/entity/refund.entity";
import logger from "../util/logger";


export class MessageHandler {
    private paymentService: PaymentService;

    constructor() {
        this.paymentService = new PaymentService();
    }

    async handleOrderCreated(data: any): Promise<void> {
        try {
            logger.info('Handling order created event', { orderId: data.orderId });
        } catch (error) {
            logger.error('Error handling order created event:', error);
        }
    }

    async handleOrderConfirmed(data: any): Promise<void> {
        try {
            logger.info('Handling order confirmed event', { orderId: data.orderId });
            const existingPayments = await this.paymentService.getPaymentsByOrder(data.orderId);
            if (existingPayments.length === 0) {
                logger.warn('No payment found for confirmed order', { orderId: data.orderId });
            }
        } catch (error) {
            logger.error('Error handling order confirmed event:', error);
        }
    }

    async handleOrderCancelled(data: any): Promise<void> {
        try {
            logger.info('Handling order cancelled event', { orderId: data.orderId });
            const payments = await this.paymentService.getPaymentsByOrder(data.orderId);
            for (const payment of payments) {
                if (payment.status === 'succeeded') {
                    await this.paymentService.processRefund({
                        paymentId: payment.paymentId,
                        reason: RefundReason.REQUESTED_BY_CUSTOMER,
                        description: `Refund due to order cancellation: ${data.reason || 'No reason provided'}`
                    });
                }
            }
        } catch (error) {
            logger.error('Error handling order cancelled event:', error);
        }
    }

    async handleBookingConfirmed(data: any): Promise<void> {
        try {
            logger.info('Handling booking confirmed event', { bookingId: data.bookingId });
        } catch (error) {
            logger.error('Error handling booking confirmed event:', error);
        }
    }

    async handleBookingCancelled(data: any): Promise<void> {
        try {
            logger.info('Handling booking cancelled event', { bookingId: data.bookingId });
        } catch (error) {
            logger.error('Error handling booking cancelled event:', error);
        }
    }
}