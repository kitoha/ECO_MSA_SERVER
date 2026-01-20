package com.ecommerce.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.LocalDateTime

/**
 * 예약 생성 이벤트 (Inventory Service에서 발행)
 */
@JsonTypeName("reservationCreated")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
data class ReservationCreatedEvent(
    val reservationId: Long,
    val orderId: String,
    val productId: String,
    val quantity: Int,
    val expiresAt: LocalDateTime,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
