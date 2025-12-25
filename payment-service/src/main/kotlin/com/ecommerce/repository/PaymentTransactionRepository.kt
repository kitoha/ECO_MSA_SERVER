package com.ecommerce.repository

import com.ecommerce.entity.PaymentTransaction
import com.ecommerce.enums.TransactionStatus
import com.ecommerce.enums.TransactionType
import org.springframework.stereotype.Repository

@Repository
class PaymentTransactionRepository(
    private val jpaRepository: PaymentTransactionJpaRepository,
    private val queryRepository: PaymentTransactionQueryRepository
) {

    fun save(paymentTransaction: PaymentTransaction): PaymentTransaction {
        return jpaRepository.save(paymentTransaction)
    }

    fun findById(id: Long): PaymentTransaction? {
        return jpaRepository.findById(id).orElse(null)
    }

    fun findByPaymentId(paymentId: Long): List<PaymentTransaction> {
        return queryRepository.findByPaymentId(paymentId)
    }

    fun findByTransactionType(transactionType: TransactionType): List<PaymentTransaction> {
        return queryRepository.findByTransactionType(transactionType)
    }

    fun findByPaymentIdAndTransactionType(
        paymentId: Long,
        transactionType: TransactionType
    ): List<PaymentTransaction> {
        return queryRepository.findByPaymentIdAndTransactionType(paymentId, transactionType)
    }

    fun findByStatus(status: TransactionStatus): List<PaymentTransaction> {
        return queryRepository.findByStatus(status)
    }

    fun findByPaymentIdAndStatus(paymentId: Long, status: TransactionStatus): List<PaymentTransaction> {
        return queryRepository.findByPaymentIdAndStatus(paymentId, status)
    }

    fun findAll(): List<PaymentTransaction> {
        return jpaRepository.findAll()
    }

    fun delete(paymentTransaction: PaymentTransaction) {
        jpaRepository.delete(paymentTransaction)
    }
}
