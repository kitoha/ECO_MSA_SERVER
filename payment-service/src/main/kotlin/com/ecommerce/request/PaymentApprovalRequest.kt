package com.ecommerce.request

import jakarta.validation.constraints.NotBlank

data class PaymentApprovalRequest(
  @field:NotBlank(message = "PG 결제 키는 필수입니다")
  val pgPaymentKey: String,

  @field:NotBlank(message = "PG 프로바이더는 필수입니다")
  val pgProvider: String,

  val pgTransactionId: String? = null
)
