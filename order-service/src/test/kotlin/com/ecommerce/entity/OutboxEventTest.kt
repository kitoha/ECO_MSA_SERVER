package com.ecommerce.entity

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.LocalDateTime

class OutboxEventTest : BehaviorSpec({

    given("OutboxEvent 엔티티가 주어졌을 때") {

        `when`("필수 필드로 생성하면") {
            then("정상적으로 생성되어야 한다") {
                val payload = "test-payload".toByteArray()

                val outboxEvent = OutboxEvent(
                    aggregateType = "ORDER",
                    aggregateId = "ORD-20250128-000001",
                    eventType = "OrderCreated",
                    topic = "order-created",
                    kafkaKey = "ORD-20250128-000001",
                    payload = payload
                )

                outboxEvent.aggregateType shouldBe "ORDER"
                outboxEvent.aggregateId shouldBe "ORD-20250128-000001"
                outboxEvent.eventType shouldBe "OrderCreated"
                outboxEvent.topic shouldBe "order-created"
                outboxEvent.kafkaKey shouldBe "ORD-20250128-000001"
                outboxEvent.payload shouldBe payload
                outboxEvent.createdAt shouldNotBe null
            }
        }

        `when`("kafkaKey 없이 생성하면") {
            then("kafkaKey가 null이어야 한다") {
                val outboxEvent = OutboxEvent(
                    aggregateType = "ORDER",
                    aggregateId = "ORD-20250128-000001",
                    eventType = "OrderCreated",
                    topic = "order-created",
                    payload = "test".toByteArray()
                )

                outboxEvent.kafkaKey shouldBe null
            }
        }

        `when`("생성 시간을 확인하면") {
            then("현재 시간으로 자동 설정되어야 한다") {
                val before = LocalDateTime.now()

                val outboxEvent = OutboxEvent(
                    aggregateType = "ORDER",
                    aggregateId = "ORD-20250128-000001",
                    eventType = "OrderCreated",
                    topic = "order-created",
                    payload = "test".toByteArray()
                )

                val after = LocalDateTime.now()

                outboxEvent.createdAt shouldNotBe null
                (outboxEvent.createdAt.isAfter(before.minusSeconds(1)) &&
                 outboxEvent.createdAt.isBefore(after.plusSeconds(1))) shouldBe true
            }
        }
    }
})
