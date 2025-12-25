-- Inventory Reservations 테이블 생성
CREATE TABLE inventory_reservations (
    id BIGSERIAL PRIMARY KEY,
    inventory_id BIGINT NOT NULL,
    order_id VARCHAR(100) NOT NULL,  -- Order Service의 order_id (외래키 아님)
    quantity INT NOT NULL,
    status VARCHAR(20) NOT NULL,  -- ACTIVE, COMPLETED, CANCELLED
    expires_at TIMESTAMP NOT NULL,  -- 예약 만료 시간 (15분)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    FOREIGN KEY (inventory_id) REFERENCES inventories(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX idx_inventory_reservations_inventory_id ON inventory_reservations(inventory_id);
CREATE INDEX idx_inventory_reservations_order_id ON inventory_reservations(order_id);
CREATE INDEX idx_inventory_reservations_status ON inventory_reservations(status);
CREATE INDEX idx_inventory_reservations_expires_at ON inventory_reservations(expires_at);

-- 만료된 ACTIVE 예약 조회 최적화
CREATE INDEX idx_inventory_reservations_active_expired
    ON inventory_reservations(status, expires_at)
    WHERE status = 'ACTIVE';

-- 제약조건: quantity는 양수
ALTER TABLE inventory_reservations ADD CONSTRAINT check_reservation_quantity_positive
    CHECK (quantity > 0);
