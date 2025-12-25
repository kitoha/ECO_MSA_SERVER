package com.ecommerce.client

import com.ecommerce.enums.PaymentMethod
import java.math.BigDecimal

interface PaymentGateway {
  fun authorize(
    orderId: String,
    amount: BigDecimal,
    paymentMethod: PaymentMethod
  ): PaymentGatewayResponse

  fun capture(
    pgPaymentKey: String,
    amount: BigDecimal
  ): PaymentGatewayResponse

  fun cancel(
    pgPaymentKey: String,
    reason: String
  ): PaymentGatewayResponse

  fun refund(
    pgPaymentKey: String,
    amount: BigDecimal,
    reason: String
  ): PaymentGatewayResponse

  fun verifyWebhook(
    signature: String,
    payload: String
  ): Boolean
}
