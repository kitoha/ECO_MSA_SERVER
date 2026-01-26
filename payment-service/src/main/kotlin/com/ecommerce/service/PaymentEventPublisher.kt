package com.ecommerce.service

import com.ecommerce.entity.Payment
import com.ecommerce.proto.common.Money
import com.ecommerce.proto.payment.*
import com.google.protobuf.Message
import com.google.protobuf.Timestamp
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import com.ecommerce.enums.PaymentMethod as DomainPaymentMethod
import com.ecommerce.proto.payment.PaymentMethod as ProtoPaymentMethod

@Component
class PaymentEventPublisher(
  private val kafkaTemplate: KafkaTemplate<String, Message>
) {

  private val logger = LoggerFactory.getLogger(PaymentEventPublisher::class.java)

  private fun createMoneyProto(amount: java.math.BigDecimal): Money {
    return Money.newBuilder()
      .setAmount(amount.toString())
      .setCurrency("KRW")
      .build()
  }

  private fun createTimestamp(): Timestamp {
    val now = java.time.Instant.now()
    return Timestamp.newBuilder()
      .setSeconds(now.epochSecond)
      .setNanos(now.nano)
      .build()
  }

  fun publishPaymentCreated(payment: Payment) {
    val protoPaymentMethod = when (payment.paymentMethod) {
      DomainPaymentMethod.CARD -> ProtoPaymentMethod.CARD
      DomainPaymentMethod.BANK_TRANSFER -> ProtoPaymentMethod.BANK_TRANSFER
      DomainPaymentMethod.VIRTUAL_ACCOUNT -> ProtoPaymentMethod.VIRTUAL_ACCOUNT
      DomainPaymentMethod.EASY_PAY -> ProtoPaymentMethod.EASY_PAY
      DomainPaymentMethod.MOBILE -> ProtoPaymentMethod.MOBILE
      null -> ProtoPaymentMethod.PAYMENT_METHOD_UNSPECIFIED
    }
    
    val event = PaymentCreatedEvent.newBuilder()
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
    val event = PaymentCompletedEvent.newBuilder()
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
    val event = PaymentFailedEvent.newBuilder()
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
    val event = PaymentCancelledEvent.newBuilder()
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
    val event = PaymentRefundedEvent.newBuilder()
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
