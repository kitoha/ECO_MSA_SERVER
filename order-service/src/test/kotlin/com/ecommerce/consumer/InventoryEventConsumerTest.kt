package com.ecommerce.consumer

import com.ecommerce.enums.OrderStatus
import com.ecommerce.event.ReservationFailedEvent
import com.ecommerce.response.OrderResponse
import com.ecommerce.service.OrderService
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import org.springframework.kafka.support.Acknowledgment
import java.math.BigDecimal
import java.time.LocalDateTime

class InventoryEventConsumerTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val orderService = mockk<OrderService>()
    val acknowledgment = mockk<Acknowledgment>()

    val consumer = InventoryEventConsumer(orderService)

    beforeEach {
        clearMocks(orderService, acknowledgment, answers = false)
        every { acknowledgment.acknowledge() } just runs
    }

    given("ReservationFailedEvent가 수신되었을 때") {
        val event = ReservationFailedEvent(
            orderId = "ORDER-001",
            productId = "PRODUCT-001",
            quantity = 5,
            reason = "재고가 부족합니다"
        )

        `when`("해당 주문이 존재하면") {
            val orderResponse = OrderResponse(
                id = 100L,
                orderNumber = event.orderId,
                userId = "USER-001",
                status = OrderStatus.PENDING,
                items = emptyList(),
                totalAmount = BigDecimal("100000"),
                shippingAddress = "서울시 강남구",
                shippingName = "홍길동",
                shippingPhone = "010-1234-5678",
                orderedAt = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            every { orderService.getOrderByOrderNumber(event.orderId) } returns orderResponse
            every { orderService.cancelOrder(orderResponse.id, "재고 예약 실패: ${event.reason}") } just runs

            then("주문이 취소되고 acknowledge가 호출되어야 한다") {
                consumer.handleReservationFailed(event, acknowledgment)

                verify(exactly = 1) { orderService.getOrderByOrderNumber(event.orderId) }
                verify(exactly = 1) { orderService.cancelOrder(orderResponse.id, "재고 예약 실패: ${event.reason}") }
                verify(exactly = 1) { acknowledgment.acknowledge() }
            }
        }

        `when`("해당 주문이 존재하지 않으면") {
            every { orderService.getOrderByOrderNumber(event.orderId) } throws
                IllegalArgumentException("존재하지 않는 주문입니다: ${event.orderId}")

            then("예외가 발생해도 acknowledge가 호출되어야 한다") {
                consumer.handleReservationFailed(event, acknowledgment)

                verify(exactly = 1) { orderService.getOrderByOrderNumber(event.orderId) }
                verify(exactly = 0) { orderService.cancelOrder(any(), any()) }
                verify(exactly = 1) { acknowledgment.acknowledge() }
            }
        }

        `when`("주문 취소 중 예외가 발생하면") {
            val orderResponse = OrderResponse(
                id = 100L,
                orderNumber = event.orderId,
                userId = "USER-001",
                status = OrderStatus.PENDING,
                items = emptyList(),
                totalAmount = BigDecimal("100000"),
                shippingAddress = "서울시 강남구",
                shippingName = "홍길동",
                shippingPhone = "010-1234-5678",
                orderedAt = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            every { orderService.getOrderByOrderNumber(event.orderId) } returns orderResponse
            every { orderService.cancelOrder(any(), any()) } throws RuntimeException("DB 오류")

            then("예외가 발생해도 acknowledge가 호출되어야 한다") {
                consumer.handleReservationFailed(event, acknowledgment)

                verify(exactly = 1) { orderService.getOrderByOrderNumber(event.orderId) }
                verify(exactly = 1) { orderService.cancelOrder(any(), any()) }
                verify(exactly = 1) { acknowledgment.acknowledge() }
            }
        }

        `when`("이미 취소된 주문이면") {
            val cancelledOrderResponse = OrderResponse(
                id = 100L,
                orderNumber = event.orderId,
                userId = "USER-001",
                status = OrderStatus.CANCELLED,
                items = emptyList(),
                totalAmount = BigDecimal("100000"),
                shippingAddress = "서울시 강남구",
                shippingName = "홍길동",
                shippingPhone = "010-1234-5678",
                orderedAt = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            every { orderService.getOrderByOrderNumber(event.orderId) } returns cancelledOrderResponse
            every { orderService.cancelOrder(any(), any()) } throws
                IllegalStateException("이미 취소된 주문입니다")

            then("예외가 발생해도 acknowledge가 호출되어야 한다 (멱등성)") {
                consumer.handleReservationFailed(event, acknowledgment)

                verify(exactly = 1) { orderService.getOrderByOrderNumber(event.orderId) }
                verify(exactly = 1) { acknowledgment.acknowledge() }
            }
        }
    }
})
