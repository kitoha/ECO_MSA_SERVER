package com.ecommerce.repository

import com.ecommerce.entity.PaymentTransaction
import com.ecommerce.entity.QPaymentTransaction
import com.ecommerce.enums.TransactionStatus
import com.ecommerce.enums.TransactionType
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class PaymentTransactionQueryRepository(
    private val queryFactory: JPAQueryFactory
) {

    private val paymentTransaction = QPaymentTransaction.paymentTransaction

    fun findByPaymentId(paymentId: Long): List<PaymentTransaction> {
        return queryFactory
            .selectFrom(paymentTransaction)
            .where(
                eqPaymentId(paymentId),
                notDeleted()
            )
            .orderBy(paymentTransaction.createdAt.desc())
            .fetch()
    }

    fun findByTransactionType(transactionType: TransactionType): List<PaymentTransaction> {
        return queryFactory
            .selectFrom(paymentTransaction)
            .where(
                eqTransactionType(transactionType),
                notDeleted()
            )
            .orderBy(paymentTransaction.createdAt.desc())
            .fetch()
    }

    fun findByPaymentIdAndTransactionType(
        paymentId: Long,
        transactionType: TransactionType
    ): List<PaymentTransaction> {
        return queryFactory
            .selectFrom(paymentTransaction)
            .where(
                eqPaymentId(paymentId),
                eqTransactionType(transactionType),
                notDeleted()
            )
            .orderBy(paymentTransaction.createdAt.desc())
            .fetch()
    }

    fun findByStatus(status: TransactionStatus): List<PaymentTransaction> {
        return queryFactory
            .selectFrom(paymentTransaction)
            .where(
                eqStatus(status),
                notDeleted()
            )
            .orderBy(paymentTransaction.createdAt.desc())
            .fetch()
    }

    fun findByPaymentIdAndStatus(
        paymentId: Long,
        status: TransactionStatus
    ): List<PaymentTransaction> {
        return queryFactory
            .selectFrom(paymentTransaction)
            .where(
                eqPaymentId(paymentId),
                eqStatus(status),
                notDeleted()
            )
            .orderBy(paymentTransaction.createdAt.desc())
            .fetch()
    }

    private fun notDeleted(): BooleanExpression {
        return paymentTransaction.deletedAt.isNull
    }

    private fun eqPaymentId(paymentId: Long?): BooleanExpression? {
        return paymentId?.let { paymentTransaction.payment.id.eq(it) }
    }

    private fun eqTransactionType(transactionType: TransactionType?): BooleanExpression? {
        return transactionType?.let { paymentTransaction.transactionType.eq(it) }
    }

    private fun eqStatus(status: TransactionStatus?): BooleanExpression? {
        return status?.let { paymentTransaction.status.eq(it) }
    }
}
