package com.ecommerce.response

import com.ecommerce.entity.Order
import com.ecommerce.enums.OrderStatus
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 주문 목록 응답 DTO (간략 정보)
 */
data class OrderSummaryResponse(
  val id: Long,
  val orderNumber: String,
  val userId: String,
  val status: OrderStatus,
  val totalAmount: BigDecimal,
  val itemCount: Int,
  val orderedAt: LocalDateTime
) {
  companion object {
    fun from(order: Order): OrderSummaryResponse {
      return OrderSummaryResponse(
        id = order.id,
        orderNumber = order.orderNumber,
        userId = order.userId,
        status = order.status,
        totalAmount = order.totalAmount,
        itemCount = order.items.size,
        orderedAt = order.orderedAt
      )
    }
  }
}
