package com.ecommerce.event

import com.ecommerce.enums.PaymentMethod
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.math.BigDecimal
import java.time.LocalDateTime

@JsonTypeName("paymentCreated")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
data class PaymentCreatedEvent(
  val paymentId: Long,
  val orderId: String,
  val userId: String,
  val amount: BigDecimal,
  val paymentMethod: PaymentMethod?,
  val timestamp: LocalDateTime = LocalDateTime.now()
)
