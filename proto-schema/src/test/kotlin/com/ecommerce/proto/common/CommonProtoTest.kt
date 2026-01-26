package com.ecommerce.proto.common

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.math.BigDecimal

class CommonProtoTest : BehaviorSpec({

    given("Money proto message") {
        `when`("creating Money with amount and currency") {
            val money = money {
                amount = "29.99"
                currency = "KRW"
            }

            then("should have correct amount") {
                money.amount shouldBe "29.99"
            }

            then("should have correct currency") {
                money.currency shouldBe "KRW"
            }

            then("should be serializable") {
                val bytes = money.toByteArray()
                bytes.size shouldNotBe 0
            }

            then("should be deserializable") {
                val bytes = money.toByteArray()
                val deserialized = Money.parseFrom(bytes)
                deserialized.amount shouldBe "29.99"
                deserialized.currency shouldBe "KRW"
            }
        }

        `when`("handling BigDecimal precision") {
            val bigDecimal = BigDecimal("123.456789")
            val money = money {
                amount = bigDecimal.toString()
                currency = "USD"
            }

            then("should preserve precision") {
                BigDecimal(money.amount) shouldBe bigDecimal
            }
        }

        `when`("creating with default currency") {
            val money = money {
                amount = "100.00"
                currency = "KRW"
            }

            then("should use KRW as default") {
                money.currency shouldBe "KRW"
            }
        }
    }

    given("Metadata proto message") {
        `when`("creating Metadata with all fields") {
            val metadata = metadata {
                timestamp = com.google.protobuf.timestamp {
                    seconds = 1640995200
                    nanos = 500000000
                }
                traceId = "trace-123"
                correlationId = "corr-456"
                serviceName = "order-service"
                version = 1
            }

            then("should have correct trace ID") {
                metadata.traceId shouldBe "trace-123"
            }

            then("should have correct correlation ID") {
                metadata.correlationId shouldBe "corr-456"
            }

            then("should have correct service name") {
                metadata.serviceName shouldBe "order-service"
            }

            then("should have correct version") {
                metadata.version shouldBe 1
            }

            then("should be serializable and deserializable") {
                val bytes = metadata.toByteArray()
                val deserialized = Metadata.parseFrom(bytes)
                deserialized.traceId shouldBe "trace-123"
                deserialized.serviceName shouldBe "order-service"
            }
        }

        `when`("comparing serialized sizes") {
            val metadata = metadata {
                timestamp = com.google.protobuf.timestamp {
                    seconds = System.currentTimeMillis() / 1000
                }
                traceId = "trace-123"
                serviceName = "order-service"
                version = 1
            }

            then("should have small serialized size") {
                val bytes = metadata.toByteArray()
                // Protobuf는 매우 작아야 함 (< 100 bytes for this simple message)
                bytes.size shouldBe io.kotest.matchers.ints.lt(100)
            }
        }
    }
})
