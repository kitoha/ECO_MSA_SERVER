package com.ecommerce.request

import com.ecommerce.enums.PaymentMethod
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class CreatePaymentRequest(
  @field:NotBlank(message = "주문 ID는 필수입니다")
  val orderId: String,

  @field:NotBlank(message = "사용자 ID는 필수입니다")
  val userId: String,

  @field:NotNull(message = "결제 금액은 필수입니다")
  @field:Positive(message = "결제 금액은 0보다 커야 합니다")
  val amount: BigDecimal,

  @field:NotNull(message = "결제 수단은 필수입니다")
  val paymentMethod: PaymentMethod
)
