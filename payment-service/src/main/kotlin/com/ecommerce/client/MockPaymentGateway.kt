package com.ecommerce.client

import com.ecommerce.enums.PaymentMethod
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*

@Component
class MockPaymentGateway : PaymentGateway {

  private val logger = LoggerFactory.getLogger(MockPaymentGateway::class.java)

  override fun authorize(
    orderId: String,
    amount: BigDecimal,
    paymentMethod: PaymentMethod
  ): PaymentGatewayResponse {
    logger.info("Mock authorize payment: orderId=$orderId, amount=$amount, method=$paymentMethod")

    return PaymentGatewayResponse(
      success = true,
      transactionId = "TXN-${UUID.randomUUID()}",
      paymentKey = "PAY-${UUID.randomUUID()}",
      amount = amount,
      responseCode = "0000",
      responseMessage = "승인 성공"
    )
  }

  override fun capture(
    pgPaymentKey: String,
    amount: BigDecimal
  ): PaymentGatewayResponse {
    logger.info("Mock capture payment: pgPaymentKey=$pgPaymentKey, amount=$amount")

    return PaymentGatewayResponse(
      success = true,
      transactionId = "TXN-${UUID.randomUUID()}",
      paymentKey = pgPaymentKey,
      amount = amount,
      responseCode = "0000",
      responseMessage = "결제 승인 성공"
    )
  }

  override fun cancel(
    pgPaymentKey: String,
    reason: String
  ): PaymentGatewayResponse {
    logger.info("Mock cancel payment: pgPaymentKey=$pgPaymentKey, reason=$reason")

    return PaymentGatewayResponse(
      success = true,
      transactionId = "TXN-${UUID.randomUUID()}",
      paymentKey = pgPaymentKey,
      amount = null,
      responseCode = "0000",
      responseMessage = "취소 성공"
    )
  }

  override fun refund(
    pgPaymentKey: String,
    amount: BigDecimal,
    reason: String
  ): PaymentGatewayResponse {
    logger.info("Mock refund payment: pgPaymentKey=$pgPaymentKey, amount=$amount, reason=$reason")

    return PaymentGatewayResponse(
      success = true,
      transactionId = "TXN-${UUID.randomUUID()}",
      paymentKey = pgPaymentKey,
      amount = amount,
      responseCode = "0000",
      responseMessage = "환불 성공"
    )
  }

  override fun verifyWebhook(
    signature: String,
    payload: String
  ): Boolean {
    logger.info("Mock verify webhook: signature=$signature")
    return true
  }
}
