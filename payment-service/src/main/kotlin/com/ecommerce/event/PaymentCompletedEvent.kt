package com.ecommerce.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.math.BigDecimal
import java.time.LocalDateTime

@JsonTypeName("paymentCompleted")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
data class PaymentCompletedEvent(
  val paymentId: Long,
  val orderId: String,
  val userId: String,
  val amount: BigDecimal,
  val pgProvider: String,
  val pgPaymentKey: String,
  val timestamp: LocalDateTime = LocalDateTime.now()
)
