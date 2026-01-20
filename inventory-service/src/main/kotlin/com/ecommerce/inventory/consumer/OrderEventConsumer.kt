package com.ecommerce.inventory.consumer

import com.ecommerce.inventory.event.InventoryReservationRequest
import com.ecommerce.inventory.event.OrderCancelledEvent
import com.ecommerce.inventory.event.OrderConfirmedEvent
import com.ecommerce.inventory.event.ReservationFailedEvent
import com.ecommerce.inventory.service.InventoryReservationService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component


@Component
class OrderEventConsumer(
    private val inventoryReservationService: InventoryReservationService,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    companion object {
        private val logger = LoggerFactory.getLogger(OrderEventConsumer::class.java)
    }

    @KafkaListener(
        topics = ["inventory-reservation-request"],
        groupId = "inventory-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleInventoryReservationRequest(
        event: InventoryReservationRequest,
        acknowledgment: Acknowledgment
    ) {
        try {
            logger.info(
                "Received InventoryReservationRequest: orderId=${event.orderId}, " +
                    "productId=${event.productId}, quantity=${event.quantity}"
            )

            inventoryReservationService.createReservation(
                orderId = event.orderId,
                productId = event.productId,
                quantity = event.quantity
            )

            acknowledgment.acknowledge()
            logger.info("Inventory reserved successfully for order: ${event.orderId}")
        } catch (e: Exception) {
            logger.error(
                "Failed to process InventoryReservationRequest: orderId=${event.orderId}, " +
                    "productId=${event.productId}",
                e
            )

            val failedEvent = ReservationFailedEvent(
                orderId = event.orderId,
                productId = event.productId,
                quantity = event.quantity,
                reason = e.message ?: "Unknown error"
            )
            kafkaTemplate.send("reservation-failed", event.orderId, failedEvent)
            logger.info("Published ReservationFailedEvent for order: ${event.orderId}")

            acknowledgment.acknowledge()
        }
    }

    @KafkaListener(
        topics = ["order-confirmed"],
        groupId = "inventory-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleOrderConfirmed(event: OrderConfirmedEvent, acknowledgment: Acknowledgment) {
        try {
            logger.info("Received OrderConfirmedEvent: orderNumber=${event.orderNumber}")

            inventoryReservationService.confirmReservationsByOrderId(event.orderNumber)

            acknowledgment.acknowledge()
            logger.info("Reservations confirmed for order: ${event.orderNumber}")
        } catch (e: Exception) {
            logger.error("Failed to process OrderConfirmedEvent: orderNumber=${event.orderNumber}", e)
        }
    }

    @KafkaListener(
        topics = ["order-cancelled"],
        groupId = "inventory-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleOrderCancelled(event: OrderCancelledEvent, acknowledgment: Acknowledgment) {
        try {
            logger.info("Received OrderCancelledEvent: orderNumber=${event.orderNumber}, reason=${event.reason}")

            inventoryReservationService.cancelReservationsByOrderId(event.orderNumber)

            acknowledgment.acknowledge()
            logger.info("Reservations cancelled for order: ${event.orderNumber}")
        } catch (e: Exception) {
            logger.error("Failed to process OrderCancelledEvent: orderNumber=${event.orderNumber}", e)
        }
    }
}
