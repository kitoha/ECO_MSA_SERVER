package com.ecommerce.consumer

import com.ecommerce.enums.PaymentMethod
import com.ecommerce.event.OrderCreatedEvent
import com.ecommerce.request.CreatePaymentRequest
import com.ecommerce.service.PaymentService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer(
  private val paymentService: PaymentService
) {

  private val logger = LoggerFactory.getLogger(OrderEventConsumer::class.java)

  @KafkaListener(
    topics = ["order-created"],
    groupId = "payment-service",
    containerFactory = "kafkaListenerContainerFactory"
  )
  fun handleOrderCreated(event: OrderCreatedEvent, acknowledgment: Acknowledgment) {
    try {
      logger.info("Received OrderCreatedEvent: ${event.orderNumber}")

      val request = CreatePaymentRequest(
        orderId = event.orderNumber,
        userId = event.userId,
        amount = event.totalAmount,
        paymentMethod = PaymentMethod.CARD
      )

      paymentService.createPayment(request)

      acknowledgment.acknowledge()
      logger.info("Payment created for order: ${event.orderNumber}")
    } catch (e: Exception) {
      logger.error("Failed to process OrderCreatedEvent: ${event.orderNumber}", e)
    }
  }
}
