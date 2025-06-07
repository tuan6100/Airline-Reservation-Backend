import 'reflect-metadata';
import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import morgan from 'morgan';
import dotenv from 'dotenv';


import { KafkaService } from './service/kafka.service';
import {AppDataSource} from "./config/database.config";
import logger from "./util/logger";


dotenv.config();

const app = express();
const port = process.env.PORT || 8003;

async function startServer() {
    try {
        await AppDataSource.initialize();
        logger.info('Database connected successfully');
        await KafkaService.getInstance().connect();
        logger.info('Kafka connected successfully');
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
        app.use(morgan('combined', {
            stream: {
                write: (message) => logger.info(message.trim())
            }
        }));
        app.use(cors({
            origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'],
            credentials: true,
        }));
        app.use('/api/webhooks', express.raw({ type: 'application/json' }));
        app.use(express.json({ limit: '10mb' }));
        app.use(express.urlencoded({ extended: true, limit: '10mb' }));

        app.use('*', (req, res) => {
            res.status(404).json({
                success: false,
                message: 'Route not found',
                timestamp: new Date().toISOString(),
            });
        });

        process.on('SIGTERM', async () => {
            logger.info('SIGTERM received, shutting down gracefully');
            await gracefulShutdown();
        });

        process.on('SIGINT', async () => {
            logger.info('SIGINT received, shutting down gracefully');
            await gracefulShutdown();
        });

        process.on('uncaughtException', (error) => {
            logger.error('Uncaught Exception:', error);
            process.exit(1);
        });

        process.on('unhandledRejection', (reason, promise) => {
            logger.error('Unhandled Rejection at:', promise, 'reason:', reason);
            process.exit(1);
        });

        app.listen(port, () => {
            logger.info(`Payment Service started on port ${port}`);
            logger.info(`Environment: ${process.env.NODE_ENV || 'development'}`);
            logger.info(`Health check available at: http://localhost:${port}/health`);
        });

    } catch (error) {
        logger.error('Failed to start server:', error);
        process.exit(1);
    }
}

async function gracefulShutdown() {
    try {
        if (AppDataSource.isInitialized) {
            await AppDataSource.destroy();
            logger.info('Database connection closed');
        }
        await KafkaService.getInstance().disconnect();
        logger.info('Kafka connection closed');
        process.exit(0);
    } catch (error) {
        logger.error('Error during shutdown:', error);
        process.exit(1);
    }
}

startServer().then(() => logger.info(`App listening on port ${port}`));