package com.ecommerce.inventory.request

import jakarta.validation.constraints.NotNull

/**
 * 재고 예약 해제 요청
 */
data class ReleaseInventoryRequest(
  @field:NotNull(message = "Reservation ID is required")
  val reservationId: Long
)
