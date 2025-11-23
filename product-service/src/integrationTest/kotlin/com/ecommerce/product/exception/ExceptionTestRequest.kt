package com.ecommerce.product.exception

import jakarta.validation.constraints.NotBlank

data class ExceptionTestRequest(
  @field:NotBlank(message = "이름은 필수입니다")
  val name: String
)
