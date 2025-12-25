package com.ecommerce.repository

import com.ecommerce.entity.Payment
import com.ecommerce.enums.PaymentStatus
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class PaymentRepository(
    private val jpaRepository: PaymentJpaRepository,
    private val queryRepository: PaymentQueryRepository
) {

    fun save(payment: Payment): Payment {
        return jpaRepository.save(payment)
    }

    fun findById(id: Long): Payment? {
        return jpaRepository.findById(id).orElse(null)
    }

    fun findByIdWithTransactions(id: Long): Payment? {
        return jpaRepository.findByIdWithTransactions(id)
    }

    fun findByOrderId(orderId: String): Payment? {
        return jpaRepository.findByOrderId(orderId)
    }

    fun findByOrderIdWithTransactions(orderId: String): Payment? {
        return jpaRepository.findByOrderIdWithTransactions(orderId)
    }

    fun findByPgPaymentKey(pgPaymentKey: String): Payment? {
        return jpaRepository.findByPgPaymentKey(pgPaymentKey)
    }

    fun findByUserId(userId: String): List<Payment> {
        return queryRepository.findByUserId(userId)
    }

    fun findByStatus(status: PaymentStatus): List<Payment> {
        return queryRepository.findByStatus(status)
    }

    fun findByUserIdAndStatus(userId: String, status: PaymentStatus): List<Payment> {
        return queryRepository.findByUserIdAndStatus(userId, status)
    }

    fun findByCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Payment> {
        return queryRepository.findByCreatedAtBetween(startDate, endDate)
    }

    fun findByUserIdAndCreatedAtBetween(
        userId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<Payment> {
        return queryRepository.findByUserIdAndCreatedAtBetween(userId, startDate, endDate)
    }

    fun searchPayments(
        userId: String? = null,
        status: PaymentStatus? = null,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null
    ): List<Payment> {
        return queryRepository.searchPayments(userId, status, startDate, endDate)
    }

    fun findAll(): List<Payment> {
        return jpaRepository.findAll()
    }

    fun delete(payment: Payment) {
        jpaRepository.delete(payment)
    }

    fun existsByOrderId(orderId: String): Boolean {
        return jpaRepository.findByOrderId(orderId) != null
    }
}
