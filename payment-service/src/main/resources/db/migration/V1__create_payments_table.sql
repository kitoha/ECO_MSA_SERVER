-- Payments 테이블 생성
CREATE TABLE payments (
    id BIGINT PRIMARY KEY,  -- TSID (Time-Sorted Unique Identifier)
    order_id VARCHAR(100) NOT NULL UNIQUE,  -- Order Service의 order_id (외래키 아님)
    user_id VARCHAR(100) NOT NULL,          -- User Service의 user_id (외래키 아님)

    amount DECIMAL(19, 2) NOT NULL,         -- 결제 금액
    status VARCHAR(20) NOT NULL,            -- PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED

    payment_method VARCHAR(50),             -- CARD, BANK_TRANSFER, VIRTUAL_ACCOUNT, EASY_PAY, MOBILE
    pg_provider VARCHAR(50),                -- PG사 (TOSS, STRIPE, INICIS 등)
    pg_payment_key VARCHAR(255) UNIQUE,     -- PG사 결제 고유키

    failure_reason TEXT,                    -- 결제 실패/취소 사유

    approved_at TIMESTAMP,                  -- 결제 승인 시각
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,                   -- Soft delete

    version INT NOT NULL DEFAULT 0          -- 낙관적 락 (동시성 제어)
);

-- 인덱스 생성
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_pg_payment_key ON payments(pg_payment_key);
CREATE INDEX idx_payments_created_at ON payments(created_at DESC);

-- 제약조건: 금액은 양수여야 함
ALTER TABLE payments ADD CONSTRAINT check_amount_positive
    CHECK (amount > 0);

-- 코멘트 추가
COMMENT ON TABLE payments IS '결제 정보를 저장하는 테이블';
COMMENT ON COLUMN payments.id IS 'TSID 기반 고유 식별자';
COMMENT ON COLUMN payments.order_id IS '주문 ID (Order Service 참조)';
COMMENT ON COLUMN payments.user_id IS '사용자 ID (User Service 참조)';
COMMENT ON COLUMN payments.pg_payment_key IS 'PG사에서 발급한 결제 고유키';
COMMENT ON COLUMN payments.version IS '낙관적 락을 위한 버전 (동시성 제어)';
