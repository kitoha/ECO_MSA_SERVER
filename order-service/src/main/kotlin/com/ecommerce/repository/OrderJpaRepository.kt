package com.ecommerce.repository

import com.ecommerce.entity.Order
import com.ecommerce.enums.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface OrderJpaRepository : JpaRepository<Order, Long> {

    fun findByOrderNumber(orderNumber: String): Order?

    fun findByUserId(userId: String): List<Order>

    fun findByUserIdAndStatus(userId: String, status: OrderStatus): List<Order>

    fun findByStatus(status: OrderStatus): List<Order>

    @Query("SELECT o FROM Order o WHERE o.orderedAt BETWEEN :startDate AND :endDate")
    fun findByOrderedAtBetween(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Order>

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.orderedAt BETWEEN :startDate AND :endDate")
    fun findByUserIdAndOrderedAtBetween(
        @Param("userId") userId: String,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Order>
}