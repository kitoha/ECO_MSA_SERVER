package com.ecommerce.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 결제 실패 이벤트
 */
@JsonTypeName("paymentFailed")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
data class PaymentFailedEvent(
    val paymentId: Long,
    val orderId: String,
    val userId: String,
    val amount: BigDecimal,
    val failureReason: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
