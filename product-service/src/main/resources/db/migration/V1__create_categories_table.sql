-- Categories 테이블 생성
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_category_id BIGINT,
    depth INT NOT NULL DEFAULT 0,
    slug VARCHAR(200) NOT NULL UNIQUE,
    display_order INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    FOREIGN KEY (parent_category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- 인덱스 생성
CREATE INDEX idx_categories_parent_category_id ON categories(parent_category_id);
CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_categories_status ON categories(status);
CREATE INDEX idx_categories_deleted_at ON categories(deleted_at);

-- 기본 카테고리 데이터
INSERT INTO categories (name, slug, depth, display_order, status) VALUES
('전자기기', 'electronics', 0, 1, 'ACTIVE'),
('패션', 'fashion', 0, 2, 'ACTIVE'),
('생활용품', 'living', 0, 3, 'ACTIVE'),
('도서', 'books', 0, 4, 'ACTIVE');
