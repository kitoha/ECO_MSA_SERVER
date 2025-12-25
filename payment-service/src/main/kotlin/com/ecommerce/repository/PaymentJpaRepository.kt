package com.ecommerce.repository

import com.ecommerce.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PaymentJpaRepository : JpaRepository<Payment, Long> {

    fun findByOrderId(orderId: String): Payment?

    fun findByPgPaymentKey(pgPaymentKey: String): Payment?

    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p._transactions WHERE p.id = :id")
    fun findByIdWithTransactions(@Param("id") id: Long): Payment?

    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p._transactions WHERE p.orderId = :orderId")
    fun findByOrderIdWithTransactions(@Param("orderId") orderId: String): Payment?
}
