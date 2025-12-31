package com.ecommerce.cart.dto

import com.ecommerce.cart.entity.Cart
import com.ecommerce.cart.generator.TsidGenerator
import java.math.BigDecimal
import java.time.LocalDateTime

data class CartResponse(
    val id: String,
    val userId: Long,
    val items: List<CartItemResponse>,
    val totalPrice: BigDecimal,
    val totalItemCount: Int,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun from(cart: Cart): CartResponse {
            return CartResponse(
                id = TsidGenerator.encode(cart.id),
                userId = cart.userId,
                items = cart.getActiveItems().map { CartItemResponse.from(it) },
                totalPrice = cart.getTotalPrice(),
                totalItemCount = cart.getTotalItemCount(),
                createdAt = cart.createdAt,
                updatedAt = cart.updatedAt
            )
        }
    }
}