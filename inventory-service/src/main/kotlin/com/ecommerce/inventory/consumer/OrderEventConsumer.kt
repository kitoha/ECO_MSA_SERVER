package com.ecommerce.inventory.consumer

import com.ecommerce.inventory.service.InventoryReservationService
import com.ecommerce.proto.inventory.InventoryReservationRequest
import com.ecommerce.proto.inventory.ReservationFailedEvent as ProtoReservationFailedEvent
import com.ecommerce.proto.order.OrderCancelledEvent
import com.ecommerce.proto.order.OrderConfirmedEvent
import com.google.protobuf.Message
import com.google.protobuf.Timestamp
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component


@Component
class OrderEventConsumer(
    private val inventoryReservationService: InventoryReservationService,
    private val kafkaTemplate: KafkaTemplate<String, Message>
) {

    companion object {
        private val logger = LoggerFactory.getLogger(OrderEventConsumer::class.java)
    }

    @KafkaListener(
        topics = ["inventory-reservation-request"],
        groupId = "inventory-service",
        containerFactory = "inventoryReservationRequestListenerContainerFactory"
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

            val now = java.time.Instant.now()
            val failedEvent = ProtoReservationFailedEvent.newBuilder()
                .setOrderId(event.orderId)
                .setProductId(event.productId)
                .setQuantity(event.quantity)
                .setReason(e.message ?: "Unknown error")
                .setTimestamp(Timestamp.newBuilder()
                    .setSeconds(now.epochSecond)
                    .setNanos(now.nano)
                    .build())
                .build()
            kafkaTemplate.send("reservation-failed", event.orderId, failedEvent)
            logger.info("Published ReservationFailedEvent for order: ${event.orderId}")

            acknowledgment.acknowledge()
        }
    }

    @KafkaListener(
        topics = ["order-confirmed"],
        groupId = "inventory-service",
        containerFactory = "orderConfirmedEventListenerContainerFactory"
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
        containerFactory = "orderCancelledEventListenerContainerFactory"
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
