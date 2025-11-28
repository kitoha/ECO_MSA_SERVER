package com.ecommerce.service

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
    private val orderRepository: OrderRepository
) {

    fun addOrderItem(orderId: Long, item: OrderItemRequest) {
        val order = orderRepository.findById(orderId)
            ?: throw IllegalArgumentException("존재하지 않는 주문입니다: $orderId")

        //val productInfo = productClient.getProductInfo(item.productId) - api gateway 통해서 호출

        val orderItem = OrderItem(
            productId = item.productId.toString(),
            productName = "상품명 (조회 필요)", // TODO: Product-service에서 조회
            price = BigDecimal(1),// TODO: Product-service에서 조회 (ProductInfo.price)
            quantity = item.quantity.toInt()
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