package com.ecommerce.response

import com.ecommerce.dto.OrderItemDto
import com.ecommerce.entity.Order
import com.ecommerce.enums.OrderStatus
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 주문 응답 DTO
 */
data class OrderResponse(
  val id: Long,
  val orderNumber: String,
  val userId: String,
  val status: OrderStatus,
  val totalAmount: BigDecimal,
  val shippingAddress: String,
  val shippingName: String,
  val shippingPhone: String,
  val items: List<OrderItemResponse>,
  val orderedAt: LocalDateTime,
  val createdAt: LocalDateTime?,
  val updatedAt: LocalDateTime?
) {
  companion object {
    fun from(order: Order): OrderResponse {
      val orderItemDtos = order.items.map { OrderItemDto.from(it) }
      return OrderResponse(
        id = order.id,
        orderNumber = order.orderNumber,
        userId = order.userId,
        status = order.status,
        totalAmount = order.totalAmount,
        shippingAddress = order.shippingAddress,
        shippingName = order.shippingName,
        shippingPhone = order.shippingPhone,
        items = orderItemDtos.map { OrderItemResponse.from(it) },
        orderedAt = order.orderedAt,
        createdAt = order.createdAt,
        updatedAt = order.updatedAt
      )
    }
  }
}