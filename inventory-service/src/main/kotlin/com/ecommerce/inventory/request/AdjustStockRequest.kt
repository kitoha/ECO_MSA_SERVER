package com.ecommerce.inventory.request

import com.ecommerce.inventory.enums.InventoryChangeType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * 재고 조정 요청 (입고/출고)
 */
data class AdjustStockRequest(
  @field:NotNull(message = "Quantity is required")
  @field:Min(value = 1, message = "Quantity must be at least 1")
  val quantity: Int,

  @field:NotNull(message = "Change type is required")
  val changeType: InventoryChangeType,

  val reason: String? = null,

  val referenceId: String? = null
)
