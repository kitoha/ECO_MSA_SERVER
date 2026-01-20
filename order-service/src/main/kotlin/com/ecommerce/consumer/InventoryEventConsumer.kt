package com.ecommerce.consumer

import com.ecommerce.event.ReservationFailedEvent
import com.ecommerce.service.OrderService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class InventoryEventConsumer(
    private val orderService: OrderService
) {

    private val logger = LoggerFactory.getLogger(InventoryEventConsumer::class.java)

    @KafkaListener(
        topics = ["reservation-failed"],
        groupId = "order-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleReservationFailed(event: ReservationFailedEvent, acknowledgment: Acknowledgment) {
        try {
            logger.warn(
                "Received ReservationFailedEvent: orderId=${event.orderId}, " +
                    "productId=${event.productId}, reason=${event.reason}"
            )

            val order = orderService.getOrderByOrderNumber(event.orderId)
            orderService.cancelOrder(order.id, "재고 예약 실패: ${event.reason}")

            acknowledgment.acknowledge()
            logger.info("Order cancelled due to reservation failure: ${event.orderId}")
        } catch (e: Exception) {
            logger.error("Failed to process ReservationFailedEvent: orderId=${event.orderId}", e)
            acknowledgment.acknowledge()
        }
    }
}
