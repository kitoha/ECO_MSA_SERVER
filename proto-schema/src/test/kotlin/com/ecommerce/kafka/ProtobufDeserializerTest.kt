package com.ecommerce.kafka

import com.ecommerce.proto.order.OrderCreatedEvent
import com.ecommerce.proto.order.orderCreatedEvent
import com.ecommerce.proto.common.money
import com.ecommerce.proto.order.orderItem
import com.google.protobuf.timestamp
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ProtobufDeserializerTest : BehaviorSpec({

    given("ProtobufDeserializer") {
        val serializer = ProtobufSerializer<OrderCreatedEvent>()
        val deserializer = ProtobufDeserializer(OrderCreatedEvent::class.java)

        `when`("deserializing a Protobuf message") {
            val originalEvent = orderCreatedEvent {
                orderId = 12345L
                orderNumber = "ORD-2024-001"
                userId = "user-123"
                items.add(orderItem {
                    productId = "PROD-001"
                    productName = "Test Product"
                    price = money { amount = "29.99"; currency = "KRW" }
                    quantity = 2
                })
                totalAmount = money { amount = "59.98"; currency = "KRW" }
                shippingAddress = "서울시 강남구"
                shippingName = "홍길동"
                shippingPhone = "010-1234-5678"
                timestamp = timestamp {
                    seconds = 1640995200L
                    nanos = 0
                }
            }

            val bytes = serializer.serialize("test-topic", originalEvent)

            val deserializedEvent = deserializer.deserialize("test-topic", bytes)

            then("should deserialize correctly") {
                deserializedEvent shouldNotBe null
                deserializedEvent!!.orderId shouldBe 12345L
                deserializedEvent.orderNumber shouldBe "ORD-2024-001"
                deserializedEvent.userId shouldBe "user-123"
                deserializedEvent.itemsCount shouldBe 1
                deserializedEvent.itemsList[0].productId shouldBe "PROD-001"
                deserializedEvent.itemsList[0].productName shouldBe "Test Product"
                deserializedEvent.itemsList[0].price.amount shouldBe "29.99"
                deserializedEvent.itemsList[0].quantity shouldBe 2
                deserializedEvent.totalAmount.amount shouldBe "59.98"
                deserializedEvent.shippingAddress shouldBe "서울시 강남구"
                deserializedEvent.shippingName shouldBe "홍길동"
                deserializedEvent.shippingPhone shouldBe "010-1234-5678"
            }
        }

        `when`("deserializing null") {
            val result = deserializer.deserialize("test-topic", null)

            then("should return null") {
                result shouldBe null
            }
        }

        `when`("deserializing empty bytes") {
            val result = deserializer.deserialize("test-topic", ByteArray(0))

            then("should return null") {
                result shouldBe null
            }
        }
    }

    given("ProtobufDeserializer configuration") {
        val deserializer = ProtobufDeserializer(OrderCreatedEvent::class.java)

        `when`("configuring deserializer") {
            val configs = mutableMapOf<String, Any>()
            deserializer.configure(configs, false)

            then("should configure without error") {
            }
        }

        `when`("closing deserializer") {
            deserializer.close()

            then("should close without error") {
            }
        }
    }
})
