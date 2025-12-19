package com.ecommerce.service

import com.ecommerce.client.PaymentGateway
import com.ecommerce.entity.Payment
import com.ecommerce.entity.PaymentTransaction
import com.ecommerce.enums.PaymentStatus
import com.ecommerce.enums.TransactionType
import com.ecommerce.event.*
import com.ecommerce.exception.*
import com.ecommerce.generator.TsidGenerator
import com.ecommerce.repository.PaymentRepository
import com.ecommerce.repository.PaymentTransactionRepository
import com.ecommerce.request.CreatePaymentRequest
import com.ecommerce.request.PaymentApprovalRequest
import com.ecommerce.request.PaymentRefundRequest
import com.ecommerce.response.PaymentResponse
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
  private val paymentRepository: PaymentRepository,
  private val paymentTransactionRepository: PaymentTransactionRepository,
  private val kafkaTemplate: KafkaTemplate<String, Any>,
  private val idGenerator: TsidGenerator,
  private val paymentGateway: PaymentGateway
) {

  private val logger = LoggerFactory.getLogger(PaymentService::class.java)

  @Transactional
  fun createPayment(request: CreatePaymentRequest): PaymentResponse {
    logger.info("Creating payment for order: ${request.orderId}")

    val existingPayment = paymentRepository.findByOrderId(request.orderId)
    if (existingPayment != null) {
      throw DuplicateOrderPaymentException("이미 해당 주문에 대한 결제가 존재합니다: ${request.orderId}")
    }

    val payment = Payment(
      id = idGenerator.generate(),
      orderId = request.orderId,
      userId = request.userId,
      amount = request.amount,
      status = PaymentStatus.PENDING,
      paymentMethod = request.paymentMethod
    )

    val savedPayment = paymentRepository.save(payment)

    val event = PaymentCreatedEvent(
      paymentId = savedPayment.id,
      orderId = savedPayment.orderId,
      userId = savedPayment.userId,
      amount = savedPayment.amount,
      paymentMethod = savedPayment.paymentMethod
    )
    kafkaTemplate.send("payment-created", savedPayment.orderId, event)

    logger.info("Payment created successfully: ${savedPayment.id}")

    return PaymentResponse.from(savedPayment, includeTransactions = false)
  }

  @Transactional(readOnly = true)
  fun getPayment(paymentId: Long): PaymentResponse {
    val payment = paymentRepository.findById(paymentId)
      ?: throw PaymentNotFoundException(paymentId)

    return PaymentResponse.from(payment, includeTransactions = true)
  }

  @Transactional(readOnly = true)
  fun getPaymentByOrderId(orderId: String): PaymentResponse {
    val payment = paymentRepository.findByOrderId(orderId)
      ?: throw PaymentNotFoundByOrderIdException("주문에 대한 결제를 찾을 수 없습니다: $orderId")

    return PaymentResponse.from(payment, includeTransactions = true)
  }

  @Transactional(readOnly = true)
  fun getPaymentsByUserId(userId: String): List<PaymentResponse> {
    val payments = paymentRepository.findByUserId(userId)

    return payments.map { PaymentResponse.from(it, includeTransactions = false) }
  }

  @Transactional
  fun approvePayment(paymentId: Long, request: PaymentApprovalRequest): PaymentResponse {
    logger.info("Approving payment: $paymentId")

    val payment = paymentRepository.findById(paymentId)
      ?: throw PaymentNotFoundException(paymentId)

    if (payment.status == PaymentStatus.COMPLETED) {
      throw PaymentAlreadyCompletedException(paymentId)
    }

    if (!payment.isPayable()) {
      throw InvalidPaymentStateException("결제를 승인할 수 있는 상태가 아닙니다: ${payment.status}")
    }

    payment.startProcessing(
      pgProvider = request.pgProvider,
      pgPaymentKey = request.pgPaymentKey,
      paymentMethod = payment.paymentMethod ?: throw IllegalStateException("결제 수단이 설정되지 않았습니다")
    )

    try {
      // PG사에 실제 결제 승인 요청
      val pgResponse = paymentGateway.authorize(
        orderId = payment.orderId,
        amount = payment.amount,
        paymentMethod = payment.paymentMethod!!
      )

      if (pgResponse.success) {
        val transaction = PaymentTransaction.success(
          transactionType = TransactionType.AUTH,
          amount = payment.amount,
          pgTransactionId = pgResponse.transactionId,
          pgResponseCode = pgResponse.responseCode,
          pgResponseMessage = pgResponse.responseMessage
        )
        payment.addTransaction(transaction)

        payment.complete()

        val savedPayment = paymentRepository.save(payment)

        val event = PaymentCompletedEvent(
          paymentId = savedPayment.id,
          orderId = savedPayment.orderId,
          userId = savedPayment.userId,
          amount = savedPayment.amount,
          pgProvider = savedPayment.pgProvider ?: "",
          pgPaymentKey = savedPayment.pgPaymentKey ?: ""
        )
        kafkaTemplate.send("payment-completed", savedPayment.orderId, event)

        logger.info("Payment approved successfully: ${savedPayment.id}")

        return PaymentResponse.from(savedPayment, includeTransactions = true)
      } else {
        // PG사 승인 실패
        val transaction = PaymentTransaction.failure(
          transactionType = TransactionType.AUTH,
          amount = payment.amount,
          pgTransactionId = pgResponse.transactionId,
          pgResponseCode = pgResponse.responseCode,
          pgResponseMessage = pgResponse.responseMessage
        )
        payment.addTransaction(transaction)

        payment.fail(pgResponse.responseMessage!!)

        val savedPayment = paymentRepository.save(payment)

        val event = PaymentFailedEvent(
          paymentId = savedPayment.id,
          orderId = savedPayment.orderId,
          userId = savedPayment.userId,
          amount = savedPayment.amount,
          failureReason = pgResponse.responseMessage!!
        )
        kafkaTemplate.send("payment-failed", savedPayment.orderId, event)

        logger.warn("Payment approval failed: ${savedPayment.id}, reason: ${pgResponse.responseMessage}")

        return PaymentResponse.from(savedPayment, includeTransactions = true)
      }
    } catch (e: Exception) {
      logger.error("PG communication error during payment approval", e)

      val transaction = PaymentTransaction.failure(
        transactionType = TransactionType.AUTH,
        amount = payment.amount,
        pgResponseCode = "ERROR",
        pgResponseMessage = "PG 통신 오류: ${e.message}"
      )
      payment.addTransaction(transaction)

      payment.fail("PG 통신 오류: ${e.message}")

      val savedPayment = paymentRepository.save(payment)

      val event = PaymentFailedEvent(
        paymentId = savedPayment.id,
        orderId = savedPayment.orderId,
        userId = savedPayment.userId,
        amount = savedPayment.amount,
        failureReason = "PG 통신 오류: ${e.message}"
      )
      kafkaTemplate.send("payment-failed", savedPayment.orderId, event)

      throw PaymentGatewayException("결제 승인 중 PG 통신 오류가 발생했습니다", e)
    }
  }

  @Transactional
  fun cancelPayment(paymentId: Long, reason: String = "사용자 요청"): PaymentResponse {
    logger.info("Cancelling payment: $paymentId")

    val payment = paymentRepository.findById(paymentId)
      ?: throw PaymentNotFoundException(paymentId)

    if (payment.status == PaymentStatus.CANCELLED) {
      throw PaymentAlreadyCancelledException(paymentId)
    }

    if (payment.status == PaymentStatus.COMPLETED) {
      throw InvalidPaymentStateException("완료된 결제는 취소할 수 없습니다. 환불을 진행해주세요")
    }

    // PG사에 취소 요청 (PROCESSING 상태이고 pgPaymentKey가 있는 경우)
    if (payment.status == PaymentStatus.PROCESSING && payment.pgPaymentKey != null) {
      try {
        val pgResponse = paymentGateway.cancel(
          pgPaymentKey = payment.pgPaymentKey!!,
          reason = reason
        )

        val transaction = if (pgResponse.success) {
          PaymentTransaction.success(
            transactionType = TransactionType.CANCEL,
            amount = payment.amount,
            pgTransactionId = pgResponse.transactionId,
            pgResponseCode = pgResponse.responseCode,
            pgResponseMessage = pgResponse.responseMessage
          )
        } else {
          PaymentTransaction.failure(
            transactionType = TransactionType.CANCEL,
            amount = payment.amount,
            pgTransactionId = pgResponse.transactionId,
            pgResponseCode = pgResponse.responseCode,
            pgResponseMessage = pgResponse.responseMessage
          )
        }
        payment.addTransaction(transaction)

        if (!pgResponse.success) {
          throw PaymentCancellationException("PG 취소 실패: ${pgResponse.responseMessage}")
        }
      } catch (e: Exception) {
        logger.error("PG communication error during payment cancellation", e)
        throw PaymentGatewayException("결제 취소 중 PG 통신 오류가 발생했습니다", e)
      }
    } else {
      // PENDING 상태이거나 pgPaymentKey가 없는 경우 (PG 승인 전)
      val transaction = PaymentTransaction.success(
        transactionType = TransactionType.CANCEL,
        amount = payment.amount,
        pgResponseCode = "CANCELLED",
        pgResponseMessage = reason
      )
      payment.addTransaction(transaction)
    }

    payment.cancel(reason)

    val savedPayment = paymentRepository.save(payment)

    val event = PaymentCancelledEvent(
      paymentId = savedPayment.id,
      orderId = savedPayment.orderId,
      userId = savedPayment.userId,
      amount = savedPayment.amount,
      reason = reason
    )
    kafkaTemplate.send("payment-cancelled", savedPayment.orderId, event)

    logger.info("Payment cancelled: ${savedPayment.id}, reason: $reason")

    return PaymentResponse.from(savedPayment, includeTransactions = true)
  }

  @Transactional
  fun refundPayment(paymentId: Long, request: PaymentRefundRequest): PaymentResponse {
    logger.info("Refunding payment: $paymentId")

    val payment = paymentRepository.findById(paymentId)
      ?: throw PaymentNotFoundException(paymentId)

    if (!payment.isRefundable()) {
      throw PaymentRefundException("환불할 수 있는 상태가 아닙니다: ${payment.status}")
    }

    if (payment.pgPaymentKey == null) {
      throw PaymentRefundException("PG 결제 키가 없어 환불을 진행할 수 없습니다")
    }

    try {
      // PG사에 환불 요청
      val pgResponse = paymentGateway.refund(
        pgPaymentKey = payment.pgPaymentKey!!,
        amount = payment.amount,
        reason = request.reason
      )

      val transaction = if (pgResponse.success) {
        PaymentTransaction.success(
          transactionType = TransactionType.REFUND,
          amount = payment.amount,
          pgTransactionId = pgResponse.transactionId,
          pgResponseCode = pgResponse.responseCode,
          pgResponseMessage = pgResponse.responseMessage
        )
      } else {
        PaymentTransaction.failure(
          transactionType = TransactionType.REFUND,
          amount = payment.amount,
          pgTransactionId = pgResponse.transactionId,
          pgResponseCode = pgResponse.responseCode,
          pgResponseMessage = pgResponse.responseMessage
        )
      }
      payment.addTransaction(transaction)

      if (!pgResponse.success) {
        throw PaymentRefundException("PG 환불 실패: ${pgResponse.responseMessage}")
      }

      payment.refund()

      val savedPayment = paymentRepository.save(payment)

      val event = PaymentRefundedEvent(
        paymentId = savedPayment.id,
        orderId = savedPayment.orderId,
        userId = savedPayment.userId,
        amount = savedPayment.amount,
        reason = request.reason
      )
      kafkaTemplate.send("payment-refunded", savedPayment.orderId, event)

      logger.info("Payment refunded successfully: ${savedPayment.id}")

      return PaymentResponse.from(savedPayment, includeTransactions = true)
    } catch (e: Exception) {
      logger.error("PG communication error during payment refund", e)
      throw PaymentGatewayException("결제 환불 중 PG 통신 오류가 발생했습니다", e)
    }
  }

  @Transactional
  fun failPayment(paymentId: Long, reason: String): PaymentResponse {
    logger.info("Failing payment: $paymentId, reason: $reason")

    val payment = paymentRepository.findById(paymentId)
      ?: throw PaymentNotFoundException(paymentId)

    payment.fail(reason)

    val transaction = PaymentTransaction.failure(
      transactionType = TransactionType.AUTH,
      amount = payment.amount,
      pgResponseCode = "FAILED",
      pgResponseMessage = reason
    )
    payment.addTransaction(transaction)

    val savedPayment = paymentRepository.save(payment)

    val event = PaymentFailedEvent(
      paymentId = savedPayment.id,
      orderId = savedPayment.orderId,
      userId = savedPayment.userId,
      amount = savedPayment.amount,
      failureReason = reason
    )
    kafkaTemplate.send("payment-failed", savedPayment.orderId, event)

    logger.info("Payment failed: ${savedPayment.id}")

    return PaymentResponse.from(savedPayment, includeTransactions = true)
  }
}
