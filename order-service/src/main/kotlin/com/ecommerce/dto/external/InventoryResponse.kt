package com.ecommerce.dto.external

data class InventoryResponse(
    val id: Long,
    val productId: String,
    val availableQuantity: Int,
    val reservedQuantity: Int,
    val totalQuantity: Int
)
