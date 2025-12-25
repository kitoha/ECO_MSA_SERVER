package com.ecommerce.product.generator

import io.hypersistence.tsid.TSID
import org.springframework.stereotype.Component

/**
 * TSID (Time-Sorted Unique Identifier) 기반 ID 생성기
 *
 * **특징:**
 * - Long 타입 (8 bytes, 성능 우수)
 * - 시간순 정렬 가능 (createdAt 인덱스 불필요)
 * - 추측 불가능 (랜덤 요소 포함)
 * - 분산 환경에서 충돌 없음
 *
 * **생성 예시:**
 * - 236372517419679744 (Long)
 * - 0C6JNH3N3B8G0 (String 표현)
 *
 * **사용 사례:**
 * - Instagram, Discord, Notion
 *
 * **내부 구조 (64-bit):**
 * - 42 bits: timestamp (milliseconds)
 * - 22 bits: random
 */
@Component
class TsidGenerator : IdGenerator {

    /**
     * TSID 생성
     *
     * Thread-safe하며, 동일 밀리초 내에서도 고유성 보장
     *
     * @return Long 타입의 TSID
     */
    override fun generate(): Long {
        return TSID.fast().toLong()
    }

    /**
     * Long ID를 TSID String으로 인코딩
     *
     * @param id Long 타입의 ID
     * @return TSID String 표현 (예: "0C6JNH3N3B8G0")
     */
    fun encode(id: Long): String {
        return TSID.from(id).toString()
    }

    /**
     * TSID String을 Long ID로 디코딩
     *
     * @param tsid TSID String 표현
     * @return Long 타입의 ID
     */
    fun decode(tsid: String): Long {
        return TSID.from(tsid).toLong()
    }
}
