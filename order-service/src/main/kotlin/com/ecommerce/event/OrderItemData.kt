package com.ecommerce.event

import java.math.BigDecimal

/**
 * 주문 항목 데이터 (이벤트용)
 */
data class OrderItemData(
  val productId: String,
  val productName: String,
  val price: BigDecimal,
  val quantity: Int
)