package com.ecommerce.response

import com.ecommerce.entity.Payment
import com.ecommerce.enums.PaymentMethod
import com.ecommerce.enums.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentResponse(
  val id: Long,
  val orderId: String,
  val userId: String,
  val amount: BigDecimal,
  val status: PaymentStatus,
  val paymentMethod: PaymentMethod?,
  val pgProvider: String?,
  val pgPaymentKey: String?,
  val failureReason: String?,
  val approvedAt: LocalDateTime?,
  val transactions: List<PaymentTransactionResponse>?,
  val createdAt: LocalDateTime?,
  val updatedAt: LocalDateTime?
) {
  companion object {
    fun from(payment: Payment, includeTransactions: Boolean = false): PaymentResponse {
      return PaymentResponse(
        id = payment.id,
        orderId = payment.orderId,
        userId = payment.userId,
        amount = payment.amount,
        status = payment.status,
        paymentMethod = payment.paymentMethod,
        pgProvider = payment.pgProvider,
        pgPaymentKey = payment.pgPaymentKey,
        failureReason = payment.failureReason,
        approvedAt = payment.approvedAt,
        transactions = if (includeTransactions) {
          payment.transactions.map { PaymentTransactionResponse.from(it) }
        } else null,
        createdAt = payment.createdAt,
        updatedAt = payment.updatedAt
      )
    }
  }
}
