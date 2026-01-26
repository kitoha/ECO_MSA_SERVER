package com.ecommerce.service

import com.ecommerce.entity.Payment
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class PaymentEventPublisher(
  private val kafkaTemplate: KafkaTemplate<String, com.google.protobuf.Message>
) {

  private val logger = LoggerFactory.getLogger(PaymentEventPublisher::class.java)

  private fun createMoneyProto(amount: java.math.BigDecimal): com.ecommerce.proto.common.Money {
    return com.ecommerce.proto.common.Money.newBuilder()
      .setAmount(amount.toString())
      .setCurrency("KRW")
      .build()
  }

  private fun createTimestamp(): com.google.protobuf.Timestamp {
    val now = java.time.Instant.now()
    return com.google.protobuf.Timestamp.newBuilder()
      .setSeconds(now.epochSecond)
      .setNanos(now.nano)
      .build()
  }

  fun publishPaymentCreated(payment: Payment) {
    val protoPaymentMethod = when (payment.paymentMethod) {
      com.ecommerce.enums.PaymentMethod.CARD -> com.ecommerce.proto.payment.PaymentMethod.CARD
      com.ecommerce.enums.PaymentMethod.BANK_TRANSFER -> com.ecommerce.proto.payment.PaymentMethod.BANK_TRANSFER
      com.ecommerce.enums.PaymentMethod.VIRTUAL_ACCOUNT -> com.ecommerce.proto.payment.PaymentMethod.VIRTUAL_ACCOUNT
      com.ecommerce.enums.PaymentMethod.EASY_PAY -> com.ecommerce.proto.payment.PaymentMethod.EASY_PAY
      com.ecommerce.enums.PaymentMethod.MOBILE -> com.ecommerce.proto.payment.PaymentMethod.MOBILE
      null -> com.ecommerce.proto.payment.PaymentMethod.PAYMENT_METHOD_UNSPECIFIED
    }
    
    val event = com.ecommerce.proto.payment.PaymentCreatedEvent.newBuilder()
      .setPaymentId(payment.id)
      .setOrderId(payment.orderId)
      .setUserId(payment.userId)
      .setAmount(createMoneyProto(payment.amount))
      .setPaymentMethod(protoPaymentMethod)
      .setTimestamp(createTimestamp())
      .build()
    kafkaTemplate.send("payment-created", payment.orderId, event)
    logger.info("Published PaymentCreatedEvent for payment: ${payment.id}")
  }

  fun publishPaymentCompleted(payment: Payment) {
    val event = com.ecommerce.proto.payment.PaymentCompletedEvent.newBuilder()
      .setPaymentId(payment.id)
      .setOrderId(payment.orderId)
      .setUserId(payment.userId)
      .setAmount(createMoneyProto(payment.amount))
      .setPgProvider(payment.pgProvider ?: "")
      .setPgPaymentKey(payment.pgPaymentKey ?: "")
      .setTimestamp(createTimestamp())
      .build()
    kafkaTemplate.send("payment-completed", payment.orderId, event)
    logger.info("Published PaymentCompletedEvent for payment: ${payment.id}")
  }

  fun publishPaymentFailed(payment: Payment, failureReason: String) {
    val event = com.ecommerce.proto.payment.PaymentFailedEvent.newBuilder()
      .setPaymentId(payment.id)
      .setOrderId(payment.orderId)
      .setUserId(payment.userId)
      .setAmount(createMoneyProto(payment.amount))
      .setFailureReason(failureReason)
      .setTimestamp(createTimestamp())
      .build()
    kafkaTemplate.send("payment-failed", payment.orderId, event)
    logger.info("Published PaymentFailedEvent for payment: ${payment.id}")
  }

  fun publishPaymentCancelled(payment: Payment, reason: String) {
    val event = com.ecommerce.proto.payment.PaymentCancelledEvent.newBuilder()
      .setPaymentId(payment.id)
      .setOrderId(payment.orderId)
      .setUserId(payment.userId)
      .setAmount(createMoneyProto(payment.amount))
      .setReason(reason)
      .setTimestamp(createTimestamp())
      .build()
    kafkaTemplate.send("payment-cancelled", payment.orderId, event)
    logger.info("Published PaymentCancelledEvent for payment: ${payment.id}")
  }

  fun publishPaymentRefunded(payment: Payment, reason: String) {
    val event = com.ecommerce.proto.payment.PaymentRefundedEvent.newBuilder()
      .setPaymentId(payment.id)
      .setOrderId(payment.orderId)
      .setUserId(payment.userId)
      .setAmount(createMoneyProto(payment.amount))
      .setReason(reason)
      .setTimestamp(createTimestamp())
      .build()
    kafkaTemplate.send("payment-refunded", payment.orderId, event)
    logger.info("Published PaymentRefundedEvent for payment: ${payment.id}")
  }
}
