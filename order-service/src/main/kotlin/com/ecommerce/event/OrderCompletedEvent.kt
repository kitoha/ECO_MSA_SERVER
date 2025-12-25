package com.ecommerce.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.LocalDateTime

/**
 * 주문 완료 이벤트 (배송 완료)
 */
@JsonTypeName("orderCompleted")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
data class OrderCompletedEvent(
  val orderId: Long,
  val orderNumber: String,
  val userId: String,
  val timestamp: LocalDateTime = LocalDateTime.now()
)