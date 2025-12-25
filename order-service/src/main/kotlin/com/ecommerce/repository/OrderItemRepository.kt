package com.ecommerce.repository

import com.ecommerce.entity.Order
import com.ecommerce.entity.OrderItem
import org.springframework.stereotype.Repository

@Repository
class OrderItemRepository(
    private val orderItemJpaRepository: OrderItemJpaRepository
) {

    fun save(orderItem: OrderItem): OrderItem {
        return orderItemJpaRepository.save(orderItem)
    }

    fun findById(id: Long): OrderItem? {
        return orderItemJpaRepository.findById(id).orElse(null)
    }

    fun findByOrder(order: Order): List<OrderItem> {
        return orderItemJpaRepository.findByOrder(order)
    }

    fun findByOrderId(orderId: Long): List<OrderItem> {
        return orderItemJpaRepository.findByOrderId(orderId)
    }
}