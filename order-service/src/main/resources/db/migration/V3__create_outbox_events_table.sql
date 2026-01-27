-- Outbox Events 테이블 생성 (Transactional Outbox Pattern)
CREATE TABLE outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    kafka_key VARCHAR(255),
    payload BYTEA NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성 (Debezium CDC 성능 최적화)
CREATE INDEX idx_outbox_created_at ON outbox_events(created_at);
CREATE INDEX idx_outbox_aggregate_type ON outbox_events(aggregate_type);
CREATE INDEX idx_outbox_aggregate_id ON outbox_events(aggregate_id);

-- 테이블 코멘트
COMMENT ON TABLE outbox_events IS 'Transactional Outbox for reliable event publishing with Debezium CDC';
COMMENT ON COLUMN outbox_events.aggregate_type IS 'Aggregate type (e.g., ORDER, PAYMENT)';
COMMENT ON COLUMN outbox_events.aggregate_id IS 'Aggregate identifier (e.g., order number)';
COMMENT ON COLUMN outbox_events.event_type IS 'Event type (e.g., OrderCreated, InventoryReservationRequest)';
COMMENT ON COLUMN outbox_events.topic IS 'Target Kafka topic name';
COMMENT ON COLUMN outbox_events.kafka_key IS 'Kafka message key for partitioning';
COMMENT ON COLUMN outbox_events.payload IS 'Serialized event payload (Protobuf binary)';
