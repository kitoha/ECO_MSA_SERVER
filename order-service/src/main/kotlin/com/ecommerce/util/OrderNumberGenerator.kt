package com.ecommerce.util

import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicLong

/**
 * 주문 번호 생성기
 * 형식: ORD-YYYYMMDD-NNNNNN
 * 예: ORD-20250128-000001
 */
@Component
class OrderNumberGenerator {

    private val counter = AtomicLong(0)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun generate(): String {
        val date = LocalDateTime.now().format(dateFormatter)
        val sequence = counter.incrementAndGet() % 1000000
        return String.format("ORD-%s-%06d", date, sequence)
    }
}