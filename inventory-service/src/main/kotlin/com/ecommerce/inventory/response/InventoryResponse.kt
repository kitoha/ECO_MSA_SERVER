package com.ecommerce.inventory.response

/**
 * 재고 조회 응답
 */
data class InventoryResponse(
  val id: Long,
  val productId: String,
  val availableQuantity: Int,
  val reservedQuantity: Int,
  val totalQuantity: Int
)
