package com.ecommerce.fixtures

import com.ecommerce.entity.Payment
import com.ecommerce.entity.PaymentTransaction
import com.ecommerce.enums.PaymentMethod
import com.ecommerce.enums.PaymentStatus
import com.ecommerce.enums.TransactionStatus
import com.ecommerce.enums.TransactionType
import java.math.BigDecimal
import java.time.LocalDateTime

object PaymentFixtures {

  fun createPayment(
    id: Long = 1L,
    orderId: String = "ORDER-001",
    userId: String = "USER-001",
    amount: BigDecimal = BigDecimal("100000"),
    status: PaymentStatus = PaymentStatus.PENDING,
    paymentMethod: PaymentMethod? = PaymentMethod.CARD,
    pgProvider: String? = null,
    pgPaymentKey: String? = null,
    failureReason: String? = null,
    approvedAt: LocalDateTime? = null
  ): Payment {
    return Payment(
      id = id,
      orderId = orderId,
      userId = userId,
      amount = amount,
      status = status,
      paymentMethod = paymentMethod,
      pgProvider = pgProvider,
      pgPaymentKey = pgPaymentKey,
      failureReason = failureReason,
      approvedAt = approvedAt
    )
  }

  fun createSuccessTransaction(
    transactionType: TransactionType = TransactionType.AUTH,
    amount: BigDecimal = BigDecimal("100000"),
    pgTransactionId: String = "PG-TXN-001",
    pgResponseCode: String = "0000",
    pgResponseMessage: String = "Success"
  ): PaymentTransaction {
    return PaymentTransaction.success(
      transactionType = transactionType,
      amount = amount,
      pgTransactionId = pgTransactionId,
      pgResponseCode = pgResponseCode,
      pgResponseMessage = pgResponseMessage
    )
  }

  fun createFailureTransaction(
    transactionType: TransactionType = TransactionType.AUTH,
    amount: BigDecimal = BigDecimal("100000"),
    pgTransactionId: String? = null,
    pgResponseCode: String = "9999",
    pgResponseMessage: String = "Failed"
  ): PaymentTransaction {
    return PaymentTransaction.failure(
      transactionType = transactionType,
      amount = amount,
      pgTransactionId = pgTransactionId,
      pgResponseCode = pgResponseCode,
      pgResponseMessage = pgResponseMessage
    )
  }
}
