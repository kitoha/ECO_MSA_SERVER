package com.ecommerce.service

import com.ecommerce.entity.Order
import com.ecommerce.enums.OrderStatus
import com.ecommerce.generator.TsidGenerator
import com.ecommerce.proto.inventory.InventoryReservationRequest
import com.ecommerce.proto.order.OrderCancelledEvent
import com.ecommerce.proto.order.OrderConfirmedEvent
import com.ecommerce.repository.OrderRepository
import com.ecommerce.request.CreateOrderRequest
import com.ecommerce.response.OrderItemResponse
import com.ecommerce.response.OrderResponse
import com.ecommerce.util.OrderNumberGenerator
import com.google.protobuf.Message
import com.google.protobuf.Timestamp
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
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
    private val protoKafkaTemplate: KafkaTemplate<String, Message>,
    private val orderNumberGenerator: OrderNumberGenerator,
    private val idGenerator: TsidGenerator
) {

    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    private fun createMoneyProto(amount: java.math.BigDecimal): com.ecommerce.proto.common.Money {
        return com.ecommerce.proto.common.Money.newBuilder()
            .setAmount(amount.toString())
            .setCurrency("KRW")
            .build()
    }

    private fun createOrderItemProto(item: com.ecommerce.dto.OrderItemDto): com.ecommerce.proto.order.OrderItem {
        return com.ecommerce.proto.order.OrderItem.newBuilder()
            .setProductId(item.productId)
            .setProductName(item.productName)
            .setPrice(createMoneyProto(item.price))
            .setQuantity(item.quantity)
            .build()
    }

    private fun createOrderCreatedEventProto(
        order: Order,
        orderItems: List<com.ecommerce.dto.OrderItemDto>
    ): com.ecommerce.proto.order.OrderCreatedEvent {
        val now = Instant.now()
        return com.ecommerce.proto.order.OrderCreatedEvent.newBuilder()
            .setOrderId(order.id)
            .setOrderNumber(order.orderNumber)
            .setUserId(order.userId)
            .addAllItems(orderItems.map { createOrderItemProto(it) })
            .setTotalAmount(createMoneyProto(order.totalAmount))
            .setShippingAddress(order.shippingAddress)
            .setShippingName(order.shippingName)
            .setShippingPhone(order.shippingPhone)
            .setTimestamp(Timestamp.newBuilder()
                .setSeconds(now.epochSecond)
                .setNanos(now.nano)
                .build())
            .build()
    }

    @Transactional
    fun createOrder(request: CreateOrderRequest, userId: String): OrderResponse {
        val totalAmount = orderItemService.calculateOrderTotal(request.items)

        val order = Order(
            id = idGenerator.generate(),
            orderNumber = orderNumberGenerator.generate(),
            userId = userId,
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
            val now = Instant.now()
            val reservationRequest = InventoryReservationRequest.newBuilder()
                .setOrderId(savedOrder.orderNumber)
                .setProductId(itemRequest.productId)
                .setQuantity(itemRequest.quantity)
                .setTimestamp(Timestamp.newBuilder()
                    .setSeconds(now.epochSecond)
                    .setNanos(now.nano)
                    .build())
                .build()
            protoKafkaTemplate.send("inventory-reservation-request", savedOrder.orderNumber, reservationRequest)
            logger.info("Sent inventory reservation request for product: ${itemRequest.productId}")
        }

        val protoEvent = createOrderCreatedEventProto(savedOrder, orderItems)
        protoKafkaTemplate.send("order-created", savedOrder.orderNumber, protoEvent)
        logger.info("Published Protobuf event to order-created: ${savedOrder.orderNumber}")

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
    fun getOrderByOrderNumber(orderNumber: String): OrderResponse {
        val order = orderRepository.findByOrderNumber(orderNumber)
            ?: throw IllegalArgumentException("존재하지 않는 주문입니다: $orderNumber")

        val orderItems = orderItemService.getOrderItems(order.id)

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

        val event = OrderCancelledEvent.newBuilder()
            .setOrderId(order.id)
            .setOrderNumber(order.orderNumber)
            .setUserId(order.userId)
            .setReason(reason)
            .setTimestamp(
                Timestamp.newBuilder()
                .setSeconds(Instant.now().epochSecond)
                .setNanos(Instant.now().nano)
                .build())
            .build()
        protoKafkaTemplate.send("order-cancelled", order.orderNumber, event)

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

        val event = OrderConfirmedEvent.newBuilder()
            .setOrderId(order.id)
            .setOrderNumber(order.orderNumber)
            .setUserId(order.userId)
            .setTimestamp(Timestamp.newBuilder()
                .setSeconds(Instant.now().epochSecond)
                .setNanos(Instant.now().nano)
                .build())
            .build()
        protoKafkaTemplate.send("order-confirmed", order.orderNumber, event)

        logger.info("Order confirmed: ${order.orderNumber}")
    }
}