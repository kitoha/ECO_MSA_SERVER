package com.ecommerce.consumer

import com.ecommerce.event.ReservationCreatedEvent
import com.ecommerce.event.ReservationFailedEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class InventoryEventConsumer {

    private val logger = LoggerFactory.getLogger(InventoryEventConsumer::class.java)

    @KafkaListener(
        topics = ["reservation-created"],
        groupId = "order-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleReservationCreated(event: ReservationCreatedEvent, acknowledgment: Acknowledgment) {
        try {
            logger.info(
                "Received ReservationCreatedEvent: orderId=${event.orderId}, " +
                    "productId=${event.productId}, reservationId=${event.reservationId}"
            )

            acknowledgment.acknowledge()
        } catch (e: Exception) {
            logger.error("Failed to process ReservationCreatedEvent: orderId=${event.orderId}", e)
        }
    }

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

            acknowledgment.acknowledge()
        } catch (e: Exception) {
            logger.error("Failed to process ReservationFailedEvent: orderId=${event.orderId}", e)
        }
    }
}
