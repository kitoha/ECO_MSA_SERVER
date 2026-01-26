package com.ecommerce.util

import com.ecommerce.dto.OrderCreatedEventDto
import com.ecommerce.dto.OrderItemDto
import com.ecommerce.proto.order.OrderCreatedEvent
import com.ecommerce.proto.order.orderCreatedEvent
import com.ecommerce.proto.order.orderItem
import java.math.BigDecimal
import java.time.LocalDateTime

object OrderEventConverter {

    fun <T> toOrderCreatedEventProto(event: T): OrderCreatedEvent where
            T : Any = orderCreatedEvent {
        orderId = ProtoTypeConverter.getField<Long>(event, "orderId")
        orderNumber = ProtoTypeConverter.getField<String>(event, "orderNumber")
        userId = ProtoTypeConverter.getField<String>(event, "userId")

        val itemsList = ProtoTypeConverter.getField<List<*>>(event, "items")
        itemsList.forEach { item ->
            items.add(orderItem {
                productId = ProtoTypeConverter.getField<String>(item!!, "productId")
                productName = ProtoTypeConverter.getField<String>(item, "productName")
                price = ProtoTypeConverter.toMoneyProto(ProtoTypeConverter.getField<BigDecimal>(item, "price"))
                quantity = ProtoTypeConverter.getField<Int>(item, "quantity")
            })
        }

        totalAmount = ProtoTypeConverter.toMoneyProto(ProtoTypeConverter.getField<BigDecimal>(event, "totalAmount"))
        shippingAddress = ProtoTypeConverter.getField<String>(event, "shippingAddress")
        shippingName = ProtoTypeConverter.getField<String>(event, "shippingName")
        shippingPhone = ProtoTypeConverter.getField<String>(event, "shippingPhone")
        timestamp = ProtoTypeConverter.toTimestampProto(ProtoTypeConverter.getField<LocalDateTime>(event, "timestamp"))
    }

    fun <T> fromOrderCreatedEventProto(proto: OrderCreatedEvent, factory: (Map<String, Any>) -> T): T {
        val fields = mapOf(
            "orderId" to proto.orderId,
            "orderNumber" to proto.orderNumber,
            "userId" to proto.userId,
            "items" to proto.itemsList.map { item ->
                mapOf(
                    "productId" to item.productId,
                    "productName" to item.productName,
                    "price" to ProtoTypeConverter.fromMoneyProto(item.price),
                    "quantity" to item.quantity
                )
            },
            "totalAmount" to ProtoTypeConverter.fromMoneyProto(proto.totalAmount),
            "shippingAddress" to proto.shippingAddress,
            "shippingName" to proto.shippingName,
            "shippingPhone" to proto.shippingPhone,
            "timestamp" to ProtoTypeConverter.fromTimestampProto(proto.timestamp)
        )

        return factory(fields)
    }

    fun fromOrderCreatedEventProto(proto: OrderCreatedEvent): OrderCreatedEventDto =
        OrderCreatedEventDto(
            orderId = proto.orderId,
            orderNumber = proto.orderNumber,
            userId = proto.userId,
            items = proto.itemsList.map { item ->
                OrderItemDto(
                    productId = item.productId,
                    productName = item.productName,
                    price = ProtoTypeConverter.fromMoneyProto(item.price),
                    quantity = item.quantity
                )
            },
            totalAmount = ProtoTypeConverter.fromMoneyProto(proto.totalAmount),
            shippingAddress = proto.shippingAddress,
            shippingName = proto.shippingName,
            shippingPhone = proto.shippingPhone,
            timestamp = ProtoTypeConverter.fromTimestampProto(proto.timestamp)
        )
}
