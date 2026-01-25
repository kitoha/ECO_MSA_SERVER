package com.ecommerce.proto.inventory

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.ints.shouldBeLessThan

class InventoryEventsProtoTest : BehaviorSpec({

    given("InventoryReservationRequest proto message") {
        `when`("creating reservation request") {
            val request = inventoryReservationRequest {
                orderId = "ORD-2024-001"
                productId = "P-100"
                quantity = 5
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
                request.orderId shouldBe "ORD-2024-001"
            }

            then("should have correct product ID") {
                request.productId shouldBe "P-100"
            }

            then("should have correct quantity") {
                request.quantity shouldBe 5
            }

            then("should be serializable") {
                val bytes = request.toByteArray()
                bytes.size shouldNotBe 0
            }

            then("should be deserializable") {
                val bytes = request.toByteArray()
                val deserialized = InventoryReservationRequest.parseFrom(bytes)
                deserialized.orderId shouldBe "ORD-2024-001"
                deserialized.productId shouldBe "P-100"
            }

            then("should be compact") {
                val bytes = request.toByteArray()
                bytes.size shouldBeLessThan 150
            }
        }
    }

    given("ReservationCreatedEvent proto message") {
        `when`("creating reservation created event") {
            val event = reservationCreatedEvent {
                reservationId = 12345L
                orderId = "ORD-2024-001"
                productId = "P-100"
                quantity = 5
                expiresAt = com.google.protobuf.timestamp {
                    seconds = 1640995200 + 900
                }
                timestamp = com.google.protobuf.timestamp {
                    seconds = 1640995200
                }
                metadata = com.ecommerce.proto.common.metadata {
                    traceId = "trace-123"
                    serviceName = "inventory-service"
                    version = 1
                }
            }

            then("should have correct reservation ID") {
                event.reservationId shouldBe 12345L
            }

            then("should have expiration time") {
                event.expiresAt.seconds shouldBe 1640995200 + 900
            }

            then("should be serializable and deserializable") {
                val bytes = event.toByteArray()
                val deserialized = ReservationCreatedEvent.parseFrom(bytes)
                deserialized.reservationId shouldBe 12345L
                deserialized.orderId shouldBe "ORD-2024-001"
            }
        }
    }

    given("ReservationConfirmedEvent proto message") {
        `when`("creating reservation confirmed event") {
            val event = reservationConfirmedEvent {
                reservationId = 12345L
                orderId = "ORD-2024-001"
                timestamp = com.google.protobuf.timestamp {
                    seconds = 1640995200
                }
                metadata = com.ecommerce.proto.common.metadata {
                    traceId = "trace-123"
                    serviceName = "inventory-service"
                    version = 1
                }
            }

            then("should have correct fields") {
                event.reservationId shouldBe 12345L
                event.orderId shouldBe "ORD-2024-001"
            }

            then("should be serializable and deserializable") {
                val bytes = event.toByteArray()
                val deserialized = ReservationConfirmedEvent.parseFrom(bytes)
                deserialized.reservationId shouldBe 12345L
            }

            then("should be very compact") {
                val bytes = event.toByteArray()
                bytes.size shouldBeLessThan 100
            }
        }
    }

    given("ReservationCancelledEvent proto message") {
        `when`("creating reservation cancelled event") {
            val event = reservationCancelledEvent {
                reservationId = 12345L
                reason = "재고 부족"
                timestamp = com.google.protobuf.timestamp {
                    seconds = 1640995200
                }
                metadata = com.ecommerce.proto.common.metadata {
                    traceId = "trace-123"
                    serviceName = "inventory-service"
                    version = 1
                }
            }

            then("should have cancellation reason") {
                event.reason shouldBe "재고 부족"
            }

            then("should be serializable and deserializable") {
                val bytes = event.toByteArray()
                val deserialized = ReservationCancelledEvent.parseFrom(bytes)
                deserialized.reason shouldBe "재고 부족"
            }
        }
    }

    given("ReservationFailedEvent proto message") {
        `when`("creating reservation failed event") {
            val event = reservationFailedEvent {
                orderId = "ORD-2024-001"
                productId = "P-100"
                quantity = 10
                reason = "재고 부족"
                timestamp = com.google.protobuf.timestamp {
                    seconds = 1640995200
                }
                metadata = com.ecommerce.proto.common.metadata {
                    traceId = "trace-123"
                    serviceName = "inventory-service"
                    version = 1
                }
            }

            then("should have failure details") {
                event.orderId shouldBe "ORD-2024-001"
                event.productId shouldBe "P-100"
                event.quantity shouldBe 10
                event.reason shouldBe "재고 부족"
            }

            then("should be serializable and deserializable") {
                val bytes = event.toByteArray()
                val deserialized = ReservationFailedEvent.parseFrom(bytes)
                deserialized.reason shouldBe "재고 부족"
                deserialized.orderId shouldBe "ORD-2024-001"
            }
        }
    }
})
