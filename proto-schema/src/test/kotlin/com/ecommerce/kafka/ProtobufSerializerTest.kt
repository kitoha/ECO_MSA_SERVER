package com.ecommerce.kafka

import com.ecommerce.proto.order.orderCreatedEvent
import com.ecommerce.proto.common.money
import com.ecommerce.proto.order.orderItem
import com.google.protobuf.timestamp
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ProtobufSerializerTest : BehaviorSpec({

    given("ProtobufSerializer") {
        val serializer = ProtobufSerializer<com.ecommerce.proto.order.OrderCreatedEvent>()

        `when`("serializing a Protobuf message") {
            val event = orderCreatedEvent {
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

            val bytes = serializer.serialize("test-topic", event)

            then("should produce non-null bytes") {
                bytes shouldNotBe null
                bytes!!.size shouldNotBe 0
            }

            then("should be smaller than JSON") {
                bytes!!.size shouldNotBe 0
            }
        }

        `when`("serializing null") {
            val bytes = serializer.serialize("test-topic", null)

            then("should produce null") {
                bytes shouldBe null
            }
        }
    }

    given("ProtobufSerializer configuration") {
        val serializer = ProtobufSerializer<com.ecommerce.proto.order.OrderCreatedEvent>()

        `when`("configuring serializer") {
            val configs = mutableMapOf<String, Any>()
            serializer.configure(configs, false)

            then("should configure without error") {
            }
        }

        `when`("closing serializer") {
            serializer.close()

            then("should close without error") {
            }
        }
    }
})
