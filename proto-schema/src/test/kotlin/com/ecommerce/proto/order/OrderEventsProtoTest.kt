package com.ecommerce.proto.order

import com.ecommerce.proto.common.money
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeLessThan

class OrderEventsProtoTest : BehaviorSpec({

    given("OrderCreatedEvent proto message") {
        `when`("creating with all required fields") {
            val event = orderCreatedEvent {
                orderId = 12345L
                orderNumber = "ORD-2024-001"
                userId = "user-123"
                items.add(orderItem {
                    productId = "P-100"
                    productName = "Test Product"
                    price = money {
                        amount = "29.99"
                        currency = "KRW"
                    }
                    quantity = 2
                })
                totalAmount = money {
                    amount = "59.98"
                    currency = "KRW"
                }
                shippingAddress = "서울시 강남구"
                shippingName = "홍길동"
                shippingPhone = "010-1234-5678"
                timestamp = com.google.protobuf.timestamp {
                    seconds = 1640995200
                }
                metadata = com.ecommerce.proto.common.metadata {
                    traceId = "trace-123"
                    serviceName = "order-service"
                    version = 1
                }
            }

            then("should have correct order ID") {
                event.orderId shouldBe 12345L
            }

            then("should have correct order number") {
                event.orderNumber shouldBe "ORD-2024-001"
            }

            then("should have correct user ID") {
                event.userId shouldBe "user-123"
            }

            then("should have order items") {
                event.itemsList shouldHaveSize 1
                event.itemsList[0].productId shouldBe "P-100"
                event.itemsList[0].quantity shouldBe 2
            }

            then("should have correct total amount") {
                event.totalAmount.amount shouldBe "59.98"
            }

            then("should be serializable") {
                val bytes = event.toByteArray()
                bytes.size shouldNotBe 0
            }

            then("should be deserializable") {
                val bytes = event.toByteArray()
                val deserialized = OrderCreatedEvent.parseFrom(bytes)
                deserialized.orderId shouldBe 12345L
                deserialized.orderNumber shouldBe "ORD-2024-001"
            }

            then("should have smaller size than JSON") {
                val bytes = event.toByteArray()
                // Protobuf는 JSON보다 작아야 함 (대략 < 300 bytes)
                bytes.size shouldBeLessThan 300
            }
        }

        `when`("creating with multiple items") {
            val event = orderCreatedEvent {
                orderId = 99999L
                orderNumber = "ORD-2024-002"
                userId = "user-456"
                items.addAll(listOf(
                    orderItem {
                        productId = "P-100"
                        productName = "Product 1"
                        price = money { amount = "10.00"; currency = "KRW" }
                        quantity = 1
                    },
                    orderItem {
                        productId = "P-200"
                        productName = "Product 2"
                        price = money { amount = "20.00"; currency = "KRW" }
                        quantity = 2
                    }
                ))
                totalAmount = money { amount = "50.00"; currency = "KRW" }
                shippingAddress = "부산시"
                shippingName = "김철수"
                shippingPhone = "010-9876-5432"
            }

            then("should have multiple items") {
                event.itemsList shouldHaveSize 2
                event.itemsList[0].productId shouldBe "P-100"
                event.itemsList[1].productId shouldBe "P-200"
            }
        }
    }

    given("OrderConfirmedEvent proto message") {
        `when`("creating confirmed event") {
            val event = orderConfirmedEvent {
                orderId = 12345L
                orderNumber = "ORD-2024-001"
                userId = "user-123"
                timestamp = com.google.protobuf.timestamp {
                    seconds = 1640995200
                }
                metadata = com.ecommerce.proto.common.metadata {
                    traceId = "trace-123"
                    serviceName = "order-service"
                    version = 1
                }
            }

            then("should have correct fields") {
                event.orderId shouldBe 12345L
                event.orderNumber shouldBe "ORD-2024-001"
                event.userId shouldBe "user-123"
            }

            then("should be serializable and deserializable") {
                val bytes = event.toByteArray()
                val deserialized = OrderConfirmedEvent.parseFrom(bytes)
                deserialized.orderNumber shouldBe "ORD-2024-001"
            }

            then("should be very small") {
                val bytes = event.toByteArray()
                bytes.size shouldBeLessThan 150
            }
        }
    }

    given("OrderCancelledEvent proto message") {
        `when`("creating cancelled event") {
            val event = orderCancelledEvent {
                orderId = 12345L
                orderNumber = "ORD-2024-001"
                userId = "user-123"
                reason = "고객 요청"
                timestamp = com.google.protobuf.timestamp {
                    seconds = 1640995200
                }
                metadata = com.ecommerce.proto.common.metadata {
                    traceId = "trace-123"
                    serviceName = "order-service"
                    version = 1
                }
            }

            then("should have cancellation reason") {
                event.reason shouldBe "고객 요청"
            }

            then("should be serializable and deserializable") {
                val bytes = event.toByteArray()
                val deserialized = OrderCancelledEvent.parseFrom(bytes)
                deserialized.reason shouldBe "고객 요청"
                deserialized.orderNumber shouldBe "ORD-2024-001"
            }
        }
    }

    given("OrderItem proto message") {
        `when`("creating order item") {
            val item = orderItem {
                productId = "P-999"
                productName = "테스트 상품"
                price = money {
                    amount = "123.45"
                    currency = "KRW"
                }
                quantity = 5
            }

            then("should have correct product info") {
                item.productId shouldBe "P-999"
                item.productName shouldBe "테스트 상품"
                item.quantity shouldBe 5
            }

            then("should have correct price") {
                item.price.amount shouldBe "123.45"
            }
        }
    }
})
