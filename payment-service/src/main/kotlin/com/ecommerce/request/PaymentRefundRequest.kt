package com.ecommerce.request

import jakarta.validation.constraints.NotBlank

data class PaymentRefundRequest(
  @field:NotBlank(message = "환불 사유는 필수입니다")
  val reason: String
)
