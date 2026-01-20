package com.ecommerce.consumer

import com.ecommerce.event.PaymentCompletedEvent
import com.ecommerce.event.PaymentFailedEvent
import com.ecommerce.service.OrderService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class PaymentEventConsumer(
    private val orderService: OrderService
) {

    private val logger = LoggerFactory.getLogger(PaymentEventConsumer::class.java)

    @KafkaListener(
        topics = ["payment-completed"],
        groupId = "order-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handlePaymentCompleted(event: PaymentCompletedEvent, acknowledgment: Acknowledgment) {
        try {
            logger.info("Received PaymentCompletedEvent: orderId=${event.orderId}, paymentId=${event.paymentId}")

            val order = orderService.getOrderByOrderNumber(event.orderId)
            orderService.confirmOrder(order.id)

            acknowledgment.acknowledge()
            logger.info("Order confirmed after payment: ${event.orderId}")
        } catch (e: Exception) {
            logger.error("Failed to process PaymentCompletedEvent: orderId=${event.orderId}", e)
        }
    }

    @KafkaListener(
        topics = ["payment-failed"],
        groupId = "order-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handlePaymentFailed(event: PaymentFailedEvent, acknowledgment: Acknowledgment) {
        try {
            logger.info("Received PaymentFailedEvent: orderId=${event.orderId}, reason=${event.failureReason}")

            val order = orderService.getOrderByOrderNumber(event.orderId)
            orderService.cancelOrder(order.id, "결제 실패: ${event.failureReason}")

            acknowledgment.acknowledge()
            logger.info("Order cancelled after payment failure: ${event.orderId}")
        } catch (e: Exception) {
            logger.error("Failed to process PaymentFailedEvent: orderId=${event.orderId}", e)
        }
    }
}
