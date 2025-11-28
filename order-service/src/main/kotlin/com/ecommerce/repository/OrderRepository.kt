package com.ecommerce.repository

import com.ecommerce.entity.Order
import com.ecommerce.enums.OrderStatus
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class OrderRepository(
    private val orderJpaRepository: OrderJpaRepository
) {

    fun save(order: Order): Order {
        return orderJpaRepository.save(order)
    }

    fun findById(id: Long): Order? {
        return orderJpaRepository.findById(id).orElse(null)
    }

    fun findByOrderNumber(orderNumber: String): Order? {
        return orderJpaRepository.findByOrderNumber(orderNumber)
    }

    fun findByUserId(userId: String): List<Order> {
        return orderJpaRepository.findByUserId(userId)
    }

    fun findByUserIdAndStatus(userId: String, status: OrderStatus): List<Order> {
        return orderJpaRepository.findByUserIdAndStatus(userId, status)
    }

    fun findByStatus(status: OrderStatus): List<Order> {
        return orderJpaRepository.findByStatus(status)
    }

    fun findByOrderedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Order> {
        return orderJpaRepository.findByOrderedAtBetween(startDate, endDate)
    }

    fun findByUserIdAndOrderedAtBetween(
        userId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<Order> {
        return orderJpaRepository.findByUserIdAndOrderedAtBetween(userId, startDate, endDate)
    }

    fun findAll(): List<Order> {
        return orderJpaRepository.findAll()
    }

    fun delete(order: Order) {
        orderJpaRepository.delete(order)
    }
}