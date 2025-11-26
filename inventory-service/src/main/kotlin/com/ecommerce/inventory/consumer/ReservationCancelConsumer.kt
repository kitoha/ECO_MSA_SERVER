package com.ecommerce.inventory.consumer

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
        event: Map<String, Any>,
        acknowledgment: Acknowledgment
    ) {
        try {
            val reservationId = (event["reservationId"] as Number).toLong()
            val reason = event["reason"] as? String ?: "UNKNOWN"

            logger.info("Received reservation cancel event: reservationId=$reservationId, reason=$reason")

            inventoryReservationService.cancelReservation(reservationId)
            acknowledgment.acknowledge()

            logger.info("Successfully processed cancel event for reservation: $reservationId")

        } catch (e: Exception) {
            logger.error("Failed to process cancel event: $event", e)
            throw e
        }
    }
}
