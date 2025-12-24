package com.ecommerce.service

import com.ecommerce.exception.PaymentNotFoundByOrderIdException
import com.ecommerce.exception.PaymentNotFoundException
import com.ecommerce.repository.PaymentRepository
import com.ecommerce.response.PaymentResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PaymentQueryService(
  private val paymentRepository: PaymentRepository
) {

  private val logger = LoggerFactory.getLogger(PaymentQueryService::class.java)

  fun getPayment(paymentId: Long): PaymentResponse {
    logger.debug("Fetching payment by id: $paymentId")

    val payment = paymentRepository.findById(paymentId)
      ?: throw PaymentNotFoundException(paymentId)

    return PaymentResponse.from(payment, includeTransactions = true)
  }

  fun getPaymentByOrderId(orderId: String): PaymentResponse {
    logger.debug("Fetching payment by orderId: $orderId")

    val payment = paymentRepository.findByOrderId(orderId)
      ?: throw PaymentNotFoundByOrderIdException(orderId)

    return PaymentResponse.from(payment, includeTransactions = true)
  }

  fun getPaymentsByUserId(userId: String): List<PaymentResponse> {
    logger.debug("Fetching payments by userId: $userId")

    val payments = paymentRepository.findByUserId(userId)

    return payments.map { PaymentResponse.from(it, includeTransactions = false) }
  }
}
