package com.ecommerce.dto

import java.time.LocalDateTime

data class ReservationCreatedEventDto(
    val reservationId: Long,
    val orderId: String,
    val productId: String,
    val quantity: Int,
    val expiresAt: LocalDateTime,
    val timestamp: LocalDateTime
)
