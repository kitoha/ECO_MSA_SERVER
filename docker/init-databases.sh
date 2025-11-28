#!/bin/bash
set -e

# PostgreSQL 컨테이너 시작 시 자동으로 여러 데이터베이스 생성
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Product Service Database
    CREATE DATABASE product_db;
    GRANT ALL PRIVILEGES ON DATABASE product_db TO postgres;

    -- Inventory Service Database
    CREATE DATABASE inventory_db;
    GRANT ALL PRIVILEGES ON DATABASE inventory_db TO postgres;

    -- Cart Service Database
    CREATE DATABASE cart_db;
    GRANT ALL PRIVILEGES ON DATABASE cart_db TO postgres;

    -- Order Service Database
    CREATE DATABASE order_db;
    GRANT ALL PRIVILEGES ON DATABASE order_db TO postgres;

    -- Payment Service Database
    CREATE DATABASE payment_db;
    GRANT ALL PRIVILEGES ON DATABASE payment_db TO postgres;

    -- User Service Database (향후 추가)
    CREATE DATABASE user_db;
    GRANT ALL PRIVILEGES ON DATABASE user_db TO postgres;
EOSQL

# pgvector 확장 활성화 (각 데이터베이스별)
for db in product_db inventory_db cart_db order_db payment_db user_db; do
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$db" <<-EOSQL
        CREATE EXTENSION IF NOT EXISTS vector;
EOSQL
    echo "✅ pgvector extension enabled for $db"
done

echo "✅ All databases created successfully with pgvector support!"
