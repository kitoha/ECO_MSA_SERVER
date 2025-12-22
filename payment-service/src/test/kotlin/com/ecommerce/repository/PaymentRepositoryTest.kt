package com.ecommerce.repository

import com.ecommerce.enums.PaymentStatus
import com.ecommerce.fixtures.PaymentFixtures
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.*

class PaymentRepositoryTest : BehaviorSpec({
  isolationMode = IsolationMode.InstancePerLeaf

  val jpaRepository = mockk<PaymentJpaRepository>()
  val queryRepository = mockk<PaymentQueryRepository>()

  val repository = PaymentRepository(jpaRepository, queryRepository)

  beforeEach {
    clearMocks(jpaRepository, queryRepository, answers = false)
  }

  given("save가 주어졌을 때") {
    val payment = PaymentFixtures.createPayment()

    `when`("결제를 저장하면") {
      every { jpaRepository.save(payment) } returns payment

      then("JPA 리포지토리를 통해 저장되어야 한다") {
        val result = repository.save(payment)

        result shouldBe payment
        verify(exactly = 1) { jpaRepository.save(payment) }
      }
    }
  }

  given("findById가 주어졌을 때") {
    val paymentId = 1L

    `when`("존재하는 ID로 조회하면") {
      val payment = PaymentFixtures.createPayment(id = paymentId)
      every { jpaRepository.findById(paymentId) } returns Optional.of(payment)

      then("결제 정보를 반환해야 한다") {
        val result = repository.findById(paymentId)

        result shouldBe payment
        verify(exactly = 1) { jpaRepository.findById(paymentId) }
      }
    }

    `when`("존재하지 않는 ID로 조회하면") {
      every { jpaRepository.findById(paymentId) } returns Optional.empty()

      then("null을 반환해야 한다") {
        val result = repository.findById(paymentId)

        result shouldBe null
        verify(exactly = 1) { jpaRepository.findById(paymentId) }
      }
    }
  }

  given("findByOrderId가 주어졌을 때") {
    val orderId = "ORDER-001"

    `when`("존재하는 주문 ID로 조회하면") {
      val payment = PaymentFixtures.createPayment(orderId = orderId)
      every { jpaRepository.findByOrderId(orderId) } returns payment

      then("결제 정보를 반환해야 한다") {
        val result = repository.findByOrderId(orderId)

        result shouldBe payment
        verify(exactly = 1) { jpaRepository.findByOrderId(orderId) }
      }
    }
  }

  given("findByUserId가 주어졌을 때") {
    val userId = "USER-001"

    `when`("사용자 ID로 조회하면") {
      val payment1 = PaymentFixtures.createPayment(id = 1L, userId = userId)
      val payment2 = PaymentFixtures.createPayment(id = 2L, userId = userId)
      val payments = listOf(payment1, payment2)

      every { queryRepository.findByUserId(userId) } returns payments

      then("QueryDSL 리포지토리를 통해 조회되어야 한다") {
        val results = repository.findByUserId(userId)

        results.size shouldBe 2
        results shouldBe payments
        verify(exactly = 1) { queryRepository.findByUserId(userId) }
      }
    }
  }

  given("findByStatus가 주어졌을 때") {
    val status = PaymentStatus.COMPLETED

    `when`("상태별로 조회하면") {
      val payment1 = PaymentFixtures.createPayment(id = 1L, status = status)
      val payment2 = PaymentFixtures.createPayment(id = 2L, status = status)
      val payments = listOf(payment1, payment2)

      every { queryRepository.findByStatus(status) } returns payments

      then("QueryDSL 리포지토리를 통해 조회되어야 한다") {
        val results = repository.findByStatus(status)

        results.size shouldBe 2
        results shouldBe payments
        verify(exactly = 1) { queryRepository.findByStatus(status) }
      }
    }
  }

  given("findByUserIdAndStatus가 주어졌을 때") {
    val userId = "USER-001"
    val status = PaymentStatus.PENDING

    `when`("사용자 ID와 상태로 조회하면") {
      val payment = PaymentFixtures.createPayment(userId = userId, status = status)
      val payments = listOf(payment)

      every { queryRepository.findByUserIdAndStatus(userId, status) } returns payments

      then("QueryDSL 리포지토리를 통해 조회되어야 한다") {
        val results = repository.findByUserIdAndStatus(userId, status)

        results.size shouldBe 1
        results shouldBe payments
        verify(exactly = 1) { queryRepository.findByUserIdAndStatus(userId, status) }
      }
    }
  }

  given("findByCreatedAtBetween이 주어졌을 때") {
    val startDate = LocalDateTime.of(2024, 1, 1, 0, 0)
    val endDate = LocalDateTime.of(2024, 1, 31, 23, 59)

    `when`("날짜 범위로 조회하면") {
      val payment = PaymentFixtures.createPayment()
      val payments = listOf(payment)

      every { queryRepository.findByCreatedAtBetween(startDate, endDate) } returns payments

      then("QueryDSL 리포지토리를 통해 조회되어야 한다") {
        val results = repository.findByCreatedAtBetween(startDate, endDate)

        results shouldBe payments
        verify(exactly = 1) { queryRepository.findByCreatedAtBetween(startDate, endDate) }
      }
    }
  }

  given("searchPayments가 주어졌을 때") {
    val userId = "USER-001"
    val status = PaymentStatus.COMPLETED
    val startDate = LocalDateTime.of(2024, 1, 1, 0, 0)
    val endDate = LocalDateTime.of(2024, 1, 31, 23, 59)

    `when`("복합 조건으로 검색하면") {
      val payment = PaymentFixtures.createPayment(userId = userId, status = status)
      val payments = listOf(payment)

      every { queryRepository.searchPayments(userId, status, startDate, endDate) } returns payments

      then("QueryDSL 리포지토리를 통해 검색되어야 한다") {
        val results = repository.searchPayments(userId, status, startDate, endDate)

        results shouldBe payments
        verify(exactly = 1) { queryRepository.searchPayments(userId, status, startDate, endDate) }
      }
    }
  }

  given("existsByOrderId가 주어졌을 때") {
    val orderId = "ORDER-001"

    `when`("존재하는 주문 ID를 확인하면") {
      val payment = PaymentFixtures.createPayment(orderId = orderId)
      every { jpaRepository.findByOrderId(orderId) } returns payment

      then("true를 반환해야 한다") {
        val result = repository.existsByOrderId(orderId)

        result shouldBe true
        verify(exactly = 1) { jpaRepository.findByOrderId(orderId) }
      }
    }

    `when`("존재하지 않는 주문 ID를 확인하면") {
      every { jpaRepository.findByOrderId(orderId) } returns null

      then("false를 반환해야 한다") {
        val result = repository.existsByOrderId(orderId)

        result shouldBe false
        verify(exactly = 1) { jpaRepository.findByOrderId(orderId) }
      }
    }
  }

  given("delete가 주어졌을 때") {
    val payment = PaymentFixtures.createPayment()

    `when`("결제를 삭제하면") {
      every { jpaRepository.delete(payment) } returns Unit

      then("JPA 리포지토리를 통해 삭제되어야 한다") {
        repository.delete(payment)

        verify(exactly = 1) { jpaRepository.delete(payment) }
      }
    }
  }
})
