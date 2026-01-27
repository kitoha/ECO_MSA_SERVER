#  E-Commerce Microservices Platform

<div align="center">

**Production-Ready Event-Driven E-Commerce Backend**

### Tech Stack

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-6DB33F?logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-7.5-231F20?logo=apache-kafka&logoColor=white)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-24.0+-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)

### Architecture & Design

[![Microservices](https://img.shields.io/badge/Architecture-Microservices-FF6B6B)]()
[![Event Driven](https://img.shields.io/badge/Pattern-Event%20Driven-4ECDC4)]()
[![DDD](https://img.shields.io/badge/Design-Domain%20Driven-F38181)]()

### Libraries & Tools

[![Querydsl](https://img.shields.io/badge/Querydsl-5.1-blue)](https://querydsl.com/)
[![Protobuf](https://img.shields.io/badge/Protocol%20Buffers-3.25-4285F4?logo=google&logoColor=white)](https://protobuf.dev/)
[![Flyway](https://img.shields.io/badge/Flyway-10.0-CC0200?logo=flyway&logoColor=white)](https://flywaydb.org/)
[![Kotest](https://img.shields.io/badge/Kotest-5.8-6FB536)](https://kotest.io/)
[![Testcontainers](https://img.shields.io/badge/Testcontainers-1.19-2496ED)](https://www.testcontainers.org/)
[![Mockk](https://img.shields.io/badge/Mockk-1.13-4ECDC4)](https://mockk.io/)

### Monitoring & DevOps

[![Prometheus](https://img.shields.io/badge/Prometheus-2.48-E6522C?logo=prometheus&logoColor=white)](https://prometheus.io/)
[![Grafana](https://img.shields.io/badge/Grafana-10.2-F46800?logo=grafana&logoColor=white)](https://grafana.com/)
[![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?logo=github-actions&logoColor=white)](https://github.com/features/actions)
[![SonarCloud](https://img.shields.io/badge/Quality-SonarCloud-4E9BCD?logo=sonarcloud&logoColor=white)](https://sonarcloud.io/)
[![Jacoco](https://img.shields.io/badge/Coverage-Jacoco-green)](https://www.jacoco.org/)

</div>

---

##  Overview

**MSA 기반 이커머스 플랫폼**입니다. DDD와 이벤트 기반 아키텍처를 통해 서비스 간 느슨한 결합을 유지하며, Kafka를 활용한 비동기 메시징으로 확장성과 내결함성을 확보했습니다.
###  Why This Project?

-  **동시성 제어**: Redis와 낙관적 락을 활용한 재고 관리
-  **분산 트랜잭션**: Kafka 기반 이벤트 소싱으로 데이터 일관성 보장
-  **서비스 간 통신**: Protobuf를 활용한 효율적인 메시지 직렬화
-  **인증/인가**: JWT + Google OAuth2 통합
-  **코드 품질**: 67개 테스트 케이스 + SonarCloud 정적 분석 + GitHub Actions CI/CD

---

##  System Architecture

<img width="2992" height="2304" alt="Image" src="https://github.com/user-attachments/assets/133788f2-3c96-4883-91df-2e7eb5f54678" />

### 서비스 구성

| Service | Port | Description | Key Tech |
|---------|------|-------------|----------|
| **API Gateway** | 8080 | 라우팅, JWT 검증, CORS | Spring Cloud Gateway |
| **Eureka Server** | 8761 | 서비스 디스커버리 | Netflix Eureka |
| **Product Service** | 8081 | 상품 관리 (CRUD, 검색) | Querydsl, TSID |
| **Inventory Service** | 8086 | 재고 관리 (예약/확정/해제) | Redis, Optimistic Lock |
| **Order Service** | 8083 | 주문 생성/취소 | Kafka Producer |
| **Payment Service** | 8084 | 결제 처리 (Stripe 연동) | Kafka Consumer |
| **Cart Service** | 8082 | 장바구니 관리 | JWT Auth |
| **User Service** | 8087 | 사용자 관리 (OAuth2) | Google OAuth2 |

---

##  Key Features

### 1. 이벤트 기반 분산 트랜잭션 (Saga Pattern)

주문 생성 시 3개 서비스 간 비동기 협업으로 데이터 정합성을 유지합니다.

**[시퀀스 다이어그램]**  
**Order Flow (Success Case)**:

<img width="1780" height="1340" alt="Image" src="https://github.com/user-attachments/assets/0c497ece-0362-4137-826a-190f7fe45198" />

**Compensation Flow (Failure Case)**:  
<img width="1780" height="1340" alt="Image" src="https://github.com/user-attachments/assets/7fd22fbd-922e-4998-b5a7-3a665bf1bf03" />

**핵심 구현 포인트**:
- **Protobuf 기반 이벤트 스키마**: 타입 안정성과 하위 호환성 보장
- **At-Least-Once Delivery**: Kafka의 Manual Ack로 메시지 유실 방지
- **트랜잭션 이력 추적**: `PaymentTransaction` 테이블로 모든 결제 상태 변경 기록

### 2. 동시성 제어 (Race Condition 대응)

**문제**: 100개 재고에 200개 주문 요청 시 overselling 발생 가능
**해결**:

```kotlin
// Inventory Entity - JPA Optimistic Lock
@Version
val version: Int = 0

fun reserveStock(amount: Int) {
    if (this.availableQuantity < amount)
        throw IllegalArgumentException("사용 가능한 재고가 부족합니다.")
    this.availableQuantity -= amount
    this.reservedQuantity += amount
}
```

- **낙관적 락 (@Version)**: 버전 불일치 시 `OptimisticLockException` 발생 → 재시도 로직
- **예약-확정 2단계 프로세스**:
  1. 주문 생성 시 재고 예약 (available → reserved)
  2. 결제 완료 시 예약 확정 (reserved → 차감)
  3. 결제 실패 시 예약 해제 (reserved → available)

### 3. 도메인 중심 설계 (Rich Domain Model)

전통적인 계층형 아키텍처를 사용하되, **DDD의 핵심 원칙인 Rich Domain Model**을 적용했습니다.

**현재 프로젝트 구조**:

```
product-service/
├── controller/    # REST API, Request/Response 처리
├── service/       # Use Case, 비즈니스 흐름 조정
├── entity/        # 도메인 모델 (비즈니스 로직 포함)
├── repository/    # 데이터 접근 (Interface + Implementation)
├── dto/           # 데이터 전송 객체
└── config/        # 설정
```

**Rich Domain Model 적용**:

도메인 로직을 서비스가 아닌 **엔티티에 캡슐화**하여, 객체지향 설계의 핵심인 "데이터와 행위의 결합"을 실현했습니다.

```kotlin
@Entity
class Product {
    // 비즈니스 규칙을 도메인 모델에 캡슐화
    fun updatePrice(newOriginalPrice: BigDecimal, newSalePrice: BigDecimal) {
        require(newOriginalPrice >= BigDecimal.ZERO) { "원가는 0 이상이어야 합니다" }
        require(newSalePrice >= BigDecimal.ZERO) { "판매가는 0 이상이어야 합니다" }
        require(newSalePrice <= newOriginalPrice) { "판매가는 원가보다 클 수 없습니다" }

        this.originalPrice = newOriginalPrice
        this.salePrice = newSalePrice
    }

    // 도메인 개념을 명확히 표현
    fun getDiscountRate(): BigDecimal {
        if (originalPrice == BigDecimal.ZERO) return BigDecimal.ZERO
        return (originalPrice - salePrice)
            .multiply(BigDecimal(100))
            .divide(originalPrice, 2, RoundingMode.HALF_UP)
    }

    fun isOnSale(): Boolean = salePrice < originalPrice
}
```

**DDD 원칙 적용 사항**:
-  **Rich Domain Model**: 엔티티에 비즈니스 로직 캡슐화
-  **Aggregate Root**: Product가 ProductImage 컬렉션 관리
-  **Value Object**: ProductStatus Enum, BaseEntity (Auditing)
-  **Repository Pattern**: 데이터 접근 추상화

**vs Anemic Domain Model (안티패턴)**:
```kotlin
// 빈약한 도메인 모델 (안티패턴)
@Entity
class Product {
    var originalPrice: BigDecimal  // getter/setter만 존재
    var salePrice: BigDecimal
}

// 비즈니스 로직이 서비스에 노출
class ProductService {
    fun updatePrice(product: Product, newOriginalPrice: BigDecimal, newSalePrice: BigDecimal) {
        if (newSalePrice > newOriginalPrice) throw Exception()  // 로직 노출
        product.originalPrice = newOriginalPrice
        product.salePrice = newSalePrice
    }
}
```

### 4. 전역 ID 생성 전략 (TSID)

분산 환경에서 ID 충돌을 방지하기 위해 **TSID (Time-Sorted Unique Identifier)** 사용:

- **장점**:
  - 시간 기반 정렬 가능 (최신 데이터 조회 시 인덱스 효율적)
  - DB Auto Increment 대비 병목 현상 없음
  - 64-bit Long 타입 (BigInt 대비 공간 효율적)
- **사용 예**: Product ID, Order ID, Payment ID

### 5. API 설계 Best Practices

**RESTful 엔드포인트**:

```http
# 상품 배치 조회 (N+1 문제 방지)
GET /api/v1/products?ids=1,2,3
# → Repository: findByIdsWithDetails() - Querydsl fetch join

# 장바구니 아이템 부분 업데이트 (PATCH)
PATCH /api/v1/carts/me/items/{itemId}
Content-Type: application/json
{ "quantity": 5 }

# 주문 취소 (POST - 상태 변경이므로 POST 사용)
POST /api/v1/orders/{orderId}/cancel
```

**인증/인가**:

- JWT Access Token (1h) + Refresh Token (7d)
- API Gateway에서 JWT 검증 → 서비스로 userId 전파
- Google OAuth2 소셜 로그인 지원

---

## Tech Stack

### Backend

- **Language**: Kotlin 1.9+ (함수형 프로그래밍 활용)
- **Framework**: Spring Boot 3.2, Spring Cloud (Gateway, Eureka)
- **Persistence**: Spring Data JPA, Querydsl, Flyway
- **Messaging**: Apache Kafka, Protocol Buffers
- **Auth**: Spring Security, JWT, Google OAuth2

### Infrastructure

- **Database**: PostgreSQL 15 (각 서비스별 독립 DB)
- **Cache**: Redis (분산 락, 세션 저장)
- **Container**: Docker, Docker Compose, Jib
- **Monitoring**: Prometheus, Grafana
- **Distributed Tracing**: Spring Cloud Sleuth (준비 중)

### DevOps & Quality

- **CI/CD**: GitHub Actions (4개 워크플로우)
  - PR 시 Unit Test (Kotest, Mockk)
  - PR 시 Integration Test (Testcontainers)
  - SonarCloud 정적 분석
  - AI Code Review
- **Test Coverage**: Jacoco (비즈니스 로직 80%+ 목표)
- **Code Quality**: SonarCloud (Maintainability Rating A)

---

## Getting Started

### Prerequisites

```bash
# Required
- Java 21 (Temurin recommended)
- Docker & Docker Compose
- Gradle 8.5+

# Optional (for local development)
- IntelliJ IDEA
- PostgreSQL Client
```

### 실행 방법

**1. 환경 변수 설정**

```bash
cp .env.example .env
# .env 파일에서 다음 값 설정:
# - JWT_SECRET (HS256용 시크릿 키)
# - GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET (OAuth2)
# - STRIPE_API_KEY (결제 연동 시)
```

**2. Docker Compose로 인프라 실행**

```bash
# PostgreSQL, Kafka, Prometheus, Grafana 실행
docker-compose up -d postgres kafka zookeeper prometheus grafana

# 헬스체크 확인
docker-compose ps
```

**3. 마이크로서비스 빌드 및 실행**

```bash
# Option A: Docker로 모든 서비스 실행
./gradlew clean build
./gradlew jibDockerBuild  # Docker 이미지 빌드
docker-compose up -d

# Option B: 로컬에서 개별 실행 (개발 시)
./gradlew :eureka-server:bootRun
./gradlew :api-gateway:bootRun
./gradlew :product-service:bootRun
# ... (다른 서비스들도 동일)
```

**4. 접속 확인**

```bash
# Eureka Dashboard
open http://localhost:8761

# API Gateway
curl http://localhost:8080/api/v1/products

# Grafana (admin/admin123)
open http://localhost:3000
```

### 테스트 실행

```bash
# 전체 테스트 (Unit + Integration)
./gradlew test integrationTest

# 커버리지 리포트 생성
./gradlew jacocoTestReport
open product-service/build/reports/jacoco/test/html/index.html

# 특정 서비스만 테스트
./gradlew :product-service:test
```

---

## Performance & Scalability

### 부하 테스트 결과 (예정)

> **[성능 테스트 그래프 - Figma로 생성 필요]**
> - JMeter 또는 k6로 측정
> - TPS, Response Time, Error Rate

**목표 지표**:

- **처리량**: 1,000 TPS (주문 생성)
- **응답 시간**: P95 < 200ms (상품 조회)
- **동시 접속**: 10,000 concurrent users

### 확장성 전략

1. **수평 확장 (Horizontal Scaling)**
   - 각 마이크로서비스는 stateless → 인스턴스 추가만으로 확장 가능
   - Kafka Consumer Group으로 메시지 처리 병렬화

2. **캐싱 전략**
   - Redis: 상품 상세 정보 캐싱 (TTL 10분)
   - HTTP Cache-Control: 정적 리소스 브라우저 캐싱

3. **데이터베이스 최적화**
   - Querydsl Fetch Join으로 N+1 문제 해결
   - 인덱스: product_id, category_id, user_id
   - Connection Pool: HikariCP (max 20 connections)

---

## Project Structure

```
ECO_MSA_SERVER/
├── api-gateway/           # Spring Cloud Gateway (라우팅, JWT 검증)
├── eureka-server/         # Service Discovery
├── product-service/       # 상품 관리 (DDD 계층 구조)
│   ├── domain/           # Product, Category (Entities)
│   ├── application/      # ProductService (Use Cases)
│   ├── infrastructure/   # JPA, Querydsl 구현체
│   └── presentation/     # REST Controllers, DTOs
├── inventory-service/    # 재고 관리 (낙관적 락, 이벤트 소싱)
├── order-service/        # 주문 관리 (Kafka Producer)
├── payment-service/      # 결제 처리 (Stripe API, Kafka Consumer)
├── cart-service/         # 장바구니 (JWT 인증)
├── user-service/         # 사용자 관리 (OAuth2)
├── proto-schema/         # Protobuf 이벤트 스키마
│   ├── order/           # OrderCreatedEvent, OrderCancelledEvent
│   ├── payment/         # PaymentCompletedEvent, PaymentFailedEvent
│   └── inventory/       # InventoryReservedEvent
├── docker/              # Docker 설정, Init Scripts
├── .github/workflows/   # CI/CD 파이프라인
└── docker-compose.yml   # 전체 인프라 오케스트레이션
```

---

## Testing Strategy

### 테스트 작성 원칙

1. **Given-When-Then 패턴** (Kotest BehaviorSpec)
2. **Mockk을 활용한 격리 테스트** (외부 의존성 모킹)
3. **Repository는 실제 DB로 테스트** (Testcontainers PostgreSQL)
4. **Integration Test는 별도 소스셋** (`src/integrationTest/`)

**예시**:

```kotlin
@Test
fun `상품 수정 시 존재하지 않는 상품이면 예외 발생`() {
    // Given
    val invalidProductId = "invalid-id"

    // When & Then
    assertThrows<IllegalArgumentException> {
        productService.updateProduct(invalidProductId, updateRequest)
    }
}
```

---

##  Security Considerations

- **API 인증**: JWT Bearer Token (HS256)
- **비밀번호 암호화**: BCrypt (Strength 10)
- **결제 정보**: Stripe 토큰화 (카드 정보 직접 저장 X)
- **HTTPS 통신**: 프로덕션 환경 필수 (Let's Encrypt)
- **Rate Limiting**: API Gateway에서 IP별 요청 제한 (준비 중)
- **SQL Injection 방지**: Querydsl Parameterized Query

---

##  Monitoring & Observability

### Prometheus Metrics

- **System Metrics**: CPU, Memory, Disk I/O
- **JVM Metrics**: Heap, GC, Thread Count
- **Application Metrics**:
  - HTTP Request Count/Duration (by endpoint)
  - Kafka Message Lag
  - DB Connection Pool Status

### Grafana Dashboards

> **[Grafana 대시보드 스크린샷 - Figma로 생성 필요]**
> - JVM Dashboard (Heap, GC)
> - API Response Time by Service
> - Kafka Lag Monitoring

**접속**: `http://localhost:3000` (admin/admin123)

---

##  Technical Decisions

### Why Kafka over REST for Inter-Service Communication?

**선택**: Kafka 이벤트 기반 통신
**이유**:
-  **느슨한 결합**: 서비스 간 직접 의존성 제거
-  **장애 격리**: Payment Service 장애 시 Order Service 영향 없음
-  **재시도 메커니즘**: Consumer가 실패해도 Kafka에 메시지 보존
-  **확장성**: Consumer Group으로 메시지 처리 병렬화

**트레이드오프**:
-  **복잡도 증가**: 이벤트 추적, 디버깅 어려움 (Sleuth로 해결 예정)
-  **즉시성 부족**: 비동기 처리로 인한 레이턴시 (수백 ms)

### Why Protobuf over JSON for Event Schema?

**선택**: Protocol Buffers
**이유**:
- **타입 안정성**: 컴파일 타임에 스키마 검증
- **하위 호환성**: 필드 추가 시 기존 Consumer 영향 없음
- **직렬화 성능**: JSON 대비 3-10배 빠름, 크기 1/3

### Why Database per Service Pattern?

**선택**: 각 서비스별 독립 PostgreSQL DB
**이유**:
-  **서비스 독립성**: 스키마 변경 시 다른 서비스 영향 없음
-  **기술 다양성**: 서비스별 최적 DB 선택 가능 (향후 MongoDB 도입 가능)
-  **장애 격리**: 한 DB 장애 시 다른 서비스 정상 동작

---

##  Lessons Learned

### 1. 이벤트 순서 보장의 중요성

**문제**: `PaymentCompletedEvent`가 `InventoryReservedEvent`보다 먼저 도착하는 경우 발생
**해결**: Kafka Partition Key를 `orderId`로 설정 → 동일 주문 이벤트는 순서 보장

### 2. 트랜잭션 이력 추적의 중요성

**문제**: 결제 상태 변경 과정을 추적할 방법이 없어 디버깅과 감사(Audit)가 어려움
**해결**: `PaymentTransaction` 테이블로 모든 트랜잭션 이력 기록 (승인, 취소, 환불 등)
- 각 상태 변경마다 별도 레코드 생성
- PG사 응답 코드 및 메시지 보관
- 문제 발생 시 전체 흐름 추적 가능

### 3. N+1 쿼리 문제

**문제**: 상품 목록 조회 시 각 상품의 이미지를 개별 쿼리로 조회 (100개 상품 = 101개 쿼리)
**해결**: Querydsl로 `fetchJoin()` 사용 → 1개 쿼리로 최적화

---

##  API Documentation

### 주요 엔드포인트

**Product Service**

```http
# 상품 등록
POST /api/v1/products
Content-Type: application/json

{
  "name": "MacBook Pro M3",
  "categoryId": 1,
  "originalPrice": 2500000,
  "salePrice": 2300000,
  "images": [
    { "imageUrl": "https://...", "displayOrder": 1, "isThumbnail": true }
  ]
}

# 상품 검색 (Querydsl 동적 쿼리)
POST /api/v1/products/search
{
  "categoryId": 1,
  "keyword": "MacBook",
  "minPrice": 1000000,
  "maxPrice": 3000000,
  "status": "ACTIVE"
}
```

**Order Service**

```http
# 주문 생성 (JWT 필수)
POST /api/v1/orders
Authorization: Bearer <JWT_TOKEN>
{
  "items": [
    { "productId": "abc123", "quantity": 2 }
  ],
  "shippingAddress": "서울시 강남구",
  "shippingName": "홍길동",
  "shippingPhone": "010-1234-5678"
}

# 주문 취소 (Compensation 트리거)
POST /api/v1/orders/{orderId}/cancel
Authorization: Bearer <JWT_TOKEN>
```

---

##  License

이 프로젝트는 학습 및 포트폴리오 목적으로 공개되었습니다.

MIT License

---

<div align="center">

** If you find this project helpful, please give it a star!**

Made with  by kitoha

</div>
