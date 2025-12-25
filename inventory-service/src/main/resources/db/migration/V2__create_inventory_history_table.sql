-- Inventory History 테이블 생성
CREATE TABLE inventory_history (
    id BIGSERIAL PRIMARY KEY,
    inventory_id BIGINT NOT NULL,
    change_type VARCHAR(20) NOT NULL,  -- INCREASE, DECREASE, RESERVE, RELEASE
    quantity INT NOT NULL,              -- 변동 수량
    before_quantity INT NOT NULL,       -- 변경 전 사용가능 수량
    after_quantity INT NOT NULL,        -- 변경 후 사용가능 수량
    reason VARCHAR(100),                -- 변동 사유
    reference_id VARCHAR(100),          -- 참조 ID (주문번호, 입고번호 등)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    FOREIGN KEY (inventory_id) REFERENCES inventories(id) ON DELETE CASCADE
);

-- 인덱스 생성 (조회 성능 최적화)
CREATE INDEX idx_inventory_history_inventory_id ON inventory_history(inventory_id);
CREATE INDEX idx_inventory_history_created_at ON inventory_history(created_at DESC);
CREATE INDEX idx_inventory_history_change_type ON inventory_history(change_type);
CREATE INDEX idx_inventory_history_reference_id ON inventory_history(reference_id);

-- 복합 인덱스: 특정 재고의 날짜별 이력 조회에 최적화
CREATE INDEX idx_inventory_history_inventory_created ON inventory_history(inventory_id, created_at DESC);
