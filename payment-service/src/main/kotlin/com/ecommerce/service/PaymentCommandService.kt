package com.ecommerce.service

import com.ecommerce.entity.Payment
import com.ecommerce.enums.PaymentStatus
import com.ecommerce.enums.TransactionType
import com.ecommerce.exception.*
import com.ecommerce.generator.TsidGenerator
import com.ecommerce.repository.PaymentRepository
import com.ecommerce.request.CreatePaymentRequest
import com.ecommerce.request.PaymentApprovalRequest
import com.ecommerce.request.PaymentRefundRequest
import com.ecommerce.response.PaymentResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentCommandService(
  private val paymentRepository: PaymentRepository,
  private val idGenerator: TsidGenerator,
  private val gatewayAdapter: PaymentGatewayAdapter,
  private val transactionFactory: PaymentTransactionFactory,
  private val eventPublisher: PaymentEventPublisher
) {

  private val logger = LoggerFactory.getLogger(PaymentCommandService::class.java)

  @Transactional
  fun createPayment(request: CreatePaymentRequest): PaymentResponse {
    logger.info("Creating payment for order: ${request.orderId}")

    validatePaymentDoesNotExist(request.orderId)

    val payment = Payment(
      id = idGenerator.generate(),
      orderId = request.orderId,
      userId = request.userId,
      amount = request.amount,
      status = PaymentStatus.PENDING,
      paymentMethod = request.paymentMethod
    )

    val savedPayment = paymentRepository.save(payment)

    eventPublisher.publishPaymentCreated(savedPayment)

    logger.info("Payment created successfully: ${savedPayment.id}")

    return PaymentResponse.from(savedPayment, includeTransactions = false)
  }

  @Transactional
  fun approvePayment(paymentId: Long, request: PaymentApprovalRequest): PaymentResponse {
    logger.info("Approving payment: $paymentId")

    val payment = findPaymentById(paymentId)

    validatePaymentCanBeApproved(payment)

    val paymentMethod = payment.paymentMethod
      ?: throw IllegalStateException("결제 수단이 설정되지 않았습니다")

    payment.startProcessing(
      pgProvider = request.pgProvider,
      pgPaymentKey = request.pgPaymentKey,
      paymentMethod = paymentMethod
    )

    val pgResponse = gatewayAdapter.authorize(
      orderId = payment.orderId,
      amount = payment.amount,
      paymentMethod = paymentMethod
    )

    val transaction = transactionFactory.createFromGatewayResponse(
      transactionType = TransactionType.AUTH,
      amount = payment.amount,
      response = pgResponse
    )
    payment.addTransaction(transaction)

    if (pgResponse.success) {
      payment.complete()
      val savedPayment = paymentRepository.save(payment)
      eventPublisher.publishPaymentCompleted(savedPayment)
      logger.info("Payment approved successfully: ${savedPayment.id}")
      return PaymentResponse.from(savedPayment, includeTransactions = true)
    } else {
      val failureReason = pgResponse.responseMessage ?: "결제 승인 실패"
      payment.fail(failureReason)
      val savedPayment = paymentRepository.save(payment)
      eventPublisher.publishPaymentFailed(savedPayment, failureReason)
      logger.warn("Payment approval failed: ${savedPayment.id}, reason: $failureReason")
      return PaymentResponse.from(savedPayment, includeTransactions = true)
    }
  }

  @Transactional
  fun cancelPayment(paymentId: Long, reason: String = "사용자 요청"): PaymentResponse {
    logger.info("Cancelling payment: $paymentId")

    val payment = findPaymentById(paymentId)

    validatePaymentCanBeCancelled(payment)

    if (payment.status == PaymentStatus.PROCESSING && payment.pgPaymentKey != null) {
      cancelWithPg(payment, reason)
    } else {
      cancelWithoutPg(payment, reason)
    }

    payment.cancel(reason)

    val savedPayment = paymentRepository.save(payment)

    eventPublisher.publishPaymentCancelled(savedPayment, reason)

    logger.info("Payment cancelled: ${savedPayment.id}, reason: $reason")

    return PaymentResponse.from(savedPayment, includeTransactions = true)
  }

  @Transactional
  fun refundPayment(paymentId: Long, request: PaymentRefundRequest): PaymentResponse {
    logger.info("Refunding payment: $paymentId")

    val payment = findPaymentById(paymentId)

    validatePaymentCanBeRefunded(payment)

    val pgPaymentKey = payment.pgPaymentKey
      ?: throw PaymentRefundException("PG 결제 키가 없어 환불을 진행할 수 없습니다")

    val pgResponse = gatewayAdapter.refund(
      pgPaymentKey = pgPaymentKey,
      amount = payment.amount,
      reason = request.reason
    )

    val transaction = transactionFactory.createFromGatewayResponse(
      transactionType = TransactionType.REFUND,
      amount = payment.amount,
      response = pgResponse
    )
    payment.addTransaction(transaction)

    if (!pgResponse.success) {
      throw PaymentRefundException("PG 환불 실패: ${pgResponse.responseMessage}")
    }

    payment.refund()

    val savedPayment = paymentRepository.save(payment)

    eventPublisher.publishPaymentRefunded(savedPayment, request.reason)

    logger.info("Payment refunded successfully: ${savedPayment.id}")

    return PaymentResponse.from(savedPayment, includeTransactions = true)
  }

  @Transactional
  fun failPayment(paymentId: Long, reason: String): PaymentResponse {
    logger.info("Failing payment: $paymentId, reason: $reason")

    val payment = findPaymentById(paymentId)

    val transaction = transactionFactory.createFailureTransaction(
      transactionType = TransactionType.AUTH,
      amount = payment.amount,
      pgResponseCode = "FAILED",
      pgResponseMessage = reason
    )
    payment.addTransaction(transaction)

    payment.fail(reason)

    val savedPayment = paymentRepository.save(payment)

    eventPublisher.publishPaymentFailed(savedPayment, reason)

    logger.info("Payment failed: ${savedPayment.id}")

    return PaymentResponse.from(savedPayment, includeTransactions = true)
  }

  private fun findPaymentById(paymentId: Long): Payment {
    return paymentRepository.findById(paymentId)
      ?: throw PaymentNotFoundException(paymentId)
  }

  private fun validatePaymentDoesNotExist(orderId: String) {
    val existingPayment = paymentRepository.findByOrderId(orderId)
    if (existingPayment != null) {
      throw DuplicateOrderPaymentException("이미 해당 주문에 대한 결제가 존재합니다: $orderId")
    }
  }

  private fun validatePaymentCanBeApproved(payment: Payment) {
    if (payment.status == PaymentStatus.COMPLETED) {
      throw PaymentAlreadyCompletedException(payment.id)
    }

    if (!payment.isPayable()) {
      throw InvalidPaymentStateException("결제를 승인할 수 있는 상태가 아닙니다: ${payment.status}")
    }
  }

  private fun validatePaymentCanBeCancelled(payment: Payment) {
    if (payment.status == PaymentStatus.CANCELLED) {
      throw PaymentAlreadyCancelledException(payment.id)
    }

    if (payment.status == PaymentStatus.COMPLETED) {
      throw InvalidPaymentStateException("완료된 결제는 취소할 수 없습니다. 환불을 진행해주세요")
    }
  }

  private fun validatePaymentCanBeRefunded(payment: Payment) {
    if (!payment.isRefundable()) {
      throw PaymentRefundException("환불할 수 있는 상태가 아닙니다: ${payment.status}")
    }
  }

  private fun cancelWithPg(payment: Payment, reason: String) {
    val pgPaymentKey = payment.pgPaymentKey
      ?: throw PaymentCancellationException("PG 결제 키가 없어 취소를 진행할 수 없습니다")

    val pgResponse = gatewayAdapter.cancel(
      pgPaymentKey = pgPaymentKey,
      reason = reason
    )

    val transaction = transactionFactory.createFromGatewayResponse(
      transactionType = TransactionType.CANCEL,
      amount = payment.amount,
      response = pgResponse
    )
    payment.addTransaction(transaction)

    if (!pgResponse.success) {
      throw PaymentCancellationException("PG 취소 실패: ${pgResponse.responseMessage}")
    }
  }

  private fun cancelWithoutPg(payment: Payment, reason: String) {
    val transaction = transactionFactory.createSuccessTransaction(
      transactionType = TransactionType.CANCEL,
      amount = payment.amount,
      pgResponseCode = "CANCELLED",
      pgResponseMessage = reason
    )
    payment.addTransaction(transaction)
  }
}
