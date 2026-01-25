package com.ecommerce.util

import com.ecommerce.dto.ReservationCreatedEventDto
import com.ecommerce.proto.inventory.ReservationCreatedEvent
import com.ecommerce.proto.inventory.reservationCreatedEvent
import java.time.LocalDateTime

object InventoryEventConverter {

    fun <T> toReservationCreatedEventProto(event: T): ReservationCreatedEvent where
            T : Any = reservationCreatedEvent {
        reservationId = ProtoTypeConverter.getField<Long>(event, "reservationId")
        orderId = ProtoTypeConverter.getField<String>(event, "orderId")
        productId = ProtoTypeConverter.getField<String>(event, "productId")
        quantity = ProtoTypeConverter.getField<Int>(event, "quantity")
        expiresAt = ProtoTypeConverter.toTimestampProto(ProtoTypeConverter.getField<LocalDateTime>(event, "expiresAt"))
        timestamp = ProtoTypeConverter.toTimestampProto(ProtoTypeConverter.getField<LocalDateTime>(event, "timestamp"))
    }

    fun fromReservationCreatedEventProto(proto: ReservationCreatedEvent): ReservationCreatedEventDto =
        ReservationCreatedEventDto(
            reservationId = proto.reservationId,
            orderId = proto.orderId,
            productId = proto.productId,
            quantity = proto.quantity,
            expiresAt = ProtoTypeConverter.fromTimestampProto(proto.expiresAt),
            timestamp = ProtoTypeConverter.fromTimestampProto(proto.timestamp)
        )
}
