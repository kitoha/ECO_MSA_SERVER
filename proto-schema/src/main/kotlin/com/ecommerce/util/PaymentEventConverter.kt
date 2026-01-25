package com.ecommerce.util

import com.ecommerce.dto.PaymentCompletedEventDto
import com.ecommerce.proto.payment.PaymentCompletedEvent
import com.ecommerce.proto.payment.paymentCompletedEvent
import java.math.BigDecimal
import java.time.LocalDateTime

object PaymentEventConverter {

    fun <T> toPaymentCompletedEventProto(event: T): PaymentCompletedEvent where
            T : Any = paymentCompletedEvent {
        paymentId = ProtoTypeConverter.getField<Long>(event, "paymentId")
        orderId = ProtoTypeConverter.getField<String>(event, "orderId")
        userId = ProtoTypeConverter.getField<String>(event, "userId")
        amount = ProtoTypeConverter.toMoneyProto(ProtoTypeConverter.getField<BigDecimal>(event, "amount"))
        pgProvider = ProtoTypeConverter.getField<String>(event, "pgProvider")
        pgPaymentKey = ProtoTypeConverter.getField<String>(event, "pgPaymentKey")
        timestamp = ProtoTypeConverter.toTimestampProto(ProtoTypeConverter.getField<LocalDateTime>(event, "timestamp"))
    }

    fun fromPaymentCompletedEventProto(proto: PaymentCompletedEvent): PaymentCompletedEventDto =
        PaymentCompletedEventDto(
            paymentId = proto.paymentId,
            orderId = proto.orderId,
            userId = proto.userId,
            amount = ProtoTypeConverter.fromMoneyProto(proto.amount),
            pgProvider = proto.pgProvider,
            pgPaymentKey = proto.pgPaymentKey,
            timestamp = ProtoTypeConverter.fromTimestampProto(proto.timestamp)
        )
}
