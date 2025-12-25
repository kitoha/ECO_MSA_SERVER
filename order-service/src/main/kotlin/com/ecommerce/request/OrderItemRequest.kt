package com.ecommerce.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

/**
 * 주문 항목 요청 DTO
 */
data class OrderItemRequest(
    @field:NotBlank(message = "상품 ID는 필수입니다")
    val productId: String,

    @field:Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    val quantity: Int
)