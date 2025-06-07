import { Router } from 'express';
import { Request, Response } from 'express';
import logger from "../util/logger";
const router = Router();
router.post('/stripe', (req: Request, res: Response) => {
    try {
        logger.info('Stripe webhook received');
        res.status(200).json({ received: true });
    } catch (error) {
        logger.error('Error handling Stripe webhook:', error);
        res.status(400).json({ error: 'Webhook handling failed' });
    }
});

router.post('/paypal', (req: Request, res: Response) => {
    try {
        logger.info('PayPal webhook received');
        res.status(200).json({ received: true });
    } catch (error) {
        logger.error('Error handling PayPal webhook:', error);
        res.status(400).json({ error: 'Webhook handling failed' });
    }
});

export { router as webhookRoutes };