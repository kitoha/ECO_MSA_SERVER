package com.ecommerce.service

import com.ecommerce.entity.Order
import com.ecommerce.enums.OrderStatus
import com.ecommerce.event.InventoryReservationRequest
import com.ecommerce.event.OrderCancelledEvent
import com.ecommerce.event.OrderConfirmedEvent
import com.ecommerce.event.OrderCreatedEvent
import com.ecommerce.event.OrderItemData
import com.ecommerce.generator.TsidGenerator
import com.ecommerce.repository.OrderRepository
import com.ecommerce.request.CreateOrderRequest
import com.ecommerce.response.OrderItemResponse
import com.ecommerce.response.OrderResponse
import com.ecommerce.util.OrderNumberGenerator
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 주문 서비스
 *
 * 주문 생성, 조회, 상태 관리를 담당
 */
@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemService: OrderItemService,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val orderNumberGenerator: OrderNumberGenerator,
    private val idGenerator: TsidGenerator
) {

    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    @Transactional
    fun createOrder(request: CreateOrderRequest): OrderResponse {
        logger.info("Creating order for user: ${request.userId}")

        val totalAmount = orderItemService.calculateOrderTotal(request.items)

        val order = Order(
            id = idGenerator.generate(),
            orderNumber = orderNumberGenerator.generate(),
            userId = request.userId,
            totalAmount = totalAmount,
            shippingAddress = request.shippingAddress,
            shippingName = request.shippingName,
            shippingPhone = request.shippingPhone,
            status = OrderStatus.PENDING,
            orderedAt = LocalDateTime.now()
        )

        val savedOrder = orderRepository.save(order)

        request.items.forEach { itemRequest ->
            orderItemService.addOrderItem(savedOrder.id, itemRequest)
        }

        val orderItems = orderItemService.getOrderItems(savedOrder.id)

        request.items.forEach { itemRequest ->
            val reservationRequest = InventoryReservationRequest(
                orderId = savedOrder.orderNumber,
                productId = itemRequest.productId.toString(),
                quantity = itemRequest.quantity.toInt()
            )
            kafkaTemplate.send("inventory-reservation-request", savedOrder.orderNumber, reservationRequest)
            logger.info("Sent inventory reservation request for product: ${itemRequest.productId}")
        }

        val orderCreatedEvent = OrderCreatedEvent(
            orderId = savedOrder.id,
            orderNumber = savedOrder.orderNumber,
            userId = savedOrder.userId,
            items = orderItems.map {
                OrderItemData(
                    productId = it.productId,
                    productName = it.productName,
                    price = it.price,
                    quantity = it.quantity
                )
            },
            totalAmount = savedOrder.totalAmount,
            shippingAddress = savedOrder.shippingAddress,
            shippingName = savedOrder.shippingName,
            shippingPhone = savedOrder.shippingPhone
        )
        kafkaTemplate.send("order-created", savedOrder.orderNumber, orderCreatedEvent)
        logger.info("Order created successfully: ${savedOrder.orderNumber}")

        return OrderResponse(
            id = savedOrder.id,
            orderNumber = savedOrder.orderNumber,
            userId = savedOrder.userId,
            status = savedOrder.status,
            items = orderItems.map { OrderItemResponse.from(it) },
            totalAmount = savedOrder.totalAmount,
            shippingAddress = savedOrder.shippingAddress,
            shippingName = savedOrder.shippingName,
            shippingPhone = savedOrder.shippingPhone,
            orderedAt = savedOrder.orderedAt,
            createdAt = savedOrder.createdAt,
            updatedAt = savedOrder.updatedAt
        )
    }

    @Transactional(readOnly = true)
    fun getOrder(orderId: Long): OrderResponse {
        val order = orderRepository.findById(orderId)
            ?: throw IllegalArgumentException("존재하지 않는 주문입니다: $orderId")

        val orderItems = orderItemService.getOrderItems(orderId)

        return OrderResponse(
            id = order.id,
            orderNumber = order.orderNumber,
            userId = order.userId,
            status = order.status,
            items = orderItems.map { OrderItemResponse.from(it) },
            totalAmount = order.totalAmount,
            shippingAddress = order.shippingAddress,
            shippingName = order.shippingName,
            shippingPhone = order.shippingPhone,
            orderedAt = order.orderedAt,
            createdAt = order.createdAt,
            updatedAt = order.updatedAt
        )
    }

    @Transactional(readOnly = true)
    fun getOrdersByUser(userId: String): List<OrderResponse> {
        val orders = orderRepository.findByUserId(userId)

        return orders.map { order ->
            val orderItems = orderItemService.getOrderItems(order.id)
            OrderResponse(
                id = order.id,
                orderNumber = order.orderNumber,
                userId = order.userId,
                status = order.status,
                items = orderItems.map { OrderItemResponse.from(it) },
                totalAmount = order.totalAmount,
                shippingAddress = order.shippingAddress,
                shippingName = order.shippingName,
                shippingPhone = order.shippingPhone,
                orderedAt = order.orderedAt,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
        }
    }

    @Transactional
    fun cancelOrder(orderId: Long, reason: String = "사용자 요청") {
        val order = orderRepository.findById(orderId)
            ?: throw IllegalArgumentException("존재하지 않는 주문입니다: $orderId")

        order.cancel()
        orderRepository.save(order)

        val event = OrderCancelledEvent(
            orderId = order.id,
            orderNumber = order.orderNumber,
            userId = order.userId,
            reason = reason
        )
        kafkaTemplate.send("order-cancelled", order.orderNumber, event)

        logger.info("Order cancelled: ${order.orderNumber}, reason: $reason")
    }

    @Transactional
    fun updateOrderStatus(orderId: Long, status: OrderStatus) {
        val order = orderRepository.findById(orderId)
            ?: throw IllegalArgumentException("존재하지 않는 주문입니다: $orderId")

        order.changeStatus(status)
        orderRepository.save(order)

        logger.info("Order status updated: ${order.orderNumber}, new status: $status")
    }

    @Transactional
    fun confirmOrder(orderId: Long) {
        val order = orderRepository.findById(orderId)
            ?: throw IllegalArgumentException("존재하지 않는 주문입니다: $orderId")

        order.confirm()
        orderRepository.save(order)

        val event = OrderConfirmedEvent(
            orderId = order.id,
            orderNumber = order.orderNumber,
            userId = order.userId
        )
        kafkaTemplate.send("order-confirmed", order.orderNumber, event)

        logger.info("Order confirmed: ${order.orderNumber}")
    }
}