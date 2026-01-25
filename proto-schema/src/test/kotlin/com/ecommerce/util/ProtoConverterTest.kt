package com.ecommerce.util

import com.ecommerce.dto.OrderCreatedEventDto
import com.ecommerce.dto.OrderItemDto
import com.ecommerce.dto.PaymentCompletedEventDto
import com.ecommerce.dto.ReservationCreatedEventDto
import com.ecommerce.proto.common.money
import com.ecommerce.proto.order.orderCreatedEvent
import com.ecommerce.proto.order.orderItem
import com.ecommerce.proto.payment.PaymentMethod
import com.ecommerce.proto.payment.paymentCompletedEvent
import com.ecommerce.proto.inventory.reservationCreatedEvent
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset

class ProtoConverterTest : BehaviorSpec({

    given("ProtoConverter for type conversions") {
        `when`("converting BigDecimal to Money proto") {
            val amount = BigDecimal("29.99")
            val currency = "KRW"

            val money = ProtoConverter.toMoneyProto(amount, currency)

            then("should convert correctly") {
                money.amount shouldBe "29.99"
                money.currency shouldBe "KRW"
            }
        }

        `when`("converting Money proto to BigDecimal") {
            val moneyProto = money {
                amount = "99.99"
                currency = "KRW"
            }

            val bigDecimal = ProtoConverter.fromMoneyProto(moneyProto)

            then("should convert correctly") {
                bigDecimal shouldBe BigDecimal("99.99")
            }
        }

        `when`("converting LocalDateTime to Timestamp proto") {
            val dateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45)

            val timestamp = ProtoConverter.toTimestampProto(dateTime)

            then("should convert correctly") {
                timestamp.seconds shouldBe dateTime.toEpochSecond(ZoneOffset.UTC)
                timestamp.nanos shouldBe dateTime.nano
            }
        }

        `when`("converting Timestamp proto to LocalDateTime") {
            val timestampProto = com.google.protobuf.timestamp {
                seconds = 1705315845L  // 2024-01-15 10:30:45 UTC
                nanos = 0
            }

            val dateTime = ProtoConverter.fromTimestampProto(timestampProto)

            then("should convert correctly") {
                dateTime shouldNotBe null
                dateTime.year shouldBe 2024
            }
        }
    }

    given("ProtoConverter for OrderCreatedEvent") {
        `when`("converting Kotlin OrderCreatedEvent to Proto") {
            // Kotlin event (현재 서비스에서 사용중)
            val kotlinEvent = MockOrderCreatedEvent(
                orderId = 12345L,
                orderNumber = "ORD-2024-001",
                userId = "user-123",
                items = listOf(
                    MockOrderItemData(
                        productId = "PROD-001",
                        productName = "Test Product",
                        price = BigDecimal("29.99"),
                        quantity = 2
                    )
                ),
                totalAmount = BigDecimal("59.98"),
                shippingAddress = "서울시 강남구",
                shippingName = "홍길동",
                shippingPhone = "010-1234-5678",
                timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 45)
            )

            val protoEvent = ProtoConverter.toOrderCreatedEventProto(kotlinEvent)

            then("should convert all fields correctly") {
                protoEvent.orderId shouldBe 12345L
                protoEvent.orderNumber shouldBe "ORD-2024-001"
                protoEvent.userId shouldBe "user-123"
                protoEvent.itemsCount shouldBe 1
                protoEvent.itemsList[0].productId shouldBe "PROD-001"
                protoEvent.itemsList[0].productName shouldBe "Test Product"
                protoEvent.itemsList[0].price.amount shouldBe "29.99"
                protoEvent.itemsList[0].quantity shouldBe 2
                protoEvent.totalAmount.amount shouldBe "59.98"
                protoEvent.shippingAddress shouldBe "서울시 강남구"
                protoEvent.shippingName shouldBe "홍길동"
                protoEvent.shippingPhone shouldBe "010-1234-5678"
                protoEvent.timestamp.seconds shouldNotBe 0L
            }
        }

        `when`("converting Proto OrderCreatedEvent to Kotlin") {
            val protoEvent = orderCreatedEvent {
                orderId = 99999L
                orderNumber = "ORD-2024-999"
                userId = "user-999"
                items.add(orderItem {
                    productId = "PROD-999"
                    productName = "Proto Product"
                    price = money { amount = "19.99"; currency = "KRW" }
                    quantity = 3
                })
                totalAmount = money { amount = "59.97"; currency = "KRW" }
                shippingAddress = "부산시 해운대구"
                shippingName = "김철수"
                shippingPhone = "010-9999-8888"
                timestamp = com.google.protobuf.timestamp {
                    seconds = 1705315845L
                    nanos = 0
                }
            }

            val kotlinEvent = ProtoConverter.fromOrderCreatedEventProto(protoEvent)

            then("should convert all fields correctly") {
                kotlinEvent.orderId shouldBe 99999L
                kotlinEvent.orderNumber shouldBe "ORD-2024-999"
                kotlinEvent.userId shouldBe "user-999"
                kotlinEvent.items.size shouldBe 1
                kotlinEvent.items[0].productId shouldBe "PROD-999"
                kotlinEvent.items[0].productName shouldBe "Proto Product"
                kotlinEvent.items[0].price shouldBe BigDecimal("19.99")
                kotlinEvent.items[0].quantity shouldBe 3
                kotlinEvent.totalAmount shouldBe BigDecimal("59.97")
                kotlinEvent.shippingAddress shouldBe "부산시 해운대구"
                kotlinEvent.shippingName shouldBe "김철수"
                kotlinEvent.shippingPhone shouldBe "010-9999-8888"
            }
        }
    }

    given("ProtoConverter for PaymentCompletedEvent") {
        `when`("converting Kotlin PaymentCompletedEvent to Proto") {
            val kotlinEvent = MockPaymentCompletedEvent(
                paymentId = 54321L,
                orderId = "ORD-2024-PAY-001",
                userId = "user-pay-123",
                amount = BigDecimal("100.00"),
                pgProvider = "TOSS_PAYMENTS",
                pgPaymentKey = "payment_abc123",
                timestamp = LocalDateTime.of(2024, 1, 20, 14, 30, 0)
            )

            val protoEvent = ProtoConverter.toPaymentCompletedEventProto(kotlinEvent)

            then("should convert all fields correctly") {
                protoEvent.paymentId shouldBe 54321L
                protoEvent.orderId shouldBe "ORD-2024-PAY-001"
                protoEvent.userId shouldBe "user-pay-123"
                protoEvent.amount.amount shouldBe "100.00"
                protoEvent.pgProvider shouldBe "TOSS_PAYMENTS"
                protoEvent.pgPaymentKey shouldBe "payment_abc123"
                protoEvent.timestamp.seconds shouldNotBe 0L
            }
        }

        `when`("converting Proto PaymentCompletedEvent to Kotlin") {
            val protoEvent = paymentCompletedEvent {
                paymentId = 77777L
                orderId = "ORD-2024-PAY-999"
                userId = "user-pay-999"
                amount = money { amount = "200.00"; currency = "KRW" }
                pgProvider = "NICE_PAYMENTS"
                pgPaymentKey = "payment_xyz999"
                timestamp = com.google.protobuf.timestamp {
                    seconds = 1705758600L
                    nanos = 0
                }
            }

            val kotlinEvent = ProtoConverter.fromPaymentCompletedEventProto(protoEvent)

            then("should convert all fields correctly") {
                kotlinEvent.paymentId shouldBe 77777L
                kotlinEvent.orderId shouldBe "ORD-2024-PAY-999"
                kotlinEvent.userId shouldBe "user-pay-999"
                kotlinEvent.amount shouldBe BigDecimal("200.00")
                kotlinEvent.pgProvider shouldBe "NICE_PAYMENTS"
                kotlinEvent.pgPaymentKey shouldBe "payment_xyz999"
            }
        }
    }

    given("ProtoConverter for ReservationCreatedEvent") {
        `when`("converting Kotlin ReservationCreatedEvent to Proto") {
            val kotlinEvent = MockReservationCreatedEvent(
                reservationId = 11111L,
                orderId = "ORD-2024-RES-001",
                productId = "PROD-RES-001",
                quantity = 5,
                expiresAt = LocalDateTime.of(2024, 1, 25, 23, 59, 59),
                timestamp = LocalDateTime.of(2024, 1, 25, 10, 0, 0)
            )

            val protoEvent = ProtoConverter.toReservationCreatedEventProto(kotlinEvent)

            then("should convert all fields correctly") {
                protoEvent.reservationId shouldBe 11111L
                protoEvent.orderId shouldBe "ORD-2024-RES-001"
                protoEvent.productId shouldBe "PROD-RES-001"
                protoEvent.quantity shouldBe 5
                protoEvent.expiresAt.seconds shouldNotBe 0L
                protoEvent.timestamp.seconds shouldNotBe 0L
            }
        }

        `when`("converting Proto ReservationCreatedEvent to Kotlin") {
            val protoEvent = reservationCreatedEvent {
                reservationId = 22222L
                orderId = "ORD-2024-RES-999"
                productId = "PROD-RES-999"
                quantity = 10
                expiresAt = com.google.protobuf.timestamp {
                    seconds = 1706227199L  // 2024-01-25 23:59:59
                    nanos = 0
                }
                timestamp = com.google.protobuf.timestamp {
                    seconds = 1706176800L  // 2024-01-25 10:00:00
                    nanos = 0
                }
            }

            val kotlinEvent = ProtoConverter.fromReservationCreatedEventProto(protoEvent)

            then("should convert all fields correctly") {
                kotlinEvent.reservationId shouldBe 22222L
                kotlinEvent.orderId shouldBe "ORD-2024-RES-999"
                kotlinEvent.productId shouldBe "PROD-RES-999"
                kotlinEvent.quantity shouldBe 10
                kotlinEvent.expiresAt shouldNotBe null
            }
        }
    }

    given("ProtoConverter edge cases") {
        `when`("converting BigDecimal with high precision") {
            val highPrecision = BigDecimal("123.456789012345")

            val money = ProtoConverter.toMoneyProto(highPrecision, "KRW")
            val converted = ProtoConverter.fromMoneyProto(money)

            then("should preserve precision") {
                converted shouldBe highPrecision
            }
        }

        `when`("converting BigDecimal zero") {
            val zero = BigDecimal.ZERO

            val money = ProtoConverter.toMoneyProto(zero, "KRW")
            val converted = ProtoConverter.fromMoneyProto(money)

            then("should handle zero correctly") {
                converted shouldBe BigDecimal.ZERO
            }
        }
    }
})

// Mock data classes for testing (실제 서비스 이벤트 구조 모방)
data class MockOrderCreatedEvent(
    val orderId: Long,
    val orderNumber: String,
    val userId: String,
    val items: List<MockOrderItemData>,
    val totalAmount: BigDecimal,
    val shippingAddress: String,
    val shippingName: String,
    val shippingPhone: String,
    val timestamp: LocalDateTime
)

data class MockOrderItemData(
    val productId: String,
    val productName: String,
    val price: BigDecimal,
    val quantity: Int
)

data class MockPaymentCompletedEvent(
    val paymentId: Long,
    val orderId: String,
    val userId: String,
    val amount: BigDecimal,
    val pgProvider: String,
    val pgPaymentKey: String,
    val timestamp: LocalDateTime
)

data class MockReservationCreatedEvent(
    val reservationId: Long,
    val orderId: String,
    val productId: String,
    val quantity: Int,
    val expiresAt: LocalDateTime,
    val timestamp: LocalDateTime
)
