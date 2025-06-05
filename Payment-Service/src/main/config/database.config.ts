import { DataSource } from 'typeorm';
import {Payment} from "@/main/model/entity/payment.entity";
import {PaymentTransaction} from "@/main/model/entity/payment-transaction.entity";
import logger from "winston";
import {Refund} from "@/main/model/entity/refund.entity";
import {OutboxMessage} from "@/main/model/entity/outbox-message.entity";



export const AppDataSource = new DataSource({
    type: 'postgres',
    host: process.env.DB_HOST ,
    port: parseInt(process.env.DB_PORT?? '5432'),
    username: process.env.DB_USERNAME ,
    password: process.env.DB_PASSWORD ,
    database: process.env.DB_NAME,
    synchronize: false,
    logging: process.env.NODE_ENV === 'development' ? 'all' : ['error'],
    entities: [
        Payment,
        PaymentTransaction,
        OutboxMessage,
        Refund,
    ],
    migrations: ['src/migrations/*.ts'],
    subscribers: ['src/subscribers/*.ts'],
    maxQueryExecutionTime: 1000,
    extra: {
        connectionLimit: 10,
        acquireTimeoutMillis: 30000,
        timeout: 60000,
    },
});


export async function checkDatabaseConnection(): Promise<boolean> {
    try {
        if (!AppDataSource.isInitialized) {
            await AppDataSource.initialize();
        }

        await AppDataSource.query('SELECT 1');
        return true;
    } catch (error) {
        logger.error('Database health check failed:', error);
        return false;
    }
}