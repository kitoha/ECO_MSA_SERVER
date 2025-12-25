package com.ecommerce.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.LocalDateTime

/**
 * 재고 예약 요청 이벤트
 * order-service -> inventory-service
 */
@JsonTypeName("inventoryReservationRequest")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
data class InventoryReservationRequest(
    val orderId: String,
    val productId: String,
    val quantity: Int,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
