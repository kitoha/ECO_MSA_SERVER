package com.ecommerce.proto.payment

import com.ecommerce.proto.common.money
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.ints.shouldBeLessThan

class PaymentEventsProtoTest : BehaviorSpec({

    given("PaymentCompletedEvent proto message") {
        `when`("creating with all required fields") {
            val event = paymentCompletedEvent {
                paymentId = 12345L
                orderId = "ORD-2024-001"
                userId = "user-123"
                amount = money {
                    amount = "29.99"
                    currency = "KRW"
                }
                pgProvider = "TOSS_PAYMENTS"
                pgPaymentKey = "payment_key_abc123xyz"
                timestamp = com.google.protobuf.timestamp {
                    seconds = 1640995200
                }
                metadata = com.ecommerce.proto.common.metadata {
                    traceId = "trace-123"
                    serviceName = "payment-service"
                    version = 1
                }
            }

            then("should have correct payment ID") {
                event.paymentId shouldBe 12345L
            }

            then("should have correct order ID") {
                event.orderId shouldBe "ORD-2024-001"
            }

            then("should have correct amount") {
                event.amount.amount shouldBe "29.99"
            }

            then("should have PG provider info") {
                event.pgProvider shouldBe "TOSS_PAYMENTS"
                event.pgPaymentKey shouldBe "payment_key_abc123xyz"
            }

            then("should be serializable") {
                val bytes = event.toByteArray()
                bytes.size shouldNotBe 0
            }

            then("should be deserializable") {
                val bytes = event.toByteArray()
                val deserialized = PaymentCompletedEvent.parseFrom(bytes)
                deserialized.paymentId shouldBe 12345L
                deserialized.orderId shouldBe "ORD-2024-001"
            }

            then("should be compact") {
                val bytes = event.toByteArray()
                bytes.size shouldBeLessThan 200
            }
        }
    }

    given("PaymentCreatedEvent proto message") {
        `when`("creating with payment method") {
            val event = paymentCreatedEvent {
                paymentId = 99999L
                orderId = "ORD-2024-002"
                userId = "user-456"
                amount = money {
                    amount = "100.00"
                    currency = "KRW"
                }
                paymentMethod = PaymentMethod.CARD
                timestamp = com.google.protobuf.timestamp {
                    seconds = 1640995200
                }
                metadata = com.ecommerce.proto.common.metadata {
                    traceId = "trace-456"
                    serviceName = "payment-service"
                    version = 1
                }
            }

            then("should have correct payment method") {
                event.paymentMethod shouldBe PaymentMethod.CARD
            }

            then("should be serializable and deserializable") {
                val bytes = event.toByteArray()
                val deserialized = PaymentCreatedEvent.parseFrom(bytes)
                deserialized.paymentMethod shouldBe PaymentMethod.CARD
                deserialized.orderId shouldBe "ORD-2024-002"
            }
        }

        `when`("creating with EASY_PAY method") {
            val event = paymentCreatedEvent {
                paymentId = 88888L
                orderId = "ORD-2024-003"
                userId = "user-789"
                amount = money { amount = "50.00"; currency = "KRW" }
                paymentMethod = PaymentMethod.EASY_PAY
            }

            then("should support EASY_PAY") {
                event.paymentMethod shouldBe PaymentMethod.EASY_PAY
            }
        }
    }

    given("PaymentFailedEvent proto message") {
        `when`("creating with failure reason") {
            val event = paymentFailedEvent {
                paymentId = 12345L
                orderId = "ORD-2024-001"
                userId = "user-123"
                amount = money { amount = "29.99"; currency = "KRW" }
                failureReason = "Insufficient funds"
                timestamp = com.google.protobuf.timestamp {
                    seconds = 1640995200
                }
                metadata = com.ecommerce.proto.common.metadata {
                    traceId = "trace-123"
                    serviceName = "payment-service"
                    version = 1
                }
            }

            then("should have failure reason") {
                event.failureReason shouldBe "Insufficient funds"
            }

            then("should be serializable and deserializable") {
                val bytes = event.toByteArray()
                val deserialized = PaymentFailedEvent.parseFrom(bytes)
                deserialized.failureReason shouldBe "Insufficient funds"
            }
        }
    }

    given("PaymentCancelledEvent proto message") {
        `when`("creating with cancellation reason") {
            val event = paymentCancelledEvent {
                paymentId = 12345L
                orderId = "ORD-2024-001"
                userId = "user-123"
                amount = money { amount = "29.99"; currency = "KRW" }
                reason = "사용자 요청"
                timestamp = com.google.protobuf.timestamp {
                    seconds = 1640995200
                }
                metadata = com.ecommerce.proto.common.metadata {
                    traceId = "trace-123"
                    serviceName = "payment-service"
                    version = 1
                }
            }

            then("should have cancellation reason") {
                event.reason shouldBe "사용자 요청"
            }

            then("should be serializable and deserializable") {
                val bytes = event.toByteArray()
                val deserialized = PaymentCancelledEvent.parseFrom(bytes)
                deserialized.reason shouldBe "사용자 요청"
            }
        }
    }

    given("PaymentRefundedEvent proto message") {
        `when`("creating with refund reason") {
            val event = paymentRefundedEvent {
                paymentId = 12345L
                orderId = "ORD-2024-001"
                userId = "user-123"
                amount = money { amount = "29.99"; currency = "KRW" }
                reason = "상품 불량"
                timestamp = com.google.protobuf.timestamp {
                    seconds = 1640995200
                }
                metadata = com.ecommerce.proto.common.metadata {
                    traceId = "trace-123"
                    serviceName = "payment-service"
                    version = 1
                }
            }

            then("should have refund reason") {
                event.reason shouldBe "상품 불량"
            }

            then("should be serializable and deserializable") {
                val bytes = event.toByteArray()
                val deserialized = PaymentRefundedEvent.parseFrom(bytes)
                deserialized.reason shouldBe "상품 불량"
            }

            then("should be compact") {
                val bytes = event.toByteArray()
                bytes.size shouldBeLessThan 150
            }
        }
    }

    given("PaymentMethod enum") {
        `when`("using all payment methods") {
            then("should support CARD") {
                PaymentMethod.CARD shouldNotBe null
            }

            then("should support BANK_TRANSFER") {
                PaymentMethod.BANK_TRANSFER shouldNotBe null
            }

            then("should support VIRTUAL_ACCOUNT") {
                PaymentMethod.VIRTUAL_ACCOUNT shouldNotBe null
            }

            then("should support EASY_PAY") {
                PaymentMethod.EASY_PAY shouldNotBe null
            }

            then("should support MOBILE") {
                PaymentMethod.MOBILE shouldNotBe null
            }
        }
    }
})
