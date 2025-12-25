package com.ecommerce.dto

import com.ecommerce.entity.OrderItem
import java.math.BigDecimal

data class OrderItemDto(
  val id: Long,
  val orderId: Long,
  val productId: String,
  val productName: String,
  val price: BigDecimal,
  val quantity: Int,
  val subtotal: BigDecimal
) {

  companion object {
    fun from(orderItemEntity: OrderItem): OrderItemDto {
      return OrderItemDto(
        id = orderItemEntity.id,
        orderId = orderItemEntity.order.id,
        productId = orderItemEntity.productId,
        productName = orderItemEntity.productName,
        price = orderItemEntity.price,
        quantity = orderItemEntity.quantity,
        subtotal = orderItemEntity.subtotal
      )
    }
  }
}