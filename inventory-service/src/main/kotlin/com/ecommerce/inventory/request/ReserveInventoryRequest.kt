package com.ecommerce.inventory.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * 재고 예약 요청
 */
data class ReserveInventoryRequest(
  @field:NotBlank(message = "Order ID is required")
  val orderId: String,

  @field:NotBlank(message = "Product ID is required")
  val productId: String,

  @field:NotNull(message = "Quantity is required")
  @field:Min(value = 1, message = "Quantity must be at least 1")
  val quantity: Int
)
