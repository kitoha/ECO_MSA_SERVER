package com.ecommerce.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderCreatedEventDto(
    val orderId: Long,
    val orderNumber: String,
    val userId: String,
    val items: List<OrderItemDto>,
    val totalAmount: BigDecimal,
    val shippingAddress: String,
    val shippingName: String,
    val shippingPhone: String,
    val timestamp: LocalDateTime
)

data class OrderItemDto(
    val productId: String,
    val productName: String,
    val price: BigDecimal,
    val quantity: Int
)
