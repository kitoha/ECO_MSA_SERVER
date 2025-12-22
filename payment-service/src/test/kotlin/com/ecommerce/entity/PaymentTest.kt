package com.ecommerce.entity

import com.ecommerce.enums.PaymentMethod
import com.ecommerce.enums.PaymentStatus
import com.ecommerce.fixtures.PaymentFixtures
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class PaymentTest : BehaviorSpec({
  isolationMode = IsolationMode.InstancePerLeaf

  given("PENDING 상태의 결제가 주어졌을 때") {
    val payment = PaymentFixtures.createPayment(
      status = PaymentStatus.PENDING,
      paymentMethod = PaymentMethod.CARD
    )

    `when`("startProcessing을 호출하면") {
      then("상태가 PROCESSING으로 변경되고 PG 정보가 설정되어야 한다") {
        payment.startProcessing("TOSS", "toss_payment_key_123", PaymentMethod.CARD)

        payment.status shouldBe PaymentStatus.PROCESSING
        payment.pgProvider shouldBe "TOSS"
        payment.pgPaymentKey shouldBe "toss_payment_key_123"
        payment.paymentMethod shouldBe PaymentMethod.CARD
      }
    }

    `when`("isPayable을 호출하면") {
      then("true를 반환해야 한다") {
        payment.isPayable() shouldBe true
      }
    }
  }

  given("PROCESSING 상태의 결제가 주어졌을 때") {
    val payment = PaymentFixtures.createPayment(status = PaymentStatus.PENDING)
    payment.startProcessing("TOSS", "toss_key", PaymentMethod.CARD)

    `when`("complete을 호출하면") {
      then("상태가 COMPLETED로 변경되고 승인 시간이 설정되어야 한다") {
        payment.complete()

        payment.status shouldBe PaymentStatus.COMPLETED
        payment.approvedAt.shouldNotBeNull()
        payment.isCompleted() shouldBe true
      }
    }

    `when`("fail을 호출하면") {
      val failurePayment = PaymentFixtures.createPayment(status = PaymentStatus.PENDING)
      failurePayment.startProcessing("TOSS", "toss_key", PaymentMethod.CARD)

      then("상태가 FAILED로 변경되고 실패 사유가 설정되어야 한다") {
        failurePayment.fail("카드 잔액 부족")

        failurePayment.status shouldBe PaymentStatus.FAILED
        failurePayment.failureReason shouldBe "카드 잔액 부족"
        failurePayment.isFailed() shouldBe true
      }
    }

    `when`("cancel을 호출하면") {
      val cancelPayment = PaymentFixtures.createPayment(status = PaymentStatus.PENDING)
      cancelPayment.startProcessing("TOSS", "toss_key", PaymentMethod.CARD)

      then("상태가 CANCELLED로 변경되어야 한다") {
        cancelPayment.cancel("사용자 요청")

        cancelPayment.status shouldBe PaymentStatus.CANCELLED
        cancelPayment.failureReason shouldBe "사용자 요청"
        cancelPayment.isCancelled() shouldBe true
      }
    }
  }

  given("COMPLETED 상태의 결제가 주어졌을 때") {
    val payment = PaymentFixtures.createPayment(status = PaymentStatus.PENDING)
    payment.startProcessing("TOSS", "toss_key", PaymentMethod.CARD)
    payment.complete()

    `when`("refund를 호출하면") {
      then("상태가 REFUNDED로 변경되어야 한다") {
        payment.refund()

        payment.status shouldBe PaymentStatus.REFUNDED
        payment.isRefunded() shouldBe true
      }
    }

    `when`("isRefundable을 호출하면") {
      val refundablePayment = PaymentFixtures.createPayment(status = PaymentStatus.PENDING)
      refundablePayment.startProcessing("TOSS", "toss_key", PaymentMethod.CARD)
      refundablePayment.complete()

      then("true를 반환해야 한다") {
        refundablePayment.isRefundable() shouldBe true
      }
    }

    `when`("cancel을 호출하면") {
      val completedPayment = PaymentFixtures.createPayment(status = PaymentStatus.PENDING)
      completedPayment.startProcessing("TOSS", "toss_key", PaymentMethod.CARD)
      completedPayment.complete()

      then("예외가 발생해야 한다") {
        val exception = shouldThrow<IllegalArgumentException> {
          completedPayment.cancel("취소 시도")
        }
        exception.message shouldBe "결제를 취소할 수 있는 상태가 아닙니다: COMPLETED"
      }
    }
  }

  given("잘못된 상태 전이를 시도할 때") {
    `when`("PROCESSING 상태가 아닌 결제를 complete하면") {
      val payment = PaymentFixtures.createPayment(status = PaymentStatus.PENDING)

      then("예외가 발생해야 한다") {
        val exception = shouldThrow<IllegalArgumentException> {
          payment.complete()
        }
        exception.message shouldBe "결제를 완료할 수 있는 상태가 아닙니다: PENDING"
      }
    }

    `when`("PENDING 상태가 아닌 결제를 startProcessing하면") {
      val payment = PaymentFixtures.createPayment(status = PaymentStatus.COMPLETED)

      then("예외가 발생해야 한다") {
        val exception = shouldThrow<IllegalArgumentException> {
          payment.startProcessing("TOSS", "key", PaymentMethod.CARD)
        }
        exception.message shouldBe "결제 처리를 시작할 수 있는 상태가 아닙니다: COMPLETED"
      }
    }

    `when`("COMPLETED 상태가 아닌 결제를 refund하면") {
      val payment = PaymentFixtures.createPayment(status = PaymentStatus.PENDING)

      then("예외가 발생해야 한다") {
        val exception = shouldThrow<IllegalArgumentException> {
          payment.refund()
        }
        exception.message shouldBe "환불할 수 있는 상태가 아닙니다: PENDING"
      }
    }
  }

  given("결제에 트랜잭션을 추가할 때") {
    val payment = PaymentFixtures.createPayment()
    val transaction = PaymentFixtures.createSuccessTransaction()

    `when`("addTransaction을 호출하면") {
      payment.addTransaction(transaction)

      then("트랜잭션이 결제에 추가되어야 한다") {
        payment.transactions.size shouldBe 1
        payment.transactions[0] shouldBe transaction
        transaction.payment shouldBe payment
      }
    }
  }

  given("결제 상태 확인 메서드들이 주어졌을 때") {
    `when`("각 상태별 확인 메서드를 호출하면") {
      then("올바른 결과를 반환해야 한다") {
        val pendingPayment = PaymentFixtures.createPayment(status = PaymentStatus.PENDING)
        pendingPayment.isPayable() shouldBe true
        pendingPayment.isCompleted() shouldBe false
        pendingPayment.isFailed() shouldBe false
        pendingPayment.isCancelled() shouldBe false
        pendingPayment.isRefunded() shouldBe false
        pendingPayment.isRefundable() shouldBe false

        val completedPayment = PaymentFixtures.createPayment(status = PaymentStatus.COMPLETED)
        completedPayment.isPayable() shouldBe false
        completedPayment.isCompleted() shouldBe true
        completedPayment.isRefundable() shouldBe true

        val failedPayment = PaymentFixtures.createPayment(status = PaymentStatus.FAILED)
        failedPayment.isFailed() shouldBe true

        val cancelledPayment = PaymentFixtures.createPayment(status = PaymentStatus.CANCELLED)
        cancelledPayment.isCancelled() shouldBe true

        val refundedPayment = PaymentFixtures.createPayment(status = PaymentStatus.REFUNDED)
        refundedPayment.isRefunded() shouldBe true
      }
    }
  }
})
