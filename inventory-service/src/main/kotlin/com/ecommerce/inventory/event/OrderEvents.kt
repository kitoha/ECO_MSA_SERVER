package com.ecommerce.inventory.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.LocalDateTime

/**
 * 재고 예약 요청 이벤트
 */
@JsonTypeName("inventoryReservationRequest")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
data class InventoryReservationRequest(
    val orderId: String,
    val productId: String,
    val quantity: Int,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * 주문 취소 이벤트
 */
@JsonTypeName("orderCancelled")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
data class OrderCancelledEvent(
    val orderId: Long,
    val orderNumber: String,
    val userId: String,
    val reason: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * 주문 확정 이벤트
 */
@JsonTypeName("orderConfirmed")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
data class OrderConfirmedEvent(
    val orderId: Long,
    val orderNumber: String,
    val userId: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
