package com.ecommerce.util

import com.ecommerce.dto.OrderCreatedEventDto
import com.ecommerce.dto.PaymentCompletedEventDto
import com.ecommerce.dto.ReservationCreatedEventDto
import com.ecommerce.proto.common.Money
import com.ecommerce.proto.order.OrderCreatedEvent
import com.ecommerce.proto.payment.PaymentCompletedEvent
import com.ecommerce.proto.inventory.ReservationCreatedEvent
import com.google.protobuf.Timestamp
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Protobuf 변환 통합 인터페이스
 *
 * 모든 Protobuf 변환 기능에 대한 단일 진입점을 제공합니다.
 * 실제 변환 로직은 도메인별 컨버터에 위임됩니다.
 *
 * 사용 예:
 * ```
 * val protoEvent = ProtoConverter.toOrderCreatedEventProto(kotlinEvent)
 * val kotlinEvent = ProtoConverter.fromOrderCreatedEventProto(protoEvent)
 * ```
 */
object ProtoConverter {

    // ============================================
    // 기본 타입 변환 (ProtoTypeConverter에 위임)
    // ============================================

    fun toMoneyProto(amount: BigDecimal, currency: String = "KRW"): Money =
        ProtoTypeConverter.toMoneyProto(amount, currency)

    fun fromMoneyProto(money: Money): BigDecimal =
        ProtoTypeConverter.fromMoneyProto(money)

    fun toTimestampProto(dateTime: LocalDateTime): Timestamp =
        ProtoTypeConverter.toTimestampProto(dateTime)

    fun fromTimestampProto(timestamp: Timestamp): LocalDateTime =
        ProtoTypeConverter.fromTimestampProto(timestamp)

    // ============================================
    // Order 이벤트 변환 (OrderEventConverter에 위임)
    // ============================================

    fun <T> toOrderCreatedEventProto(event: T): OrderCreatedEvent where T : Any =
        OrderEventConverter.toOrderCreatedEventProto(event)

    fun <T> fromOrderCreatedEventProto(proto: OrderCreatedEvent, factory: (Map<String, Any>) -> T): T =
        OrderEventConverter.fromOrderCreatedEventProto(proto, factory)

    fun fromOrderCreatedEventProto(proto: OrderCreatedEvent): OrderCreatedEventDto =
        OrderEventConverter.fromOrderCreatedEventProto(proto)

    // ============================================
    // Payment 이벤트 변환 (PaymentEventConverter에 위임)
    // ============================================

    fun <T> toPaymentCompletedEventProto(event: T): PaymentCompletedEvent where T : Any =
        PaymentEventConverter.toPaymentCompletedEventProto(event)

    fun fromPaymentCompletedEventProto(proto: PaymentCompletedEvent): PaymentCompletedEventDto =
        PaymentEventConverter.fromPaymentCompletedEventProto(proto)

    // ============================================
    // Inventory 이벤트 변환 (InventoryEventConverter에 위임)
    // ============================================

    fun <T> toReservationCreatedEventProto(event: T): ReservationCreatedEvent where T : Any =
        InventoryEventConverter.toReservationCreatedEventProto(event)

    fun fromReservationCreatedEventProto(proto: ReservationCreatedEvent): ReservationCreatedEventDto =
        InventoryEventConverter.fromReservationCreatedEventProto(proto)
}
