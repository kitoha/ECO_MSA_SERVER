package com.ecommerce.request

import com.ecommerce.enums.PaymentMethod
import java.math.BigDecimal

data class PaymentWebhookRequest(
  val eventType: String,
  val orderId: String,
  val pgPaymentKey: String,
  val pgTransactionId: String,
  val amount: BigDecimal,
  val paymentMethod: PaymentMethod,
  val pgProvider: String,
  val status: String,
  val responseCode: String,
  val responseMessage: String,
  val timestamp: String
)
