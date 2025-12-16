package com.ecommerce.response

import com.ecommerce.entity.PaymentTransaction
import com.ecommerce.enums.TransactionStatus
import com.ecommerce.enums.TransactionType
import java.time.LocalDateTime

data class PaymentTransactionResponse(
  val id: Long,
  val paymentId: Long,
  val transactionType: TransactionType,
  val status: TransactionStatus,
  val pgTransactionId: String?,
  val pgResponseCode: String?,
  val pgResponseMessage: String?,
  val createdAt: LocalDateTime?
) {
  companion object {
    fun from(transaction: PaymentTransaction): PaymentTransactionResponse {
      return PaymentTransactionResponse(
        id = transaction.id,
        paymentId = transaction.payment.id,
        transactionType = transaction.transactionType,
        status = transaction.status,
        pgTransactionId = transaction.pgTransactionId,
        pgResponseCode = transaction.pgResponseCode,
        pgResponseMessage = transaction.pgResponseMessage,
        createdAt = transaction.createdAt
      )
    }
  }
}
