package com.ecommerce.util

import com.ecommerce.proto.common.Money
import com.ecommerce.proto.common.money
import com.google.protobuf.Timestamp
import com.google.protobuf.timestamp
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Protobuf 기본 타입 변환 유틸리티
 *
 * BigDecimal, LocalDateTime 등 기본 타입과 Protobuf 메시지 간 변환을 담당합니다.
 */
object ProtoTypeConverter {

    /**
     * BigDecimal을 Money proto로 변환
     *
     * BigDecimal을 string으로 변환하여 정밀도를 보존합니다.
     */
    fun toMoneyProto(amount: BigDecimal, currency: String = "KRW"): Money = money {
        this.amount = amount.toPlainString()
        this.currency = currency
    }

    /**
     * Money proto를 BigDecimal로 변환
     */
    fun fromMoneyProto(money: Money): BigDecimal =
        BigDecimal(money.amount)

    /**
     * LocalDateTime을 Timestamp proto로 변환
     *
     * UTC 기준으로 변환합니다.
     */
    fun toTimestampProto(dateTime: LocalDateTime): Timestamp = timestamp {
        val instant = dateTime.toInstant(ZoneOffset.UTC)
        seconds = instant.epochSecond
        nanos = instant.nano
    }

    /**
     * Timestamp proto를 LocalDateTime으로 변환
     *
     * UTC 기준으로 변환합니다.
     */
    fun fromTimestampProto(timestamp: Timestamp): LocalDateTime {
        val instant = Instant.ofEpochSecond(timestamp.seconds, timestamp.nanos.toLong())
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
    }

    /**
     * Reflection을 사용하여 객체의 필드 값 가져오기
     *
     * 내부 유틸리티 메서드로, 다른 컨버터에서 사용됩니다.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun <T> getField(obj: Any, fieldName: String): T {
        val kClass = obj::class
        val property = kClass.members.find { it.name == fieldName }
            ?: throw IllegalArgumentException("Field $fieldName not found in ${kClass.simpleName}")

        return property.call(obj) as T
    }
}
