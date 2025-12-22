package com.ecommerce.entity

import com.ecommerce.enums.TransactionStatus
import com.ecommerce.enums.TransactionType
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class PaymentTransactionTest : BehaviorSpec({
  isolationMode = IsolationMode.InstancePerLeaf

  given("PaymentTransaction.success 팩토리 메서드가 주어졌을 때") {
    `when`("성공 트랜잭션을 생성하면") {
      val transaction = PaymentTransaction.success(
        transactionType = TransactionType.AUTH,
        amount = BigDecimal("50000"),
        pgTransactionId = "TXN-SUCCESS-001",
        pgResponseCode = "0000",
        pgResponseMessage = "승인 성공"
      )

      then("SUCCESS 상태의 트랜잭션이 생성되어야 한다") {
        transaction.transactionType shouldBe TransactionType.AUTH
        transaction.amount shouldBe BigDecimal("50000")
        transaction.status shouldBe TransactionStatus.SUCCESS
        transaction.pgTransactionId shouldBe "TXN-SUCCESS-001"
        transaction.pgResponseCode shouldBe "0000"
        transaction.pgResponseMessage shouldBe "승인 성공"
        transaction.isSuccess() shouldBe true
        transaction.isFailed() shouldBe false
      }
    }
  }

  given("PaymentTransaction.failure 팩토리 메서드가 주어졌을 때") {
    `when`("실패 트랜잭션을 생성하면") {
      val transaction = PaymentTransaction.failure(
        transactionType = TransactionType.AUTH,
        amount = BigDecimal("50000"),
        pgTransactionId = "TXN-FAILED-001",
        pgResponseCode = "9999",
        pgResponseMessage = "카드 잔액 부족"
      )

      then("FAILED 상태의 트랜잭션이 생성되어야 한다") {
        transaction.transactionType shouldBe TransactionType.AUTH
        transaction.amount shouldBe BigDecimal("50000")
        transaction.status shouldBe TransactionStatus.FAILED
        transaction.pgTransactionId shouldBe "TXN-FAILED-001"
        transaction.pgResponseCode shouldBe "9999"
        transaction.pgResponseMessage shouldBe "카드 잔액 부족"
        transaction.isSuccess() shouldBe false
        transaction.isFailed() shouldBe true
      }
    }
  }

  given("다양한 트랜잭션 타입이 주어졌을 때") {
    `when`("AUTH 트랜잭션을 생성하면") {
      val authTransaction = PaymentTransaction.success(
        transactionType = TransactionType.AUTH,
        amount = BigDecimal("10000")
      )

      then("AUTH 타입이어야 한다") {
        authTransaction.transactionType shouldBe TransactionType.AUTH
      }
    }

    `when`("CAPTURE 트랜잭션을 생성하면") {
      val captureTransaction = PaymentTransaction.success(
        transactionType = TransactionType.CAPTURE,
        amount = BigDecimal("10000")
      )

      then("CAPTURE 타입이어야 한다") {
        captureTransaction.transactionType shouldBe TransactionType.CAPTURE
      }
    }

    `when`("CANCEL 트랜잭션을 생성하면") {
      val cancelTransaction = PaymentTransaction.success(
        transactionType = TransactionType.CANCEL,
        amount = BigDecimal("10000")
      )

      then("CANCEL 타입이어야 한다") {
        cancelTransaction.transactionType shouldBe TransactionType.CANCEL
      }
    }

    `when`("REFUND 트랜잭션을 생성하면") {
      val refundTransaction = PaymentTransaction.success(
        transactionType = TransactionType.REFUND,
        amount = BigDecimal("10000")
      )

      then("REFUND 타입이어야 한다") {
        refundTransaction.transactionType shouldBe TransactionType.REFUND
      }
    }
  }
})
