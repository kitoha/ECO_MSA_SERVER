package com.ecommerce.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentCompletedEventDto(
    val paymentId: Long,
    val orderId: String,
    val userId: String,
    val amount: BigDecimal,
    val pgProvider: String,
    val pgPaymentKey: String,
    val timestamp: LocalDateTime
)
