package com.ecommerce.cart.dto

import com.ecommerce.cart.entity.CartItem
import com.ecommerce.cart.generator.TsidGenerator
import java.math.BigDecimal
import java.time.LocalDateTime

data class CartItemResponse(
    val id: String,
    val productId: String,
    val productName: String,
    val price: BigDecimal,
    val quantity: Int,
    val subtotal: BigDecimal,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun from(item: CartItem): CartItemResponse {
            return CartItemResponse(
                id = TsidGenerator.encode(item.id),
                productId = TsidGenerator.encode(item.productId),
                productName = item.productName,
                price = item.price,
                quantity = item.quantity,
                subtotal = item.getSubtotal(),
                createdAt = item.createdAt,
                updatedAt = item.updatedAt
            )
        }
    }
}
