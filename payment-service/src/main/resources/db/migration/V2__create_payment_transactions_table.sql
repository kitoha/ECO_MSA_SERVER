-- Payment Transactions 테이블 생성
CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,             -- payments 테이블의 id

    transaction_type VARCHAR(20) NOT NULL,  -- AUTH, CAPTURE, CANCEL, REFUND
    amount DECIMAL(19, 2) NOT NULL,         -- 트랜잭션 금액
    status VARCHAR(20) NOT NULL,            -- SUCCESS, FAILED

    -- PG사 응답 정보
    pg_transaction_id VARCHAR(255),         -- PG사 트랜잭션 ID
    pg_response_code VARCHAR(20),           -- PG사 응답 코드
    pg_response_message TEXT,               -- PG사 응답 메시지

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,                   -- Soft delete

    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX idx_payment_transactions_payment_id ON payment_transactions(payment_id);
CREATE INDEX idx_payment_transactions_type ON payment_transactions(transaction_type);
CREATE INDEX idx_payment_transactions_created_at ON payment_transactions(created_at DESC);

-- 제약조건: 금액은 양수여야 함
ALTER TABLE payment_transactions ADD CONSTRAINT check_transaction_amount_positive
    CHECK (amount > 0);

-- 코멘트 추가
COMMENT ON TABLE payment_transactions IS '결제 트랜잭션 이력을 저장하는 테이블 (PG사와의 모든 통신 기록)';
COMMENT ON COLUMN payment_transactions.transaction_type IS '트랜잭션 타입 (AUTH: 인증, CAPTURE: 승인, CANCEL: 취소, REFUND: 환불)';
COMMENT ON COLUMN payment_transactions.status IS '트랜잭션 처리 결과 (SUCCESS: 성공, FAILED: 실패)';
COMMENT ON COLUMN payment_transactions.pg_transaction_id IS 'PG사에서 발급한 트랜잭션 고유 ID';
