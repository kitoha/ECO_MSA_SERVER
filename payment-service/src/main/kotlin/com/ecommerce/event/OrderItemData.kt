package com.ecommerce.event

import java.math.BigDecimal

data class OrderItemData(
  val productId: String,
  val productName: String,
  val price: BigDecimal,
  val quantity: Int
)
