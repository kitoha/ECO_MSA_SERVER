package com.ecommerce.service

import com.ecommerce.client.ProductClient
import com.ecommerce.dto.OrderItemDto
import com.ecommerce.entity.OrderItem
import com.ecommerce.repository.OrderItemRepository
import com.ecommerce.repository.OrderRepository
import com.ecommerce.request.OrderItemRequest
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * 주문 항목 서비스
 */
@Service
class OrderItemService(
    private val orderItemRepository: OrderItemRepository,
    private val orderRepository: OrderRepository,
    private val productClient: ProductClient
) {

    fun addOrderItem(orderId: Long, item: OrderItemRequest) {
        val order = orderRepository.findById(orderId)
            ?: throw IllegalArgumentException("존재하지 않는 주문입니다: $orderId")

        val productInfo = productClient.getProductById(item.productId)
            ?: throw IllegalArgumentException("존재하지 않는 상품입니다: ${item.productId}")

        val orderItem = OrderItem(
            productId = item.productId,
            productName = productInfo.name,
            price = productInfo.salePrice,
            quantity = item.quantity
        )

        order.addItem(orderItem)
        orderItemRepository.save(orderItem)
    }

    fun getOrderItems(orderId: Long): List<OrderItemDto> {
        val orderItems = orderItemRepository.findByOrderId(orderId)
        return orderItems.map { OrderItemDto.from(it) }
    }

    fun calculateOrderTotal(items: List<OrderItemRequest>): BigDecimal {
        return items.fold(BigDecimal.ZERO) { acc, item ->
            //TODO Product-service에서 가격 조회
           // acc + (item.price.multiply(item.quantity))
            return BigDecimal.ZERO // - 임시 반환
        }
    }
}