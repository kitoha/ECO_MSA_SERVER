-- Carts 테이블 생성
CREATE TABLE carts (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_carts_user_id ON carts(user_id);
CREATE INDEX idx_carts_deleted_at ON carts(deleted_at);
CREATE INDEX idx_carts_created_at ON carts(created_at DESC);
