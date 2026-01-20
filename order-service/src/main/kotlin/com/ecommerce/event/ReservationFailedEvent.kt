package com.ecommerce.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.LocalDateTime

/**
 * 예약 실패 이벤트 (Inventory Service에서 발행)
 */
@JsonTypeName("reservationFailed")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
data class ReservationFailedEvent(
    val orderId: String,
    val productId: String,
    val quantity: Int,
    val reason: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
