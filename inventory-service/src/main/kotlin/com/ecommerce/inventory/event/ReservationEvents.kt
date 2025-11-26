package com.ecommerce.inventory.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.LocalDateTime

/**
 * 예약 생성 이벤트
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

/**
 * 예약 취소 이벤트
 */
@JsonTypeName("reservationCancelled")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
data class ReservationCancelledEvent(
    val reservationId: Long,
    val reason: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * 예약 확정 이벤트
 */
@JsonTypeName("reservationConfirmed")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
data class ReservationConfirmedEvent(
    val reservationId: Long,
    val orderId: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
