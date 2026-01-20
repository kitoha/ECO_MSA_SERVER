package com.ecommerce.consumer

import com.ecommerce.enums.PaymentMethod
import com.ecommerce.event.OrderCancelledEvent
import com.ecommerce.event.OrderCreatedEvent
import com.ecommerce.request.CreatePaymentRequest
import com.ecommerce.service.PaymentCommandService
import com.ecommerce.service.PaymentQueryService
import com.ecommerce.util.sanitizeForLog
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer(
    private val commandService: PaymentCommandService,
    private val queryService: PaymentQueryService
) {

  private val logger = LoggerFactory.getLogger(OrderEventConsumer::class.java)

  @KafkaListener(
    topics = ["order-created"],
    groupId = "payment-service",
    containerFactory = "kafkaListenerContainerFactory"
  )
  fun handleOrderCreated(event: OrderCreatedEvent, acknowledgment: Acknowledgment) {
    try {
      logger.info("Received OrderCreatedEvent: ${event.orderNumber.sanitizeForLog()}")

      val request = CreatePaymentRequest(
        orderId = event.orderNumber,
        userId = event.userId,
        amount = event.totalAmount,
        paymentMethod = PaymentMethod.CARD
      )

      commandService.createPayment(request)

      acknowledgment.acknowledge()
      logger.info("Payment created for order: ${event.orderNumber.sanitizeForLog()}")
    } catch (e: Exception) {
      logger.error("Failed to process OrderCreatedEvent: ${event.orderNumber.sanitizeForLog()}", e)
    }
  }

  @KafkaListener(
      topics = ["order-cancelled"],
      groupId = "payment-service",
      containerFactory = "kafkaListenerContainerFactory"
  )
  fun handleOrderCancelled(event: OrderCancelledEvent, acknowledgment: Acknowledgment) {
    try {
      logger.info("Received OrderCancelledEvent: ${event.orderNumber.sanitizeForLog()}, reason: ${event.reason}")

      val payment = queryService.getPaymentByOrderId(event.orderNumber)
      commandService.cancelPayment(payment.id, event.reason)

      acknowledgment.acknowledge()
      logger.info("Payment cancelled for order: ${event.orderNumber.sanitizeForLog()}")
    } catch (e: Exception) {
      logger.error("Failed to process OrderCancelledEvent: ${event.orderNumber.sanitizeForLog()}", e)
      acknowledgment.acknowledge()
    }
  }
}
