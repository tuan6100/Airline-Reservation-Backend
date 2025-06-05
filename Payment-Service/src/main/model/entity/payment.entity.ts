import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    UpdateDateColumn,
    Index, Long, ManyToOne,
} from 'typeorm';

export enum PaymentStatus {
    PENDING = 'pending',
    PROCESSING = 'processing',
    SUCCEEDED = 'succeeded',
    FAILED = 'failed',
    CANCELLED = 'cancelled',
}

export enum PaymentMethodType {
    CREDIT_CARD = 'credit_card',
    DEBIT_CARD = 'debit_card',
    BANK_TRANSFER = 'bank_transfer',
    E_WALLET = 'e_wallet',
    CASH = 'cash',
}

@Entity('payment')
@Index(['orderId'])
@Index(['customerId'])
@Index(['status'])
export class Payment {
    @PrimaryGeneratedColumn({ name: 'payment_id' })
    paymentId: Long;

    @Column({ type: 'bigint', name: 'order_id' })
    orderId: Long;

    @Column({ type: 'bigint', name: 'customer_id' })
    customerId: Long;

    @Column({ type: 'bigint' })
    amount: number;

    @Column({ type: 'varchar', length: 3, default: 'VND' })
    currency: string;

    @Column({
        type: 'enum',
        enum: PaymentStatus,
        default: PaymentStatus.PENDING
    })
    status: PaymentStatus;

    @Column({
        type: 'enum',
        enum: PaymentMethodType,
        name: 'payment_method'
    })
    paymentMethod: PaymentMethodType;

    @Column({ type: 'timestamp', nullable: true, name: 'paid_at' })
    paidAt?: Date;

    @Column({ type: 'varchar', length: 500, nullable: true, name: 'failure_reason' })
    failureReason?: string;

    @CreateDateColumn({ name: 'created_at' })
    createdAt: Date;

    @UpdateDateColumn({ name: 'updated_at' })
    updatedAt: Date;
}