package com.ecommerce.service

import com.ecommerce.entity.Payment
import com.ecommerce.event.*
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class PaymentEventPublisher(
  private val kafkaTemplate: KafkaTemplate<String, Any>
) {

  private val logger = LoggerFactory.getLogger(PaymentEventPublisher::class.java)

  fun publishPaymentCreated(payment: Payment) {
    val event = PaymentCreatedEvent(
      paymentId = payment.id,
      orderId = payment.orderId,
      userId = payment.userId,
      amount = payment.amount,
      paymentMethod = payment.paymentMethod!!
    )
    kafkaTemplate.send("payment-created", payment.orderId, event)
    logger.info("Published PaymentCreatedEvent for payment: ${payment.id}")
  }

  fun publishPaymentCompleted(payment: Payment) {
    val event = PaymentCompletedEvent(
      paymentId = payment.id,
      orderId = payment.orderId,
      userId = payment.userId,
      amount = payment.amount,
      pgProvider = payment.pgProvider ?: "",
      pgPaymentKey = payment.pgPaymentKey ?: ""
    )
    kafkaTemplate.send("payment-completed", payment.orderId, event)
    logger.info("Published PaymentCompletedEvent for payment: ${payment.id}")
  }

  fun publishPaymentFailed(payment: Payment, failureReason: String) {
    val event = PaymentFailedEvent(
      paymentId = payment.id,
      orderId = payment.orderId,
      userId = payment.userId,
      amount = payment.amount,
      failureReason = failureReason
    )
    kafkaTemplate.send("payment-failed", payment.orderId, event)
    logger.info("Published PaymentFailedEvent for payment: ${payment.id}")
  }

  fun publishPaymentCancelled(payment: Payment, reason: String) {
    val event = PaymentCancelledEvent(
      paymentId = payment.id,
      orderId = payment.orderId,
      userId = payment.userId,
      amount = payment.amount,
      reason = reason
    )
    kafkaTemplate.send("payment-cancelled", payment.orderId, event)
    logger.info("Published PaymentCancelledEvent for payment: ${payment.id}")
  }

  fun publishPaymentRefunded(payment: Payment, reason: String) {
    val event = PaymentRefundedEvent(
      paymentId = payment.id,
      orderId = payment.orderId,
      userId = payment.userId,
      amount = payment.amount,
      reason = reason
    )
    kafkaTemplate.send("payment-refunded", payment.orderId, event)
    logger.info("Published PaymentRefundedEvent for payment: ${payment.id}")
  }
}
