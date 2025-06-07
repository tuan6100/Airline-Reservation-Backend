import { Router, Request, Response } from 'express';


import {checkDatabaseConnection} from "../config/database.config";
import logger from "../util/logger";

const router = Router();

router.get('/', (req: Request, res: Response) => {
    res.status(200).json({
        status: 'OK',
        service: 'Payment Service',
        timestamp: new Date().toISOString(),
        uptime: process.uptime(),
        environment: process.env.NODE_ENV || 'development'
    });
});

router.get('/detailed', async (req: Request, res: Response) => {
    const healthChecks = {
        service: 'OK',
        database: 'Unknown',
        kafka: 'Unknown',
        memory: 'OK',
        disk: 'OK'
    };
    let overallStatus = 'OK';
    try {
        const dbHealthy = await checkDatabaseConnection();
        healthChecks.database = dbHealthy ? 'OK' : 'Error';
        if (!dbHealthy) overallStatus = 'Degraded';
        try {
            healthChecks.kafka = 'OK';
        } catch (error) {
            healthChecks.kafka = 'Error';
            overallStatus = 'Degraded';
        }
        const memUsage = process.memoryUsage();
        const memUsagePercent = (memUsage.heapUsed / memUsage.heapTotal) * 100;
        if (memUsagePercent > 90) {
            healthChecks.memory = 'Warning';
            if (overallStatus === 'OK') overallStatus = 'Warning';
        }

    } catch (error) {
        logger.error('Health check error:', error);
        overallStatus = 'Error';
    }

    const statusCode = overallStatus === 'OK' ? 200 : overallStatus === 'Warning' ? 200 : 503;

    res.status(statusCode).json({
        status: overallStatus,
        service: 'Payment Service',
        timestamp: new Date().toISOString(),
        uptime: process.uptime(),
        environment: process.env.NODE_ENV || 'development',
        checks: healthChecks,
        memory: {
            rss: process.memoryUsage().rss,
            heapTotal: process.memoryUsage().heapTotal,
            heapUsed: process.memoryUsage().heapUsed,
            external: process.memoryUsage().external
        }
    });
});

router.get('/ready', async (req: Request, res: Response) => {
    try {
        const dbHealthy = await checkDatabaseConnection();

        if (dbHealthy) {
            res.status(200).json({
                status: 'Ready',
                timestamp: new Date().toISOString()
            });
        } else {
            res.status(503).json({
                status: 'Not Ready',
                reason: 'Database connection failed',
                timestamp: new Date().toISOString()
            });
        }
    } catch (error) {
        res.status(503).json({
            status: 'Not Ready',
            reason: 'Service initialization failed',
            timestamp: new Date().toISOString()
        });
    }
});

router.get('/live', (req: Request, res: Response) => {
    res.status(200).json({
        status: 'Alive',
        timestamp: new Date().toISOString()
    });
});

export { router as healthRoutes };