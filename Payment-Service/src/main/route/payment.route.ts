import { Router } from 'express';
import {PaymentController} from "../controller/payment.controller";



const router = Router();
const paymentController = new PaymentController();

router.post('/', paymentController.createPayment.bind(paymentController));
router.post('/:paymentId/process', paymentController.processPayment.bind(paymentController));
router.get('/:paymentId', paymentController.getPayment.bind(paymentController));
router.post('/:paymentId/refund', paymentController.processRefund.bind(paymentController));

router.get('/order/:orderId', paymentController.getPaymentsByOrder.bind(paymentController));
router.get('/customer/:customerId', paymentController.getPaymentsByCustomer.bind(paymentController));

export { router as paymentRoutes };

