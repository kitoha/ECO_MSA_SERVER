package com.ecommerce.response
import com.ecommerce.dto.OrderItemDto
import com.ecommerce.entity.OrderItem
import java.math.BigDecimal

/**
 * 주문 항목 응답 DTO
 */
data class OrderItemResponse(
    val id: Long,
    val productId: String,
    val productName: String,
    val price: BigDecimal,
    val quantity: Int,
    val subtotal: BigDecimal
) {
    companion object {
        fun from(item: OrderItemDto): OrderItemResponse {
            return OrderItemResponse(
                id = item.id,
                productId = item.productId,
                productName = item.productName,
                price = item.price,
                quantity = item.quantity,
                subtotal = item.subtotal
            )
        }
    }
}
