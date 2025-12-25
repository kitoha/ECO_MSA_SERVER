package com.ecommerce.product.generator

/**
 * ID 생성 전략 인터페이스
 *
 * 다양한 ID 생성 전략을 구현할 수 있도록 추상화
 * - TSID: 시간 정렬 가능한 고유 ID
 * - UUID: 범용 고유 식별자
 * - Snowflake: 분산 환경용 ID
 *
 * **장점:**
 * - 테스트 용이: Mock으로 교체 가능
 * - 전략 패턴: 쉽게 다른 전략으로 교체
 * - DI 친화적: Spring Bean으로 관리
 */
interface IdGenerator {
    /**
     * 고유한 ID 생성
     *
     * @return 생성된 고유 ID
     */
    fun generate(): Long
}
