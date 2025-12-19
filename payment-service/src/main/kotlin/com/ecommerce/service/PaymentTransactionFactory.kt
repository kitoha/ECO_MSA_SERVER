package com.ecommerce.service

import com.ecommerce.client.PaymentGatewayResponse
import com.ecommerce.entity.PaymentTransaction
import com.ecommerce.enums.TransactionType
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PaymentTransactionFactory {

  fun createSuccessTransaction(
    transactionType: TransactionType,
    amount: BigDecimal,
    pgTransactionId: String? = null,
    pgResponseCode: String? = null,
    pgResponseMessage: String? = null
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
    transactionType: TransactionType,
    amount: BigDecimal,
    pgTransactionId: String? = null,
    pgResponseCode: String? = null,
    pgResponseMessage: String? = null
  ): PaymentTransaction {
    return PaymentTransaction.failure(
      transactionType = transactionType,
      amount = amount,
      pgTransactionId = pgTransactionId,
      pgResponseCode = pgResponseCode,
      pgResponseMessage = pgResponseMessage
    )
  }

  fun createFromGatewayResponse(
    transactionType: TransactionType,
    amount: BigDecimal,
    response: PaymentGatewayResponse
  ): PaymentTransaction {
    return if (response.success) {
      createSuccessTransaction(
        transactionType = transactionType,
        amount = amount,
        pgTransactionId = response.transactionId,
        pgResponseCode = response.responseCode,
        pgResponseMessage = response.responseMessage
      )
    } else {
      createFailureTransaction(
        transactionType = transactionType,
        amount = amount,
        pgTransactionId = response.transactionId,
        pgResponseCode = response.responseCode,
        pgResponseMessage = response.responseMessage
      )
    }
  }
}
