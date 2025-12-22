package com.ecommerce.service

import com.ecommerce.exception.PaymentNotFoundByOrderIdException
import com.ecommerce.exception.PaymentNotFoundException
import com.ecommerce.fixtures.PaymentFixtures
import com.ecommerce.repository.PaymentRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class PaymentQueryServiceTest : BehaviorSpec({
  isolationMode = IsolationMode.InstancePerLeaf

  val paymentRepository = mockk<PaymentRepository>()
  val service = PaymentQueryService(paymentRepository)

  beforeEach {
    clearMocks(paymentRepository, answers = false)
  }

  given("getPayment이 주어졌을 때") {
    val paymentId = 1L

    `when`("존재하는 결제 ID로 조회하면") {
      val payment = PaymentFixtures.createPayment(id = paymentId)
      every { paymentRepository.findById(paymentId) } returns payment

      then("결제 정보를 반환해야 한다") {
        val result = service.getPayment(paymentId)

        result.id shouldBe paymentId
        result.orderId shouldBe payment.orderId
        result.userId shouldBe payment.userId
        result.amount shouldBe payment.amount
        result.status shouldBe payment.status

        verify(exactly = 1) { paymentRepository.findById(paymentId) }
      }
    }

    `when`("존재하지 않는 결제 ID로 조회하면") {
      every { paymentRepository.findById(paymentId) } returns null

      then("PaymentNotFoundException이 발생해야 한다") {
        shouldThrow<PaymentNotFoundException> {
          service.getPayment(paymentId)
        }

        verify(exactly = 1) { paymentRepository.findById(paymentId) }
      }
    }
  }

  given("getPaymentByOrderId가 주어졌을 때") {
    val orderId = "ORDER-001"

    `when`("존재하는 주문 ID로 조회하면") {
      val payment = PaymentFixtures.createPayment(orderId = orderId)
      every { paymentRepository.findByOrderId(orderId) } returns payment

      then("결제 정보를 반환해야 한다") {
        val result = service.getPaymentByOrderId(orderId)

        result.orderId shouldBe orderId
        result.userId shouldBe payment.userId
        result.amount shouldBe payment.amount

        verify(exactly = 1) { paymentRepository.findByOrderId(orderId) }
      }
    }

    `when`("존재하지 않는 주문 ID로 조회하면") {
      every { paymentRepository.findByOrderId(orderId) } returns null

      then("PaymentNotFoundByOrderIdException이 발생해야 한다") {
        val exception = shouldThrow<PaymentNotFoundByOrderIdException> {
          service.getPaymentByOrderId(orderId)
        }
        exception.message shouldBe "주문에 대한 결제를 찾을 수 없습니다: $orderId"

        verify(exactly = 1) { paymentRepository.findByOrderId(orderId) }
      }
    }
  }

  given("getPaymentsByUserId가 주어졌을 때") {
    val userId = "USER-001"

    `when`("사용자 ID로 조회하면") {
      val payment1 = PaymentFixtures.createPayment(id = 1L, userId = userId, orderId = "ORDER-001")
      val payment2 = PaymentFixtures.createPayment(id = 2L, userId = userId, orderId = "ORDER-002")
      val payments = listOf(payment1, payment2)

      every { paymentRepository.findByUserId(userId) } returns payments

      then("해당 사용자의 모든 결제 정보를 반환해야 한다") {
        val results = service.getPaymentsByUserId(userId)

        results.size shouldBe 2
        results[0].id shouldBe 1L
        results[0].orderId shouldBe "ORDER-001"
        results[1].id shouldBe 2L
        results[1].orderId shouldBe "ORDER-002"

        verify(exactly = 1) { paymentRepository.findByUserId(userId) }
      }
    }

    `when`("결제가 없는 사용자 ID로 조회하면") {
      every { paymentRepository.findByUserId(userId) } returns emptyList()

      then("빈 리스트를 반환해야 한다") {
        val results = service.getPaymentsByUserId(userId)

        results.size shouldBe 0

        verify(exactly = 1) { paymentRepository.findByUserId(userId) }
      }
    }
  }
})
