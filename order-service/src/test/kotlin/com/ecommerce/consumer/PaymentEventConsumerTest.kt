package com.ecommerce.consumer

import com.ecommerce.enums.OrderStatus
import com.ecommerce.event.PaymentCompletedEvent
import com.ecommerce.event.PaymentFailedEvent
import com.ecommerce.response.OrderResponse
import com.ecommerce.service.OrderService
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import org.springframework.kafka.support.Acknowledgment
import java.math.BigDecimal
import java.time.LocalDateTime

class PaymentEventConsumerTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val orderService = mockk<OrderService>()
    val acknowledgment = mockk<Acknowledgment>()

    val consumer = PaymentEventConsumer(orderService)

    beforeEach {
        clearMocks(orderService, acknowledgment, answers = false)
        every { acknowledgment.acknowledge() } just runs
    }

    given("PaymentCompletedEvent가 수신되었을 때") {
        val event = PaymentCompletedEvent(
            paymentId = 1L,
            orderId = "ORDER-001",
            userId = "USER-001",
            amount = BigDecimal("100000"),
            pgProvider = "TOSS",
            pgPaymentKey = "toss_key_123"
        )

        `when`("해당 주문이 존재하면") {
            val orderResponse = OrderResponse(
                id = 100L,
                orderNumber = event.orderId,
                userId = event.userId,
                status = OrderStatus.PENDING,
                items = emptyList(),
                totalAmount = event.amount,
                shippingAddress = "서울시 강남구",
                shippingName = "홍길동",
                shippingPhone = "010-1234-5678",
                orderedAt = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            every { orderService.getOrderByOrderNumber(event.orderId) } returns orderResponse
            every { orderService.confirmOrder(orderResponse.id) } just runs

            then("주문이 확정되고 acknowledge가 호출되어야 한다") {
                consumer.handlePaymentCompleted(event, acknowledgment)

                verify(exactly = 1) { orderService.getOrderByOrderNumber(event.orderId) }
                verify(exactly = 1) { orderService.confirmOrder(orderResponse.id) }
                verify(exactly = 1) { acknowledgment.acknowledge() }
            }
        }

        `when`("해당 주문이 존재하지 않으면") {
            every { orderService.getOrderByOrderNumber(event.orderId) } throws
                IllegalArgumentException("존재하지 않는 주문입니다: ${event.orderId}")

            then("예외가 발생해도 로그만 남기고 처리되어야 한다") {
                consumer.handlePaymentCompleted(event, acknowledgment)

                verify(exactly = 1) { orderService.getOrderByOrderNumber(event.orderId) }
                verify(exactly = 0) { orderService.confirmOrder(any()) }
                verify(exactly = 0) { acknowledgment.acknowledge() }
            }
        }

        `when`("주문 확정 중 예외가 발생하면") {
            val orderResponse = OrderResponse(
                id = 100L,
                orderNumber = event.orderId,
                userId = event.userId,
                status = OrderStatus.PENDING,
                items = emptyList(),
                totalAmount = event.amount,
                shippingAddress = "서울시 강남구",
                shippingName = "홍길동",
                shippingPhone = "010-1234-5678",
                orderedAt = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            every { orderService.getOrderByOrderNumber(event.orderId) } returns orderResponse
            every { orderService.confirmOrder(orderResponse.id) } throws RuntimeException("DB 오류")

            then("예외가 발생해도 로그만 남기고 처리되어야 한다") {
                consumer.handlePaymentCompleted(event, acknowledgment)

                verify(exactly = 1) { orderService.getOrderByOrderNumber(event.orderId) }
                verify(exactly = 1) { orderService.confirmOrder(orderResponse.id) }
                verify(exactly = 0) { acknowledgment.acknowledge() }
            }
        }
    }

    given("PaymentFailedEvent가 수신되었을 때") {
        val event = PaymentFailedEvent(
            paymentId = 1L,
            orderId = "ORDER-001",
            userId = "USER-001",
            amount = BigDecimal("100000"),
            failureReason = "카드 잔액 부족"
        )

        `when`("해당 주문이 존재하면") {
            val orderResponse = OrderResponse(
                id = 100L,
                orderNumber = event.orderId,
                userId = event.userId,
                status = OrderStatus.PENDING,
                items = emptyList(),
                totalAmount = event.amount,
                shippingAddress = "서울시 강남구",
                shippingName = "홍길동",
                shippingPhone = "010-1234-5678",
                orderedAt = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            every { orderService.getOrderByOrderNumber(event.orderId) } returns orderResponse
            every { orderService.cancelOrder(orderResponse.id, "결제 실패: ${event.failureReason}") } just runs

            then("주문이 취소되고 acknowledge가 호출되어야 한다") {
                consumer.handlePaymentFailed(event, acknowledgment)

                verify(exactly = 1) { orderService.getOrderByOrderNumber(event.orderId) }
                verify(exactly = 1) { orderService.cancelOrder(orderResponse.id, "결제 실패: ${event.failureReason}") }
                verify(exactly = 1) { acknowledgment.acknowledge() }
            }
        }

        `when`("해당 주문이 존재하지 않으면") {
            every { orderService.getOrderByOrderNumber(event.orderId) } throws
                IllegalArgumentException("존재하지 않는 주문입니다: ${event.orderId}")

            then("예외가 발생해도 로그만 남기고 처리되어야 한다") {
                consumer.handlePaymentFailed(event, acknowledgment)

                verify(exactly = 1) { orderService.getOrderByOrderNumber(event.orderId) }
                verify(exactly = 0) { orderService.cancelOrder(any(), any()) }
                verify(exactly = 0) { acknowledgment.acknowledge() }
            }
        }
    }
})
