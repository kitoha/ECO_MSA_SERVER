package com.ecommerce.inventory.response

import com.ecommerce.inventory.enums.ReservationStatus
import java.time.LocalDateTime

/**
 * 재고 예약 응답
 */
data class ReservationResponse(
  val id: Long,
  val inventoryId: Long,
  val orderId: String,
  val quantity: Int,
  val status: ReservationStatus,
  val expiresAt: LocalDateTime,
  val createdAt: LocalDateTime?
)
