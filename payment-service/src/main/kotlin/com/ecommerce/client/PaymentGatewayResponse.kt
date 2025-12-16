package com.ecommerce.client

import java.math.BigDecimal

data class PaymentGatewayResponse(
  val success: Boolean,
  val transactionId: String?,
  val paymentKey: String?,
  val amount: BigDecimal?,
  val responseCode: String?,
  val responseMessage: String?,
  val errorCode: String? = null,
  val errorMessage: String? = null
)
