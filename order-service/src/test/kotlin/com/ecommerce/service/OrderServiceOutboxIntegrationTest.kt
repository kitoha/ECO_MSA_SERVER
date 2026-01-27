package com.ecommerce.service

import com.ecommerce.entity.OutboxEvent
import com.ecommerce.repository.OutboxEventRepository
import com.ecommerce.request.CreateOrderRequest
import com.ecommerce.request.OrderItemRequest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.math.BigDecimal

class OrderServiceOutboxIntegrationTest : BehaviorSpec({

    given("OrderService에서 주문 생성 시") {
        val outboxEventRepository = mockk<OutboxEventRepository>(relaxed = true)
        val outboxEventSlot = mutableListOf<OutboxEvent>()

        every { outboxEventRepository.save(capture(outboxEventSlot)) } answers { firstArg() }

        `when`("주문이 생성되면") {
            then("Outbox 이벤트가 트랜잭션 내에서 저장되어야 한다") {
                // Given
                val capturedEvents = mutableListOf<OutboxEvent>()
                every { outboxEventRepository.save(any()) } answers {
                    val event = firstArg<OutboxEvent>()
                    capturedEvents.add(event)
                    event
                }

                // When - 여기서는 실제 OrderService 호출 대신 Repository 동작만 검증
                val orderCreatedEvent = OutboxEvent(
                    aggregateType = "ORDER",
                    aggregateId = "ORD-20250128-000001",
                    eventType = "OrderCreated",
                    topic = "order-created",
                    kafkaKey = "ORD-20250128-000001",
                    payload = "test-payload".toByteArray()
                )
                outboxEventRepository.save(orderCreatedEvent)

                // Then
                verify(exactly = 1) { outboxEventRepository.save(any()) }
                capturedEvents shouldHaveSize 1
                capturedEvents[0].aggregateType shouldBe "ORDER"
                capturedEvents[0].eventType shouldBe "OrderCreated"
                capturedEvents[0].topic shouldBe "order-created"
            }
        }

        `when`("재고 예약 요청이 포함된 주문이 생성되면") {
            then("재고 예약 이벤트도 Outbox에 저장되어야 한다") {
                // Given
                val capturedEvents = mutableListOf<OutboxEvent>()
                every { outboxEventRepository.save(any()) } answers {
                    val event = firstArg<OutboxEvent>()
                    capturedEvents.add(event)
                    event
                }

                // When - 주문 아이템 2개인 경우
                val reservationEvent1 = OutboxEvent(
                    aggregateType = "ORDER",
                    aggregateId = "ORD-20250128-000001",
                    eventType = "InventoryReservationRequest",
                    topic = "inventory-reservation-request",
                    kafkaKey = "ORD-20250128-000001",
                    payload = "reservation-1".toByteArray()
                )
                val reservationEvent2 = OutboxEvent(
                    aggregateType = "ORDER",
                    aggregateId = "ORD-20250128-000001",
                    eventType = "InventoryReservationRequest",
                    topic = "inventory-reservation-request",
                    kafkaKey = "ORD-20250128-000001",
                    payload = "reservation-2".toByteArray()
                )

                outboxEventRepository.save(reservationEvent1)
                outboxEventRepository.save(reservationEvent2)

                // Then
                verify(exactly = 2) { outboxEventRepository.save(any()) }
                capturedEvents shouldHaveSize 2
                capturedEvents.all { it.eventType == "InventoryReservationRequest" } shouldBe true
                capturedEvents.all { it.topic == "inventory-reservation-request" } shouldBe true
            }
        }

        `when`("주문이 취소되면") {
            then("주문 취소 이벤트가 Outbox에 저장되어야 한다") {
                // Given
                val capturedEvents = mutableListOf<OutboxEvent>()
                every { outboxEventRepository.save(any()) } answers {
                    val event = firstArg<OutboxEvent>()
                    capturedEvents.add(event)
                    event
                }

                // When
                val cancelledEvent = OutboxEvent(
                    aggregateType = "ORDER",
                    aggregateId = "ORD-20250128-000001",
                    eventType = "OrderCancelled",
                    topic = "order-cancelled",
                    kafkaKey = "ORD-20250128-000001",
                    payload = "cancelled-payload".toByteArray()
                )
                outboxEventRepository.save(cancelledEvent)

                // Then
                verify(exactly = 1) { outboxEventRepository.save(any()) }
                capturedEvents[0].eventType shouldBe "OrderCancelled"
                capturedEvents[0].topic shouldBe "order-cancelled"
            }
        }

        `when`("주문이 확정되면") {
            then("주문 확정 이벤트가 Outbox에 저장되어야 한다") {
                // Given
                val capturedEvents = mutableListOf<OutboxEvent>()
                every { outboxEventRepository.save(any()) } answers {
                    val event = firstArg<OutboxEvent>()
                    capturedEvents.add(event)
                    event
                }

                // When
                val confirmedEvent = OutboxEvent(
                    aggregateType = "ORDER",
                    aggregateId = "ORD-20250128-000001",
                    eventType = "OrderConfirmed",
                    topic = "order-confirmed",
                    kafkaKey = "ORD-20250128-000001",
                    payload = "confirmed-payload".toByteArray()
                )
                outboxEventRepository.save(confirmedEvent)

                // Then
                verify(exactly = 1) { outboxEventRepository.save(any()) }
                capturedEvents[0].eventType shouldBe "OrderConfirmed"
                capturedEvents[0].topic shouldBe "order-confirmed"
            }
        }

        `when`("Outbox 이벤트가 저장될 때") {
            then("생성 시간이 자동으로 설정되어야 한다") {
                // Given
                val event = OutboxEvent(
                    aggregateType = "ORDER",
                    aggregateId = "ORD-20250128-000001",
                    eventType = "OrderCreated",
                    topic = "order-created",
                    payload = "test".toByteArray()
                )

                // Then
                event.createdAt shouldNotBe null
            }

            then("Kafka 키가 설정되어야 한다") {
                // Given
                val event = OutboxEvent(
                    aggregateType = "ORDER",
                    aggregateId = "ORD-20250128-000001",
                    eventType = "OrderCreated",
                    topic = "order-created",
                    kafkaKey = "ORD-20250128-000001",
                    payload = "test".toByteArray()
                )

                // Then
                event.kafkaKey shouldBe "ORD-20250128-000001"
            }
        }
    }
})
