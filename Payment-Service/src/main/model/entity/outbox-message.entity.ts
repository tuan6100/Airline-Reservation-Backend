import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    CreateDateColumn,
    Index,
} from 'typeorm';

@Entity('outbox_events')
@Index(['processed'])
@Index(['createdAt'])
export class OutboxMessage {
    @PrimaryGeneratedColumn('uuid')
    id: string;

    @Column({ type: 'varchar', length: 100, name: 'aggregate_type' })
    aggregateType: string;

    @Column({ type: 'varchar', length: 255, name: 'aggregate_id' })
    aggregateId: string;

    @Column({ type: 'varchar', length: 100, name: 'event_type' })
    eventType: string;

    @Column({ type: 'jsonb' })
    payload: Record<string, any>;

    @CreateDateColumn({ name: 'created_at' })
    createdAt: Date;

    @Column({ type: 'boolean', default: false })
    processed: boolean;

    @Column({ type: 'timestamp', nullable: true, name: 'processed_at' })
    processedAt?: Date;

    @Column({ type: 'int', default: 0, name: 'retry_count' })
    retryCount: number;

    @Column({ type: 'text', nullable: true, name: 'error_message' })
    errorMessage?: string;


}