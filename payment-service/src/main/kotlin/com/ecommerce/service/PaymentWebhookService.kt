package com.ecommerce.service

import com.ecommerce.entity.PaymentTransaction
import com.ecommerce.enums.TransactionType
import com.ecommerce.exception.PaymentNotFoundByOrderIdException
import com.ecommerce.repository.PaymentRepository
import com.ecommerce.request.PaymentWebhookRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentWebhookService(
  private val paymentRepository: PaymentRepository,
  private val gatewayAdapter: PaymentGatewayAdapter,
  private val eventPublisher: PaymentEventPublisher
) {

  private val logger = LoggerFactory.getLogger(PaymentWebhookService::class.java)

  @Transactional
  fun processPaymentWebhook(request: PaymentWebhookRequest, signature: String, payload: String) {
    logger.info("Processing payment webhook: eventType=${request.eventType}, orderId=${request.orderId}")

    // Webhook 검증
    if (!gatewayAdapter.verifyWebhook(signature, payload)) {
      logger.error("Invalid webhook signature for orderId: ${request.orderId}")
      throw SecurityException("유효하지 않은 Webhook 요청입니다")
    }

    when (request.eventType) {
      "PAYMENT_COMPLETED" -> handlePaymentCompleted(request)
      "PAYMENT_FAILED" -> handlePaymentFailed(request)
      "PAYMENT_CANCELLED" -> handlePaymentCancelled(request)
      "PAYMENT_REFUNDED" -> handlePaymentRefunded(request)
      else -> {
        logger.warn("Unknown webhook event type: ${request.eventType}")
      }
    }
  }

  private fun handlePaymentCompleted(request: PaymentWebhookRequest) {
    val payment = paymentRepository.findByOrderId(request.orderId)
      ?: throw PaymentNotFoundByOrderIdException(request.orderId)

    if (payment.isCompleted()) {
      logger.warn("Payment already completed: ${payment.id}")
      return
    }

    payment.startProcessing(
      pgProvider = request.pgProvider,
      pgPaymentKey = request.pgPaymentKey,
      paymentMethod = request.paymentMethod
    )

    val transaction = PaymentTransaction.success(
      transactionType = TransactionType.AUTH,
      amount = request.amount,
      pgTransactionId = request.pgTransactionId,
      pgResponseCode = request.responseCode,
      pgResponseMessage = request.responseMessage
    )
    payment.addTransaction(transaction)

    payment.complete()

    val savedPayment = paymentRepository.save(payment)

    eventPublisher.publishPaymentCompleted(savedPayment)

    logger.info("Payment completed via webhook: ${savedPayment.id}")
  }

  private fun handlePaymentFailed(request: PaymentWebhookRequest) {
    val payment = paymentRepository.findByOrderId(request.orderId)
      ?: throw PaymentNotFoundByOrderIdException(request.orderId)

    if (payment.isFailed()) {
      logger.warn("Payment already failed: ${payment.id}")
      return
    }

    val transaction = PaymentTransaction.failure(
      transactionType = TransactionType.AUTH,
      amount = request.amount,
      pgTransactionId = request.pgTransactionId,
      pgResponseCode = request.responseCode,
      pgResponseMessage = request.responseMessage
    )
    payment.addTransaction(transaction)

    payment.fail(request.responseMessage)

    val savedPayment = paymentRepository.save(payment)

    eventPublisher.publishPaymentFailed(savedPayment, request.responseMessage)

    logger.info("Payment failed via webhook: ${savedPayment.id}")
  }

  private fun handlePaymentCancelled(request: PaymentWebhookRequest) {
    val payment = paymentRepository.findByOrderId(request.orderId)
      ?: throw PaymentNotFoundByOrderIdException(request.orderId)

    if (payment.isCancelled()) {
      logger.warn("Payment already cancelled: ${payment.id}")
      return
    }

    val transaction = PaymentTransaction.success(
      transactionType = TransactionType.CANCEL,
      amount = request.amount,
      pgTransactionId = request.pgTransactionId,
      pgResponseCode = request.responseCode,
      pgResponseMessage = request.responseMessage
    )
    payment.addTransaction(transaction)

    payment.cancel(request.responseMessage)

    val savedPayment = paymentRepository.save(payment)

    eventPublisher.publishPaymentCancelled(savedPayment, request.responseMessage)

    logger.info("Payment cancelled via webhook: ${savedPayment.id}")
  }

  private fun handlePaymentRefunded(request: PaymentWebhookRequest) {
    val payment = paymentRepository.findByOrderId(request.orderId)
      ?: throw PaymentNotFoundByOrderIdException(request.orderId)

    if (payment.isRefunded()) {
      logger.warn("Payment already refunded: ${payment.id}")
      return
    }

    val transaction = PaymentTransaction.success(
      transactionType = TransactionType.REFUND,
      amount = request.amount,
      pgTransactionId = request.pgTransactionId,
      pgResponseCode = request.responseCode,
      pgResponseMessage = request.responseMessage
    )
    payment.addTransaction(transaction)

    payment.refund()

    val savedPayment = paymentRepository.save(payment)

    eventPublisher.publishPaymentRefunded(savedPayment, request.responseMessage)

    logger.info("Payment refunded via webhook: ${savedPayment.id}")
  }
}
