import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    ManyToOne,
    JoinColumn,
    Index,
} from 'typeorm';
import { Payment } from './payment.entity';

export enum TransactionType {
    AUTHORIZATION = 'authorization',
    CAPTURE = 'capture',
    REFUND = 'refund',
    VOID = 'void',
}

export enum TransactionStatus {
    PENDING = 'pending',
    SUCCESS = 'success',
    FAILED = 'failed',
}

@Entity('payment_transactions')
@Index(['paymentId'])
@Index(['type'])
export class PaymentTransaction {
    @PrimaryGeneratedColumn('uuid')
    id: string;

    @Column({ type: 'int', name: 'payment_id' })
    paymentId: number;

    @Column({
        type: 'enum',
        enum: TransactionType
    })
    type: TransactionType;

    @Column({
        type: 'enum',
        enum: TransactionStatus,
        default: TransactionStatus.PENDING
    })
    status: TransactionStatus;

    @Column({ type: 'bigint' })
    amount: number;

    @Column({ type: 'varchar', length: 3 })
    currency: string;

    @Column({ type: 'text', nullable: true, name: 'response_message' })
    responseMessage?: string;

    @Column({ type: 'jsonb', nullable: true })
    metadata?: Record<string, any>;

    @CreateDateColumn({ name: 'created_at' })
    createdAt: Date;

    @ManyToOne(() => Payment)
    @JoinColumn({ name: 'payment_id' })
    payment: Payment;
}