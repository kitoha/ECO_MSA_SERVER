package com.ecommerce.inventory.consumer

import com.ecommerce.inventory.event.ReservationCancelledEvent
import com.ecommerce.inventory.service.InventoryReservationService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class ReservationCancelConsumer(
    private val inventoryReservationService: InventoryReservationService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(ReservationCancelConsumer::class.java)
    }

    @KafkaListener(
        topics = ["reservation-cancel"],
        groupId = "inventory-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun consumeCancelEvent(
        event: ReservationCancelledEvent,
        acknowledgment: Acknowledgment
    ) {
        try {
            logger.info("Received reservation cancel event: reservationId=${event.reservationId}, reason=${event.reason}")

            inventoryReservationService.cancelReservation(event.reservationId)
            acknowledgment.acknowledge()

            logger.info("Successfully processed cancel event for reservation: ${event.reservationId}")

        } catch (e: Exception) {
            logger.error("Failed to process cancel event: reservationId=${event.reservationId}", e)
            throw e
        }
    }
}
