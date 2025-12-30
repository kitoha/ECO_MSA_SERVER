package com.ecommerce.cart.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class AddItemRequest(
    @field:NotNull(message = "상품 ID는 필수입니다")
    val productId: Long,

    @field:NotNull(message = "수량은 필수입니다")
    @field:Min(value = 1, message = "수량은 1 이상이어야 합니다")
    val quantity: Int
)
