package com.ecommerce.consumer

import com.ecommerce.enums.PaymentMethod
import com.ecommerce.enums.PaymentStatus
import com.ecommerce.fixtures.PaymentFixtures
import com.ecommerce.request.CreatePaymentRequest
import com.ecommerce.response.PaymentResponse
import com.ecommerce.service.PaymentCommandService
import com.ecommerce.service.PaymentQueryService
import com.google.protobuf.Timestamp
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import org.springframework.kafka.support.Acknowledgment
import java.math.BigDecimal

class OrderEventConsumerTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val commandService = mockk<PaymentCommandService>()
    val queryService = mockk<PaymentQueryService>()
    val acknowledgment = mockk<Acknowledgment>()

    val consumer = OrderEventConsumer(commandService, queryService)

    beforeEach {
        clearMocks(commandService, queryService, acknowledgment, answers = false)
        every { acknowledgment.acknowledge() } just runs
    }

    given("OrderCreatedEvent(Protobuf)가 수신되었을 때") {
        val event = com.ecommerce.proto.order.OrderCreatedEvent.newBuilder()
            .setOrderId(1L)
            .setOrderNumber("ORDER-001")
            .setUserId("USER-001")
            .addItems(com.ecommerce.proto.order.OrderItem.newBuilder()
                .setProductId("PROD-001")
                .setProductName("테스트 상품")
                .setPrice(com.ecommerce.proto.common.Money.newBuilder()
                    .setAmount("50000")
                    .setCurrency("KRW")
                    .build())
                .setQuantity(2)
                .build())
            .setTotalAmount(com.ecommerce.proto.common.Money.newBuilder()
                .setAmount("100000")
                .setCurrency("KRW")
                .build())
            .setShippingAddress("서울시 강남구")
            .setShippingName("홍길동")
            .setShippingPhone("010-1234-5678")
            .setTimestamp(Timestamp.newBuilder()
                .setSeconds(System.currentTimeMillis() / 1000)
                .build())
            .build()

        `when`("정상적으로 처리되면") {
            val paymentResponse = PaymentResponse(
                id = 1L,
                orderId = event.orderNumber,
                userId = event.userId,
                amount = BigDecimal(event.totalAmount.amount),
                status = PaymentStatus.PENDING,
                paymentMethod = PaymentMethod.CARD,
                pgProvider = null,
                pgPaymentKey = null,
                failureReason = null,
                approvedAt = null,
                transactions = emptyList(),
                createdAt = null,
                updatedAt = null
            )
            every { commandService.createPayment(any()) } returns paymentResponse

            then("결제가 생성되고 acknowledge가 호출되어야 한다") {
                consumer.handleOrderCreated(event, acknowledgment)

                verify(exactly = 1) {
                    commandService.createPayment(match<CreatePaymentRequest> {
                        it.orderId == event.orderNumber &&
                            it.userId == event.userId &&
                            it.amount == BigDecimal(event.totalAmount.amount) &&
                            it.paymentMethod == PaymentMethod.CARD
                    })
                }
                verify(exactly = 1) { acknowledgment.acknowledge() }
            }
        }

        `when`("결제 생성 중 예외가 발생하면") {
            every { commandService.createPayment(any()) } throws RuntimeException("DB 연결 오류")

            then("예외가 발생해도 로그만 남기고 처리되어야 한다") {
                consumer.handleOrderCreated(event, acknowledgment)

                verify(exactly = 1) { commandService.createPayment(any()) }
                verify(exactly = 0) { acknowledgment.acknowledge() }
            }
        }
    }

    given("OrderCancelledEvent(Protobuf)가 수신되었을 때") {
        val event = com.ecommerce.proto.order.OrderCancelledEvent.newBuilder()
            .setOrderId(1L)
            .setOrderNumber("ORDER-001")
            .setUserId("USER-001")
            .setReason("재고 부족")
            .setTimestamp(Timestamp.newBuilder()
                .setSeconds(System.currentTimeMillis() / 1000)
                .build())
            .build()

        `when`("해당 주문의 결제가 존재하면") {
            val payment = PaymentFixtures.createPayment(
                id = 10L,
                orderId = event.orderNumber,
                status = PaymentStatus.PENDING
            )
            val paymentResponse = PaymentResponse(
                id = payment.id,
                orderId = payment.orderId,
                userId = payment.userId,
                amount = payment.amount,
                status = PaymentStatus.CANCELLED,
                paymentMethod = payment.paymentMethod,
                pgProvider = null,
                pgPaymentKey = null,
                failureReason = null,
                approvedAt = null,
                transactions = emptyList(),
                createdAt = null,
                updatedAt = null
            )

            every { queryService.getPaymentByOrderId(event.orderNumber) } returns paymentResponse
            every { commandService.cancelPayment(payment.id, event.reason) } returns paymentResponse

            then("결제가 취소되고 acknowledge가 호출되어야 한다") {
                consumer.handleOrderCancelled(event, acknowledgment)

                verify(exactly = 1) { queryService.getPaymentByOrderId(event.orderNumber) }
                verify(exactly = 1) { commandService.cancelPayment(payment.id, event.reason) }
                verify(exactly = 1) { acknowledgment.acknowledge() }
            }
        }

        `when`("해당 주문의 결제가 존재하지 않으면") {
            every { queryService.getPaymentByOrderId(event.orderNumber) } throws
                IllegalArgumentException("결제를 찾을 수 없습니다")

            then("예외가 발생해도 acknowledge가 호출되어야 한다") {
                consumer.handleOrderCancelled(event, acknowledgment)

                verify(exactly = 1) { queryService.getPaymentByOrderId(event.orderNumber) }
                verify(exactly = 0) { commandService.cancelPayment(any(), any()) }
                verify(exactly = 1) { acknowledgment.acknowledge() }
            }
        }

        `when`("결제 취소 중 예외가 발생하면") {
            val paymentResponse = PaymentResponse(
                id = 10L,
                orderId = event.orderNumber,
                userId = event.userId,
                amount = BigDecimal("100000"),
                status = PaymentStatus.PENDING,
                paymentMethod = PaymentMethod.CARD,
                pgProvider = null,
                pgPaymentKey = null,
                failureReason = null,
                approvedAt = null,
                transactions = emptyList(),
                createdAt = null,
                updatedAt = null
            )

            every { queryService.getPaymentByOrderId(event.orderNumber) } returns paymentResponse
            every { commandService.cancelPayment(any(), any()) } throws RuntimeException("취소 실패")

            then("예외가 발생해도 acknowledge가 호출되어야 한다") {
                consumer.handleOrderCancelled(event, acknowledgment)

                verify(exactly = 1) { queryService.getPaymentByOrderId(event.orderNumber) }
                verify(exactly = 1) { commandService.cancelPayment(any(), any()) }
                verify(exactly = 1) { acknowledgment.acknowledge() }
            }
        }
    }
})
