package com.ecommerce.service

import com.ecommerce.client.PaymentGateway
import com.ecommerce.client.PaymentGatewayResponse
import com.ecommerce.enums.PaymentMethod
import com.ecommerce.exception.PaymentGatewayException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PaymentGatewayAdapter(
  private val paymentGateway: PaymentGateway
) {

  private val logger = LoggerFactory.getLogger(PaymentGatewayAdapter::class.java)

  fun authorize(
    orderId: String,
    amount: BigDecimal,
    paymentMethod: PaymentMethod
  ): PaymentGatewayResponse {
    return executeWithErrorHandling("authorize") {
      paymentGateway.authorize(orderId, amount, paymentMethod)
    }
  }

  fun capture(
    pgPaymentKey: String,
    amount: BigDecimal
  ): PaymentGatewayResponse {
    return executeWithErrorHandling("capture") {
      paymentGateway.capture(pgPaymentKey, amount)
    }
  }

  fun cancel(
    pgPaymentKey: String,
    reason: String
  ): PaymentGatewayResponse {
    return executeWithErrorHandling("cancel") {
      paymentGateway.cancel(pgPaymentKey, reason)
    }
  }

  fun refund(
    pgPaymentKey: String,
    amount: BigDecimal,
    reason: String
  ): PaymentGatewayResponse {
    return executeWithErrorHandling("refund") {
      paymentGateway.refund(pgPaymentKey, amount, reason)
    }
  }

  fun verifyWebhook(signature: String, payload: String): Boolean {
    return try {
      paymentGateway.verifyWebhook(signature, payload)
    } catch (e: Exception) {
      logger.error("Webhook verification failed", e)
      false
    }
  }

  private fun executeWithErrorHandling(
    operation: String,
    block: () -> PaymentGatewayResponse
  ): PaymentGatewayResponse {
    return try {
      block()
    } catch (e: Exception) {
      logger.error("PG $operation operation failed", e)
      throw PaymentGatewayException("PG $operation 중 통신 오류가 발생했습니다: ${e.message}", e)
    }
  }
}
