package com.ecommerce.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 주문 생성 이벤트
 */
@JsonTypeName("orderCreated")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
data class OrderCreatedEvent(
    val orderId: Long,
    val orderNumber: String,
    val userId: String,
    val items: List<OrderItemData>,
    val totalAmount: BigDecimal,
    val shippingAddress: String,
    val shippingName: String,
    val shippingPhone: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
