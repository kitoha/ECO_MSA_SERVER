package com.ecommerce.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

/**
 * 주문 생성 요청 DTO
 */
data class CreateOrderRequest(
  @field:NotBlank(message = "사용자 ID는 필수입니다")
  val userId: String,

  @field:NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다")
  @field:Valid
  val items: List<OrderItemRequest>,

  @field:NotBlank(message = "배송 주소는 필수입니다")
  val shippingAddress: String,

  @field:NotBlank(message = "수령인 이름은 필수입니다")
  @field:Size(max = 100, message = "수령인 이름은 100자 이하여야 합니다")
  val shippingName: String,

  @field:NotBlank(message = "연락처는 필수입니다")
  @field:Size(max = 20, message = "연락처는 20자 이하여야 합니다")
  val shippingPhone: String
)
