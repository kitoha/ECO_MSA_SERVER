package com.ecommerce.repository

import com.ecommerce.entity.Payment
import com.ecommerce.entity.QPayment
import com.ecommerce.enums.PaymentStatus
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class PaymentQueryRepository(
    private val queryFactory: JPAQueryFactory
) {

    private val payment = QPayment.payment

    fun findByUserId(userId: String): List<Payment> {
        return queryFactory
            .selectFrom(payment)
            .where(
                eqUserId(userId),
                notDeleted()
            )
            .orderBy(payment.createdAt.desc())
            .fetch()
    }

    fun findByStatus(status: PaymentStatus): List<Payment> {
        return queryFactory
            .selectFrom(payment)
            .where(
                eqStatus(status),
                notDeleted()
            )
            .orderBy(payment.createdAt.desc())
            .fetch()
    }

    fun findByUserIdAndStatus(userId: String, status: PaymentStatus): List<Payment> {
        return queryFactory
            .selectFrom(payment)
            .where(
                eqUserId(userId),
                eqStatus(status),
                notDeleted()
            )
            .orderBy(payment.createdAt.desc())
            .fetch()
    }

    fun findByCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Payment> {
        return queryFactory
            .selectFrom(payment)
            .where(
                betweenCreatedAt(startDate, endDate),
                notDeleted()
            )
            .orderBy(payment.createdAt.desc())
            .fetch()
    }

    fun findByUserIdAndCreatedAtBetween(
        userId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<Payment> {
        return queryFactory
            .selectFrom(payment)
            .where(
                eqUserId(userId),
                betweenCreatedAt(startDate, endDate),
                notDeleted()
            )
            .orderBy(payment.createdAt.desc())
            .fetch()
    }

    fun searchPayments(
        userId: String?,
        status: PaymentStatus?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?
    ): List<Payment> {
        return queryFactory
            .selectFrom(payment)
            .where(
                eqUserId(userId),
                eqStatus(status),
                betweenCreatedAt(startDate, endDate),
                notDeleted()
            )
            .orderBy(payment.createdAt.desc())
            .fetch()
    }

    private fun notDeleted(): BooleanExpression {
        return payment.deletedAt.isNull
    }

    private fun eqUserId(userId: String?): BooleanExpression? {
        return userId?.let { payment.userId.eq(it) }
    }

    private fun eqStatus(status: PaymentStatus?): BooleanExpression? {
        return status?.let { payment.status.eq(it) }
    }

    private fun betweenCreatedAt(startDate: LocalDateTime?, endDate: LocalDateTime?): BooleanExpression? {
        if (startDate == null || endDate == null) return null
        return payment.createdAt.between(startDate, endDate)
    }
}
