import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    ManyToOne,
    JoinColumn,
    Index,
} from 'typeorm';
import { Payment } from './payment.entity';

export enum RefundStatus {
    PENDING = 'pending',
    PROCESSING = 'processing',
    SUCCEEDED = 'succeeded',
    FAILED = 'failed',
    CANCELLED = 'cancelled',
}

export enum RefundReason {
    DUPLICATE = 'duplicate',
    FRAUDULENT = 'fraudulent',
    REQUESTED_BY_CUSTOMER = 'requested_by_customer',
    EXPIRED_UNCAPTURED = 'expired_uncaptured',
    OTHER = 'other',
}

@Entity('refunds')
@Index(['paymentId'])
@Index(['status'])
export class Refund {
    @PrimaryGeneratedColumn('uuid')
    id: string;

    @Column({ type: 'int', name: 'payment_id' })
    paymentId: number;

    @Column({ type: 'bigint' })
    amount: number;

    @Column({ type: 'varchar', length: 3 })
    currency: string;

    @Column({
        type: 'enum',
        enum: RefundStatus,
        default: RefundStatus.PENDING
    })
    status: RefundStatus;

    @Column({
        type: 'enum',
        enum: RefundReason,
        nullable: true
    })
    reason?: RefundReason;

    @Column({ type: 'text', nullable: true })
    description?: string;

    @Column({ type: 'jsonb', nullable: true })
    metadata?: Record<string, any>;

    @Column({ type: 'varchar', length: 500, nullable: true, name: 'failure_reason' })
    failureReason?: string;

    @CreateDateColumn({ name: 'created_at' })
    createdAt: Date;

    @UpdateDateColumn({ name: 'updated_at' })
    updatedAt: Date;

    @ManyToOne(() => Payment)
    @JoinColumn({ name: 'payment_id' })
    payment: Payment;
}
