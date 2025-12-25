package com.ecommerce.service

import com.ecommerce.enums.PaymentMethod
import com.ecommerce.enums.PaymentStatus
import com.ecommerce.exception.PaymentNotFoundByOrderIdException
import com.ecommerce.fixtures.PaymentFixtures
import com.ecommerce.repository.PaymentRepository
import com.ecommerce.request.PaymentWebhookRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.math.BigDecimal

class PaymentWebhookServiceTest : BehaviorSpec({
  isolationMode = IsolationMode.InstancePerLeaf

  val paymentRepository = mockk<PaymentRepository>()
  val gatewayAdapter = mockk<PaymentGatewayAdapter>()
  val eventPublisher = mockk<PaymentEventPublisher>()

  val service = PaymentWebhookService(
    paymentRepository,
    gatewayAdapter,
    eventPublisher
  )

  beforeEach {
    clearMocks(
      paymentRepository,
      gatewayAdapter,
      eventPublisher,
      answers = false
    )
  }

  given("processPaymentWebhook이 주어졌을 때") {
    val signature = "valid_signature"
    val payload = "webhook_payload"

    `when`("유효한 PAYMENT_COMPLETED 웹훅을 받으면") {
      val webhookRequest = PaymentWebhookRequest(
        eventType = "PAYMENT_COMPLETED",
        orderId = "ORDER-001",
        pgPaymentKey = "toss_key_123",
        pgTransactionId = "TXN-001",
        amount = BigDecimal("100000"),
        paymentMethod = PaymentMethod.CARD,
        pgProvider = "TOSS",
        status = "COMPLETED",
        responseCode = "0000",
        responseMessage = "승인 성공",
        timestamp = "2024-01-01T00:00:00"
      )
      val payment = PaymentFixtures.createPayment(
        orderId = "ORDER-001",
        status = PaymentStatus.PENDING,
        paymentMethod = PaymentMethod.CARD
      )

      every { gatewayAdapter.verifyWebhook(signature, payload) } returns true
      every { paymentRepository.findByOrderId("ORDER-001") } returns payment
      every { paymentRepository.save(any()) } returns payment
      every { eventPublisher.publishPaymentCompleted(any()) } just runs

      then("결제가 완료 상태로 변경되고 완료 이벤트가 발행되어야 한다") {
        service.processPaymentWebhook(webhookRequest, signature, payload)

        payment.status shouldBe PaymentStatus.COMPLETED
        payment.pgProvider shouldBe "TOSS"
        payment.pgPaymentKey shouldBe "toss_key_123"
        payment.transactions.size shouldBe 1

        verify(exactly = 1) { gatewayAdapter.verifyWebhook(signature, payload) }
        verify(exactly = 1) { paymentRepository.findByOrderId("ORDER-001") }
        verify(exactly = 1) { paymentRepository.save(payment) }
        verify(exactly = 1) { eventPublisher.publishPaymentCompleted(payment) }
      }
    }

    `when`("이미 완료된 결제에 대한 PAYMENT_COMPLETED 웹훅을 받으면") {
      val webhookRequest = PaymentWebhookRequest(
        eventType = "PAYMENT_COMPLETED",
        orderId = "ORDER-001",
        pgPaymentKey = "toss_key_123",
        pgTransactionId = "TXN-001",
        amount = BigDecimal("100000"),
        paymentMethod = PaymentMethod.CARD,
        pgProvider = "TOSS",
        status = "COMPLETED",
        responseCode = "0000",
        responseMessage = "승인 성공",
        timestamp = "2024-01-01T00:00:00"
      )
      val payment = PaymentFixtures.createPayment(
        orderId = "ORDER-001",
        status = PaymentStatus.COMPLETED
      )

      every { gatewayAdapter.verifyWebhook(signature, payload) } returns true
      every { paymentRepository.findByOrderId("ORDER-001") } returns payment

      then("중복 처리를 방지하고 조기 반환해야 한다") {
        service.processPaymentWebhook(webhookRequest, signature, payload)

        verify(exactly = 1) { gatewayAdapter.verifyWebhook(signature, payload) }
        verify(exactly = 1) { paymentRepository.findByOrderId("ORDER-001") }
        verify(exactly = 0) { paymentRepository.save(any()) }
        verify(exactly = 0) { eventPublisher.publishPaymentCompleted(any()) }
      }
    }

    `when`("유효한 PAYMENT_FAILED 웹훅을 받으면") {
      val webhookRequest = PaymentWebhookRequest(
        eventType = "PAYMENT_FAILED",
        orderId = "ORDER-002",
        pgPaymentKey = "toss_key_456",
        pgTransactionId = "TXN-002",
        amount = BigDecimal("50000"),
        paymentMethod = PaymentMethod.CARD,
        pgProvider = "TOSS",
        status = "FAILED",
        responseCode = "9999",
        responseMessage = "카드 잔액 부족",
        timestamp = "2024-01-01T00:00:00"
      )
      val payment = PaymentFixtures.createPayment(
        orderId = "ORDER-002",
        status = PaymentStatus.PENDING
      )

      every { gatewayAdapter.verifyWebhook(signature, payload) } returns true
      every { paymentRepository.findByOrderId("ORDER-002") } returns payment
      every { paymentRepository.save(any()) } returns payment
      every { eventPublisher.publishPaymentFailed(any(), any()) } just runs

      then("결제가 실패 상태로 변경되고 실패 이벤트가 발행되어야 한다") {
        service.processPaymentWebhook(webhookRequest, signature, payload)

        payment.status shouldBe PaymentStatus.FAILED
        payment.failureReason shouldBe "카드 잔액 부족"
        payment.transactions.size shouldBe 1

        verify(exactly = 1) { gatewayAdapter.verifyWebhook(signature, payload) }
        verify(exactly = 1) { paymentRepository.findByOrderId("ORDER-002") }
        verify(exactly = 1) { paymentRepository.save(payment) }
        verify(exactly = 1) { eventPublisher.publishPaymentFailed(payment, "카드 잔액 부족") }
      }
    }

    `when`("유효한 PAYMENT_CANCELLED 웹훅을 받으면") {
      val webhookRequest = PaymentWebhookRequest(
        eventType = "PAYMENT_CANCELLED",
        orderId = "ORDER-003",
        pgPaymentKey = "toss_key_789",
        pgTransactionId = "TXN-003",
        amount = BigDecimal("75000"),
        paymentMethod = PaymentMethod.CARD,
        pgProvider = "TOSS",
        status = "CANCELLED",
        responseCode = "0000",
        responseMessage = "사용자 취소",
        timestamp = "2024-01-01T00:00:00"
      )
      val payment = PaymentFixtures.createPayment(
        orderId = "ORDER-003",
        status = PaymentStatus.PROCESSING
      )

      every { gatewayAdapter.verifyWebhook(signature, payload) } returns true
      every { paymentRepository.findByOrderId("ORDER-003") } returns payment
      every { paymentRepository.save(any()) } returns payment
      every { eventPublisher.publishPaymentCancelled(any(), any()) } just runs

      then("결제가 취소 상태로 변경되고 취소 이벤트가 발행되어야 한다") {
        service.processPaymentWebhook(webhookRequest, signature, payload)

        payment.status shouldBe PaymentStatus.CANCELLED
        payment.failureReason shouldBe "사용자 취소"
        payment.transactions.size shouldBe 1

        verify(exactly = 1) { gatewayAdapter.verifyWebhook(signature, payload) }
        verify(exactly = 1) { paymentRepository.findByOrderId("ORDER-003") }
        verify(exactly = 1) { paymentRepository.save(payment) }
        verify(exactly = 1) { eventPublisher.publishPaymentCancelled(payment, "사용자 취소") }
      }
    }

    `when`("유효한 PAYMENT_REFUNDED 웹훅을 받으면") {
      val webhookRequest = PaymentWebhookRequest(
        eventType = "PAYMENT_REFUNDED",
        orderId = "ORDER-004",
        pgPaymentKey = "toss_key_012",
        pgTransactionId = "TXN-004",
        amount = BigDecimal("120000"),
        paymentMethod = PaymentMethod.CARD,
        pgProvider = "TOSS",
        status = "REFUNDED",
        responseCode = "0000",
        responseMessage = "환불 완료",
        timestamp = "2024-01-01T00:00:00"
      )
      val payment = PaymentFixtures.createPayment(
        orderId = "ORDER-004",
        status = PaymentStatus.COMPLETED
      )

      every { gatewayAdapter.verifyWebhook(signature, payload) } returns true
      every { paymentRepository.findByOrderId("ORDER-004") } returns payment
      every { paymentRepository.save(any()) } returns payment
      every { eventPublisher.publishPaymentRefunded(any(), any()) } just runs

      then("결제가 환불 상태로 변경되고 환불 이벤트가 발행되어야 한다") {
        service.processPaymentWebhook(webhookRequest, signature, payload)

        payment.status shouldBe PaymentStatus.REFUNDED
        payment.transactions.size shouldBe 1

        verify(exactly = 1) { gatewayAdapter.verifyWebhook(signature, payload) }
        verify(exactly = 1) { paymentRepository.findByOrderId("ORDER-004") }
        verify(exactly = 1) { paymentRepository.save(payment) }
        verify(exactly = 1) { eventPublisher.publishPaymentRefunded(payment, "환불 완료") }
      }
    }

    `when`("알 수 없는 이벤트 타입의 웹훅을 받으면") {
      val webhookRequest = PaymentWebhookRequest(
        eventType = "UNKNOWN_EVENT",
        orderId = "ORDER-005",
        pgPaymentKey = "toss_key_345",
        pgTransactionId = "TXN-005",
        amount = BigDecimal("50000"),
        paymentMethod = PaymentMethod.CARD,
        pgProvider = "TOSS",
        status = "UNKNOWN",
        responseCode = "0000",
        responseMessage = "Unknown",
        timestamp = "2024-01-01T00:00:00"
      )

      every { gatewayAdapter.verifyWebhook(signature, payload) } returns true

      then("로그만 남기고 아무 처리도 하지 않아야 한다") {
        service.processPaymentWebhook(webhookRequest, signature, payload)

        verify(exactly = 1) { gatewayAdapter.verifyWebhook(signature, payload) }
        verify(exactly = 0) { paymentRepository.findByOrderId(any()) }
      }
    }

    `when`("웹훅 서명이 유효하지 않으면") {
      val webhookRequest = PaymentWebhookRequest(
        eventType = "PAYMENT_COMPLETED",
        orderId = "ORDER-006",
        pgPaymentKey = "toss_key_678",
        pgTransactionId = "TXN-006",
        amount = BigDecimal("100000"),
        paymentMethod = PaymentMethod.CARD,
        pgProvider = "TOSS",
        status = "COMPLETED",
        responseCode = "0000",
        responseMessage = "승인 성공",
        timestamp = "2024-01-01T00:00:00"
      )

      every { gatewayAdapter.verifyWebhook(signature, payload) } returns false

      then("SecurityException이 발생해야 한다") {
        val exception = shouldThrow<SecurityException> {
          service.processPaymentWebhook(webhookRequest, signature, payload)
        }
        exception.message shouldBe "유효하지 않은 Webhook 요청입니다"

        verify(exactly = 1) { gatewayAdapter.verifyWebhook(signature, payload) }
        verify(exactly = 0) { paymentRepository.findByOrderId(any()) }
      }
    }

    `when`("존재하지 않는 주문 ID로 웹훅을 받으면") {
      val webhookRequest = PaymentWebhookRequest(
        eventType = "PAYMENT_COMPLETED",
        orderId = "ORDER-999",
        pgPaymentKey = "toss_key_999",
        pgTransactionId = "TXN-999",
        amount = BigDecimal("100000"),
        paymentMethod = PaymentMethod.CARD,
        pgProvider = "TOSS",
        status = "COMPLETED",
        responseCode = "0000",
        responseMessage = "승인 성공",
        timestamp = "2024-01-01T00:00:00"
      )

      every { gatewayAdapter.verifyWebhook(signature, payload) } returns true
      every { paymentRepository.findByOrderId("ORDER-999") } returns null

      then("PaymentNotFoundByOrderIdException이 발생해야 한다") {
        shouldThrow<PaymentNotFoundByOrderIdException> {
          service.processPaymentWebhook(webhookRequest, signature, payload)
        }

        verify(exactly = 1) { gatewayAdapter.verifyWebhook(signature, payload) }
        verify(exactly = 1) { paymentRepository.findByOrderId("ORDER-999") }
      }
    }
  }
})
