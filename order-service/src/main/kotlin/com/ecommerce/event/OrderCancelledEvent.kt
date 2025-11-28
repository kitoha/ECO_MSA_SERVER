package com.ecommerce.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.LocalDateTime

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
