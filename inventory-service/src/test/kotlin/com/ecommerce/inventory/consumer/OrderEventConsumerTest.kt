package com.ecommerce.inventory.consumer

import com.ecommerce.inventory.entity.InventoryReservation
import com.ecommerce.inventory.enums.ReservationStatus
import com.ecommerce.inventory.event.InventoryReservationRequest
import com.ecommerce.inventory.event.OrderCancelledEvent
import com.ecommerce.inventory.event.OrderConfirmedEvent
import com.ecommerce.inventory.event.ReservationFailedEvent
import com.ecommerce.inventory.service.InventoryReservationService
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import java.time.LocalDateTime

class OrderEventConsumerTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val inventoryReservationService = mockk<InventoryReservationService>()
    val kafkaTemplate = mockk<KafkaTemplate<String, Any>>()
    val acknowledgment = mockk<Acknowledgment>()

    val consumer = OrderEventConsumer(inventoryReservationService, kafkaTemplate)

    beforeEach {
        clearMocks(inventoryReservationService, kafkaTemplate, acknowledgment, answers = false)
        every { acknowledgment.acknowledge() } just runs
    }

    given("InventoryReservationRequest가 수신되었을 때") {
        val event = InventoryReservationRequest(
            orderId = "ORDER-001",
            productId = "PRODUCT-001",
            quantity = 5
        )

        `when`("재고가 충분하면") {
            val reservation = InventoryReservation(
                id = 1L,
                inventoryId = 100L,
                orderId = event.orderId,
                quantity = event.quantity,
                status = ReservationStatus.ACTIVE,
                expiresAt = LocalDateTime.now().plusMinutes(15)
            )
            every {
                inventoryReservationService.createReservation(
                    orderId = event.orderId,
                    productId = event.productId,
                    quantity = event.quantity
                )
            } returns reservation

            then("예약이 생성되고 acknowledge가 호출되어야 한다") {
                consumer.handleInventoryReservationRequest(event, acknowledgment)

                verify(exactly = 1) {
                    inventoryReservationService.createReservation(
                        orderId = event.orderId,
                        productId = event.productId,
                        quantity = event.quantity
                    )
                }
                verify(exactly = 1) { acknowledgment.acknowledge() }
                verify(exactly = 0) { kafkaTemplate.send(any(), any(), any()) }
            }
        }

        `when`("재고가 부족하면") {
            every {
                inventoryReservationService.createReservation(
                    orderId = event.orderId,
                    productId = event.productId,
                    quantity = event.quantity
                )
            } throws IllegalArgumentException("재고가 부족합니다")
            every { kafkaTemplate.send(any<String>(), any<String>(), any()) } returns mockk()

            then("예약 실패 이벤트가 발행되고 acknowledge가 호출되어야 한다") {
                consumer.handleInventoryReservationRequest(event, acknowledgment)

                verify(exactly = 1) {
                    inventoryReservationService.createReservation(
                        orderId = event.orderId,
                        productId = event.productId,
                        quantity = event.quantity
                    )
                }
                verify(exactly = 1) {
                    kafkaTemplate.send(
                        "reservation-failed",
                        event.orderId,
                        match<ReservationFailedEvent> {
                            it.orderId == event.orderId &&
                                it.productId == event.productId &&
                                it.quantity == event.quantity &&
                                it.reason == "재고가 부족합니다"
                        }
                    )
                }
                verify(exactly = 1) { acknowledgment.acknowledge() }
            }
        }

        `when`("상품을 찾을 수 없으면") {
            every {
                inventoryReservationService.createReservation(
                    orderId = event.orderId,
                    productId = event.productId,
                    quantity = event.quantity
                )
            } throws IllegalArgumentException("Product not found: ${event.productId}")
            every { kafkaTemplate.send(any<String>(), any<String>(), any()) } returns mockk()

            then("예약 실패 이벤트가 발행되어야 한다") {
                consumer.handleInventoryReservationRequest(event, acknowledgment)

                verify(exactly = 1) {
                    kafkaTemplate.send(
                        "reservation-failed",
                        event.orderId,
                        match<ReservationFailedEvent> {
                            it.orderId == event.orderId &&
                                it.reason.contains("Product not found")
                        }
                    )
                }
                verify(exactly = 1) { acknowledgment.acknowledge() }
            }
        }
    }

    given("OrderConfirmedEvent가 수신되었을 때") {
        val event = OrderConfirmedEvent(
            orderId = 1L,
            orderNumber = "ORDER-001",
            userId = "USER-001"
        )

        `when`("해당 주문의 예약이 존재하면") {
            every { inventoryReservationService.confirmReservationsByOrderId(event.orderNumber) } just runs

            then("예약이 확정되고 acknowledge가 호출되어야 한다") {
                consumer.handleOrderConfirmed(event, acknowledgment)

                verify(exactly = 1) { inventoryReservationService.confirmReservationsByOrderId(event.orderNumber) }
                verify(exactly = 1) { acknowledgment.acknowledge() }
            }
        }

        `when`("예약 확정 중 예외가 발생하면") {
            every {
                inventoryReservationService.confirmReservationsByOrderId(event.orderNumber)
            } throws RuntimeException("DB 오류")

            then("예외가 발생해도 로그만 남기고 처리되어야 한다") {
                consumer.handleOrderConfirmed(event, acknowledgment)

                verify(exactly = 1) { inventoryReservationService.confirmReservationsByOrderId(event.orderNumber) }
                verify(exactly = 0) { acknowledgment.acknowledge() }
            }
        }
    }

    given("OrderCancelledEvent가 수신되었을 때") {
        val event = OrderCancelledEvent(
            orderId = 1L,
            orderNumber = "ORDER-001",
            userId = "USER-001",
            reason = "사용자 요청"
        )

        `when`("해당 주문의 예약이 존재하면") {
            every { inventoryReservationService.cancelReservationsByOrderId(event.orderNumber) } just runs

            then("예약이 취소되고 acknowledge가 호출되어야 한다") {
                consumer.handleOrderCancelled(event, acknowledgment)

                verify(exactly = 1) { inventoryReservationService.cancelReservationsByOrderId(event.orderNumber) }
                verify(exactly = 1) { acknowledgment.acknowledge() }
            }
        }

        `when`("해당 주문의 예약이 없으면") {
            every { inventoryReservationService.cancelReservationsByOrderId(event.orderNumber) } just runs

            then("예외 없이 정상 처리되어야 한다 (idempotent)") {
                consumer.handleOrderCancelled(event, acknowledgment)

                verify(exactly = 1) { inventoryReservationService.cancelReservationsByOrderId(event.orderNumber) }
                verify(exactly = 1) { acknowledgment.acknowledge() }
            }
        }

        `when`("예약 취소 중 예외가 발생하면") {
            every {
                inventoryReservationService.cancelReservationsByOrderId(event.orderNumber)
            } throws RuntimeException("DB 오류")

            then("예외가 발생해도 로그만 남기고 처리되어야 한다") {
                consumer.handleOrderCancelled(event, acknowledgment)

                verify(exactly = 1) { inventoryReservationService.cancelReservationsByOrderId(event.orderNumber) }
                verify(exactly = 0) { acknowledgment.acknowledge() }
            }
        }
    }
})
