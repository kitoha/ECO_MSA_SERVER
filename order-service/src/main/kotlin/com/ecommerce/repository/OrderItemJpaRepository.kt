package com.ecommerce.repository

import com.ecommerce.entity.Order
import com.ecommerce.entity.OrderItem
import org.springframework.data.jpa.repository.JpaRepository

interface OrderItemJpaRepository : JpaRepository<OrderItem, Long> {

    fun findByOrder(order: Order): List<OrderItem>

    /**
     * 주문 ID로 주문 항목 조회
     */
    fun findByOrderId(orderId: Long): List<OrderItem>
}