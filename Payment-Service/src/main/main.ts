import 'reflect-metadata';
import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import morgan from 'morgan';
import dotenv from 'dotenv';

import { AppDataSource } from '@/config/database.config';
import { KafkaService } from './service/kafka.service';


// Background jobs
import './jobs/payment.jobs';
import logger from "winston";

dotenv.config();

const app = express();
const port = process.env.PORT || 8003;

async function startServer() {
    try {
        // Initialize database
        await AppDataSource.initialize();
        logger.info('Database connected successfully');

        // Initialize Kafka
        await KafkaService.getInstance().connect();
        logger.info('Kafka connected successfully');

        // Middleware
        app.use(helmet({
            contentSecurityPolicy: {
                directives: {
                    defaultSrc: ["'self'"],
                    styleSrc: ["'self'", "'unsafe-inline'"],
                    scriptSrc: ["'self'"],
                    imgSrc: ["'self'", "data:", "https:"],
                },
            },
        }));

        app.use(compression());
        app.use(morgan('combined', { stream: { write: (message) => logger.info(message.trim()) } }));

        // Rate limiting
        app.use('/api', rateLimiter);

        // CORS
        app.use(cors({
            origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'],
            credentials: true,
        }));

        // Body parsers
        app.use('/api/webhooks', express.raw({ type: 'application/json' })); // Raw for webhooks
        app.use(express.json({ limit: '10mb' }));
        app.use(express.urlencoded({ extended: true, limit: '10mb' }));

        // Routes
        app.use('/health', healthRoutes);
        app.use('/api/webhooks', webhookRoutes);
        app.use('/api/payments', paymentRoutes);

        // Error handling
        app.use(errorHandler);

        // 404 handler
        app.use('*', (req, res) => {
            res.status(404).json({
                success: false,
                message: 'Route not found',
                timestamp: new Date().toISOString(),
            });
        });

        // Graceful shutdown
        process.on('SIGTERM', async () => {
            logger.info('SIGTERM received, shutting down gracefully');
            await gracefulShutdown();
        });

        process.on('SIGINT', async () => {
            logger.info('SIGINT received, shutting down gracefully');
            await gracefulShutdown();
        });

        app.listen(port, () => {
            logger.info(`Payment Service started on port ${port}`);
            logger.info(`Environment: ${process.env.NODE_ENV || 'development'}`);
        });

    } catch (error) {
        logger.error('Failed to start server:', error);
        process.exit(1);
    }
}

async function gracefulShutdown() {
    try {
        // Close database connection
        if (AppDataSource.isInitialized) {
            await AppDataSource.destroy();
            logger.info('Database connection closed');
        }

        // Close Kafka connection
        await KafkaService.getInstance().disconnect();
        logger.info('Kafka connection closed');

        process.exit(0);
    } catch (error) {
        logger.error('Error during shutdown:', error);
        process.exit(1);
    }
}

startServer().then(logger.info(`Payment Service is listening on port ${port}`));