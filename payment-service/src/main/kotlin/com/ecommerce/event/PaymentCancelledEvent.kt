package com.ecommerce.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.math.BigDecimal
import java.time.LocalDateTime

@JsonTypeName("paymentCancelled")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
data class PaymentCancelledEvent(
  val paymentId: Long,
  val orderId: String,
  val userId: String,
  val amount: BigDecimal,
  val reason: String,
  val timestamp: LocalDateTime = LocalDateTime.now()
)
