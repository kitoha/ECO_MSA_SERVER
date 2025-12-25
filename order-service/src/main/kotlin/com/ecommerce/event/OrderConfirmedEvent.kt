package com.ecommerce.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.LocalDateTime

/**
 * 주문 확정 이벤트 (재고 예약 완료 후)
 */
@JsonTypeName("orderConfirmed")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
data class OrderConfirmedEvent(
  val orderId: Long,
  val orderNumber: String,
  val userId: String,
  val timestamp: LocalDateTime = LocalDateTime.now()
)