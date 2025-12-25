package com.ecommerce.service

import com.ecommerce.enums.PaymentMethod
import com.ecommerce.enums.PaymentStatus
import com.ecommerce.event.*
import com.ecommerce.fixtures.PaymentFixtures
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import org.springframework.kafka.core.KafkaTemplate
import java.math.BigDecimal

class PaymentEventPublisherTest : BehaviorSpec({
  isolationMode = IsolationMode.InstancePerLeaf

  val kafkaTemplate = mockk<KafkaTemplate<String, Any>>()
  val publisher = PaymentEventPublisher(kafkaTemplate)

  beforeTest {
    every { kafkaTemplate.send(any<String>(), any<String>(), any()) } returns mockk()
  }

  given("PaymentEventPublisher가 주어졌을 때") {

    `when`("publishPaymentCreated를 호출하면") {
      val payment = PaymentFixtures.createPayment(
        id = 1L,
        orderId = "ORDER-001",
        userId = "USER-001",
        amount = BigDecimal("10000.00"),
        paymentMethod = PaymentMethod.CARD
      )

      then("payment-created 토픽으로 PaymentCreatedEvent를 발행해야 한다") {
        publisher.publishPaymentCreated(payment)

        verify {
          kafkaTemplate.send(
            "payment-created",
            "ORDER-001",
            match<PaymentCreatedEvent> {
              it.paymentId == 1L &&
                it.orderId == "ORDER-001" &&
                it.userId == "USER-001" &&
                it.amount == BigDecimal("10000.00") &&
                it.paymentMethod == PaymentMethod.CARD
            }
          )
        }
      }
    }

    `when`("publishPaymentCompleted를 호출하면") {
      val payment = PaymentFixtures.createPayment(
        id = 1L,
        orderId = "ORDER-001",
        userId = "USER-001",
        amount = BigDecimal("10000.00"),
        status = PaymentStatus.COMPLETED,
        paymentMethod = PaymentMethod.CARD,
        pgProvider = "TOSS",
        pgPaymentKey = "PG-KEY-001"
      )

      then("payment-completed 토픽으로 PaymentCompletedEvent를 발행해야 한다") {
        publisher.publishPaymentCompleted(payment)

        verify {
          kafkaTemplate.send(
            "payment-completed",
            "ORDER-001",
            match<PaymentCompletedEvent> {
              it.paymentId == 1L &&
                it.orderId == "ORDER-001" &&
                it.userId == "USER-001" &&
                it.amount == BigDecimal("10000.00") &&
                it.pgProvider == "TOSS" &&
                it.pgPaymentKey == "PG-KEY-001"
            }
          )
        }
      }
    }

    `when`("pgProvider와 pgPaymentKey가 null인 경우 publishPaymentCompleted를 호출하면") {
      val payment = PaymentFixtures.createPayment(
        id = 1L,
        orderId = "ORDER-001",
        userId = "USER-001",
        amount = BigDecimal("10000.00"),
        status = PaymentStatus.COMPLETED,
        paymentMethod = PaymentMethod.CARD,
        pgProvider = null,
        pgPaymentKey = null
      )

      then("빈 문자열로 이벤트를 발행해야 한다") {
        publisher.publishPaymentCompleted(payment)

        verify {
          kafkaTemplate.send(
            "payment-completed",
            "ORDER-001",
            match<PaymentCompletedEvent> {
              it.pgProvider == "" &&
                it.pgPaymentKey == ""
            }
          )
        }
      }
    }

    `when`("publishPaymentFailed를 호출하면") {
      val payment = PaymentFixtures.createPayment(
        id = 1L,
        orderId = "ORDER-001",
        userId = "USER-001",
        amount = BigDecimal("10000.00"),
        status = PaymentStatus.FAILED,
        paymentMethod = PaymentMethod.CARD
      )
      val failureReason = "카드 한도 초과"

      then("payment-failed 토픽으로 PaymentFailedEvent를 발행해야 한다") {
        publisher.publishPaymentFailed(payment, failureReason)

        verify {
          kafkaTemplate.send(
            "payment-failed",
            "ORDER-001",
            match<PaymentFailedEvent> {
              it.paymentId == 1L &&
                it.orderId == "ORDER-001" &&
                it.userId == "USER-001" &&
                it.amount == BigDecimal("10000.00") &&
                it.failureReason == "카드 한도 초과"
            }
          )
        }
      }
    }

    `when`("publishPaymentCancelled를 호출하면") {
      val payment = PaymentFixtures.createPayment(
        id = 1L,
        orderId = "ORDER-001",
        userId = "USER-001",
        amount = BigDecimal("10000.00"),
        status = PaymentStatus.CANCELLED,
        paymentMethod = PaymentMethod.CARD
      )
      val reason = "사용자 취소"

      then("payment-cancelled 토픽으로 PaymentCancelledEvent를 발행해야 한다") {
        publisher.publishPaymentCancelled(payment, reason)

        verify {
          kafkaTemplate.send(
            "payment-cancelled",
            "ORDER-001",
            match<PaymentCancelledEvent> {
              it.paymentId == 1L &&
                it.orderId == "ORDER-001" &&
                it.userId == "USER-001" &&
                it.amount == BigDecimal("10000.00") &&
                it.reason == "사용자 취소"
            }
          )
        }
      }
    }

    `when`("publishPaymentRefunded를 호출하면") {
      val payment = PaymentFixtures.createPayment(
        id = 1L,
        orderId = "ORDER-001",
        userId = "USER-001",
        amount = BigDecimal("10000.00"),
        status = PaymentStatus.REFUNDED,
        paymentMethod = PaymentMethod.CARD
      )
      val reason = "상품 환불"

      then("payment-refunded 토픽으로 PaymentRefundedEvent를 발행해야 한다") {
        publisher.publishPaymentRefunded(payment, reason)

        verify {
          kafkaTemplate.send(
            "payment-refunded",
            "ORDER-001",
            match<PaymentRefundedEvent> {
              it.paymentId == 1L &&
                it.orderId == "ORDER-001" &&
                it.userId == "USER-001" &&
                it.amount == BigDecimal("10000.00") &&
                it.reason == "상품 환불"
            }
          )
        }
      }
    }

    `when`("여러 이벤트를 순차적으로 발행하면") {
      val payment = PaymentFixtures.createPayment(
        id = 1L,
        orderId = "ORDER-001",
        userId = "USER-001",
        amount = BigDecimal("10000.00"),
        paymentMethod = PaymentMethod.CARD
      )

      then("모든 이벤트가 올바르게 발행되어야 한다") {
        publisher.publishPaymentCreated(payment)
        publisher.publishPaymentCompleted(payment)
        publisher.publishPaymentFailed(payment, "실패")
        publisher.publishPaymentCancelled(payment, "취소")
        publisher.publishPaymentRefunded(payment, "환불")

        verify(exactly = 1) { kafkaTemplate.send("payment-created", any<String>(), any<PaymentCreatedEvent>()) }
        verify(exactly = 1) { kafkaTemplate.send("payment-completed", any<String>(), any<PaymentCompletedEvent>()) }
        verify(exactly = 1) { kafkaTemplate.send("payment-failed", any<String>(), any<PaymentFailedEvent>()) }
        verify(exactly = 1) { kafkaTemplate.send("payment-cancelled", any<String>(), any<PaymentCancelledEvent>()) }
        verify(exactly = 1) { kafkaTemplate.send("payment-refunded", any<String>(), any<PaymentRefundedEvent>()) }
      }
    }
  }
})
