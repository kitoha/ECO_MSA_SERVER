package com.ecommerce.util

import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * 주문 번호 생성기
 * 형식: ORD-YYYYMMDD-RandomString
 * 예: ORD-20260124-A1B2C3
 */
@Component
class OrderNumberGenerator {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun generate(): String {
        val date = LocalDateTime.now().format(dateFormatter)
        val randomSuffix = UUID.randomUUID().toString().substring(0, 8).uppercase()
        return "ORD-$date-$randomSuffix"
    }
}