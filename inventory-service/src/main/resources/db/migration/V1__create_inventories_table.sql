-- Inventories 테이블 생성
CREATE TABLE inventories (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(100) NOT NULL UNIQUE,  -- Product Service의 product_id (외래키 아님)
    available_quantity INT NOT NULL DEFAULT 0,  -- 사용 가능한 재고
    reserved_quantity INT NOT NULL DEFAULT 0,   -- 예약된 재고
    total_quantity INT NOT NULL DEFAULT 0,      -- 총 재고 (available + reserved)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    version INT NOT NULL DEFAULT 0  -- 낙관적 락
);

-- 인덱스 생성
CREATE INDEX idx_inventories_product_id ON inventories(product_id);

-- 제약조건: total_quantity = available_quantity + reserved_quantity
-- PostgreSQL CHECK constraint
ALTER TABLE inventories ADD CONSTRAINT check_total_quantity
    CHECK (total_quantity = available_quantity + reserved_quantity);

-- 제약조건: 수량은 음수가 될 수 없음
ALTER TABLE inventories ADD CONSTRAINT check_available_quantity_positive
    CHECK (available_quantity >= 0);

ALTER TABLE inventories ADD CONSTRAINT check_reserved_quantity_positive
    CHECK (reserved_quantity >= 0);

ALTER TABLE inventories ADD CONSTRAINT check_total_quantity_positive
    CHECK (total_quantity >= 0);
