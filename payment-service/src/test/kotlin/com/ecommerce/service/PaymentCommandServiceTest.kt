package com.ecommerce.service

import com.ecommerce.client.PaymentGatewayResponse
import com.ecommerce.enums.PaymentMethod
import com.ecommerce.enums.PaymentStatus
import com.ecommerce.enums.TransactionType
import com.ecommerce.exception.*
import com.ecommerce.fixtures.PaymentFixtures
import com.ecommerce.generator.TsidGenerator
import com.ecommerce.repository.PaymentRepository
import com.ecommerce.request.CreatePaymentRequest
import com.ecommerce.request.PaymentApprovalRequest
import com.ecommerce.request.PaymentRefundRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.math.BigDecimal

class PaymentCommandServiceTest : BehaviorSpec({
  isolationMode = IsolationMode.InstancePerLeaf

  val paymentRepository = mockk<PaymentRepository>()
  val idGenerator = mockk<TsidGenerator>()
  val gatewayAdapter = mockk<PaymentGatewayAdapter>()
  val transactionFactory = mockk<PaymentTransactionFactory>()
  val eventPublisher = mockk<PaymentEventPublisher>()

  val service = PaymentCommandService(
    paymentRepository,
    idGenerator,
    gatewayAdapter,
    transactionFactory,
    eventPublisher
  )

  beforeEach {
    clearMocks(
      paymentRepository,
      idGenerator,
      gatewayAdapter,
      transactionFactory,
      eventPublisher,
      answers = false
    )
  }

  given("createPayment이 주어졌을 때") {
    val request = CreatePaymentRequest(
      orderId = "ORDER-001",
      userId = "USER-001",
      amount = BigDecimal("100000"),
      paymentMethod = PaymentMethod.CARD
    )

    `when`("정상적인 결제 생성 요청을 받으면") {
      val generatedId = 1234567890L
      val payment = PaymentFixtures.createPayment(
        id = generatedId,
        orderId = request.orderId,
        userId = request.userId,
        amount = request.amount,
        paymentMethod = request.paymentMethod
      )

      every { paymentRepository.findByOrderId(request.orderId) } returns null
      every { idGenerator.generate() } returns generatedId
      every { paymentRepository.save(any()) } returns payment
      every { eventPublisher.publishPaymentCreated(any()) } just runs

      then("결제가 생성되고 이벤트가 발행되어야 한다") {
        val result = service.createPayment(request)

        result.orderId shouldBe request.orderId
        result.userId shouldBe request.userId
        result.amount shouldBe request.amount
        result.status shouldBe PaymentStatus.PENDING

        verify(exactly = 1) { paymentRepository.findByOrderId(request.orderId) }
        verify(exactly = 1) { idGenerator.generate() }
        verify(exactly = 1) { paymentRepository.save(any()) }
        verify(exactly = 1) { eventPublisher.publishPaymentCreated(payment) }
      }
    }

    `when`("이미 존재하는 주문 ID로 결제 생성을 시도하면") {
      val existingPayment = PaymentFixtures.createPayment(orderId = request.orderId)
      every { paymentRepository.findByOrderId(request.orderId) } returns existingPayment

      then("DuplicateOrderPaymentException이 발생해야 한다") {
        shouldThrow<DuplicateOrderPaymentException> {
          service.createPayment(request)
        }

        verify(exactly = 1) { paymentRepository.findByOrderId(request.orderId) }
        verify(exactly = 0) { paymentRepository.save(any()) }
      }
    }
  }

  given("approvePayment이 주어졌을 때") {
    val paymentId = 1L
    val payment = PaymentFixtures.createPayment(
      id = paymentId,
      status = PaymentStatus.PENDING,
      paymentMethod = PaymentMethod.CARD
    )
    val request = PaymentApprovalRequest(
      pgProvider = "TOSS",
      pgPaymentKey = "toss_key_123"
    )

    `when`("PG 승인이 성공하면") {
      val successResponse = PaymentGatewayResponse(
        success = true,
        transactionId = "TXN-001",
        paymentKey = "toss_key_123",
        amount = payment.amount,
        responseCode = "0000",
        responseMessage = "승인 성공"
      )
      val transaction = PaymentFixtures.createSuccessTransaction()

      every { paymentRepository.findByIdWithTransactions(paymentId) } returns payment
      every { gatewayAdapter.authorize(any(), any(), any()) } returns successResponse
      every { transactionFactory.createFromGatewayResponse(any(), any(), any()) } returns transaction
      every { paymentRepository.save(any()) } returns payment
      every { eventPublisher.publishPaymentCompleted(any()) } just runs

      then("결제가 완료되고 완료 이벤트가 발행되어야 한다") {
        val result = service.approvePayment(paymentId, request)

        result.status shouldBe PaymentStatus.COMPLETED

        verify(exactly = 1) { paymentRepository.findByIdWithTransactions(paymentId) }
        verify(exactly = 1) { gatewayAdapter.authorize(payment.orderId, payment.amount, PaymentMethod.CARD) }
        verify(exactly = 1) { transactionFactory.createFromGatewayResponse(TransactionType.AUTH, payment.amount, successResponse) }
        verify(exactly = 1) { paymentRepository.save(any()) }
        verify(exactly = 1) { eventPublisher.publishPaymentCompleted(any()) }
      }
    }

    `when`("PG 승인이 실패하면") {
      val failureResponse = PaymentGatewayResponse(
        success = false,
        transactionId = null,
        paymentKey = null,
        amount = payment.amount,
        responseCode = "9999",
        responseMessage = "카드 잔액 부족"
      )
      val transaction = PaymentFixtures.createFailureTransaction()
      val pendingPayment = PaymentFixtures.createPayment(
        id = paymentId,
        status = PaymentStatus.PENDING,
        paymentMethod = PaymentMethod.CARD
      )

      every { paymentRepository.findByIdWithTransactions(paymentId) } returns pendingPayment
      every { gatewayAdapter.authorize(any(), any(), any()) } returns failureResponse
      every { transactionFactory.createFromGatewayResponse(any(), any(), any()) } returns transaction
      every { paymentRepository.save(any()) } returns pendingPayment
      every { eventPublisher.publishPaymentFailed(any(), any()) } just runs

      then("결제가 실패 상태로 변경되고 실패 이벤트가 발행되어야 한다") {
        val result = service.approvePayment(paymentId, request)

        result.status shouldBe PaymentStatus.FAILED

        verify(exactly = 1) { paymentRepository.findByIdWithTransactions(paymentId) }
        verify(exactly = 1) { gatewayAdapter.authorize(any(), any(), any()) }
        verify(exactly = 1) { paymentRepository.save(any()) }
        verify(exactly = 1) { eventPublisher.publishPaymentFailed(any(), "카드 잔액 부족") }
      }
    }

    `when`("이미 완료된 결제를 승인하려고 하면") {
      val completedPayment = PaymentFixtures.createPayment(
        id = paymentId,
        status = PaymentStatus.COMPLETED
      )
      every { paymentRepository.findByIdWithTransactions(paymentId) } returns completedPayment

      then("PaymentAlreadyCompletedException이 발생해야 한다") {
        shouldThrow<PaymentAlreadyCompletedException> {
          service.approvePayment(paymentId, request)
        }

        verify(exactly = 1) { paymentRepository.findByIdWithTransactions(paymentId) }
        verify(exactly = 0) { gatewayAdapter.authorize(any(), any(), any()) }
      }
    }

    `when`("존재하지 않는 결제 ID로 승인을 시도하면") {
      every { paymentRepository.findByIdWithTransactions(paymentId) } returns null

      then("PaymentNotFoundException이 발생해야 한다") {
        shouldThrow<PaymentNotFoundException> {
          service.approvePayment(paymentId, request)
        }

        verify(exactly = 1) { paymentRepository.findByIdWithTransactions(paymentId) }
      }
    }
  }

  given("cancelPayment이 주어졌을 때") {
    val paymentId = 1L
    val reason = "사용자 요청"

    `when`("PENDING 상태의 결제를 취소하면") {
      val payment = spyk(PaymentFixtures.createPayment(
        id = paymentId,
        status = PaymentStatus.PENDING
      ))
      val transaction = PaymentFixtures.createSuccessTransaction(transactionType = TransactionType.CANCEL)

      every { paymentRepository.findByIdWithTransactions(paymentId) } returns payment
      every { transactionFactory.createSuccessTransaction(any(), any(), any(), any(), any()) } returns transaction
      every { paymentRepository.save(any()) } returns payment
      every { eventPublisher.publishPaymentCancelled(any(), any()) } just runs

      then("PG 호출 없이 취소되어야 한다") {
        val result = service.cancelPayment(paymentId, reason)

        result.status shouldBe PaymentStatus.CANCELLED

        verify(exactly = 1) { paymentRepository.findByIdWithTransactions(paymentId) }
        verify(exactly = 0) { gatewayAdapter.cancel(any(), any()) }
        verify(exactly = 1) { transactionFactory.createSuccessTransaction(TransactionType.CANCEL, any(), null, "CANCELLED", reason) }
        verify(exactly = 1) { paymentRepository.save(any()) }
        verify(exactly = 1) { eventPublisher.publishPaymentCancelled(any(), reason) }
      }
    }

    `when`("PROCESSING 상태의 결제를 취소하면") {
      val payment = PaymentFixtures.createPayment(
        id = paymentId,
        status = PaymentStatus.PENDING,
        pgPaymentKey = "toss_key_123"
      )
      payment.startProcessing("TOSS", "toss_key_123", PaymentMethod.CARD)

      val cancelResponse = PaymentGatewayResponse(
        success = true,
        transactionId = "CANCEL-TXN-001",
        paymentKey = "toss_key_123",
        amount = payment.amount,
        responseCode = "0000",
        responseMessage = "취소 성공"
      )
      val transaction = PaymentFixtures.createSuccessTransaction(transactionType = TransactionType.CANCEL)

      every { paymentRepository.findByIdWithTransactions(paymentId) } returns payment
      every { gatewayAdapter.cancel(any(), any()) } returns cancelResponse
      every { transactionFactory.createFromGatewayResponse(any(), any(), any()) } returns transaction
      every { paymentRepository.save(any()) } returns payment
      every { eventPublisher.publishPaymentCancelled(any(), any()) } just runs

      then("PG 취소 후 취소되어야 한다") {
        val result = service.cancelPayment(paymentId, reason)

        result.status shouldBe PaymentStatus.CANCELLED

        verify(exactly = 1) { paymentRepository.findByIdWithTransactions(paymentId) }
        verify(exactly = 1) { gatewayAdapter.cancel("toss_key_123", reason) }
        verify(exactly = 1) { transactionFactory.createFromGatewayResponse(TransactionType.CANCEL, payment.amount, cancelResponse) }
        verify(exactly = 1) { paymentRepository.save(any()) }
        verify(exactly = 1) { eventPublisher.publishPaymentCancelled(any(), reason) }
      }
    }

    `when`("이미 취소된 결제를 취소하려고 하면") {
      val cancelledPayment = PaymentFixtures.createPayment(
        id = paymentId,
        status = PaymentStatus.CANCELLED
      )
      every { paymentRepository.findByIdWithTransactions(paymentId) } returns cancelledPayment

      then("PaymentAlreadyCancelledException이 발생해야 한다") {
        shouldThrow<PaymentAlreadyCancelledException> {
          service.cancelPayment(paymentId, reason)
        }

        verify(exactly = 1) { paymentRepository.findByIdWithTransactions(paymentId) }
        verify(exactly = 0) { paymentRepository.save(any()) }
      }
    }

    `when`("완료된 결제를 취소하려고 하면") {
      val completedPayment = PaymentFixtures.createPayment(
        id = paymentId,
        status = PaymentStatus.COMPLETED
      )
      every { paymentRepository.findByIdWithTransactions(paymentId) } returns completedPayment

      then("InvalidPaymentStateException이 발생해야 한다") {
        val exception = shouldThrow<InvalidPaymentStateException> {
          service.cancelPayment(paymentId, reason)
        }
        exception.message shouldBe "완료된 결제는 취소할 수 없습니다. 환불을 진행해주세요"

        verify(exactly = 1) { paymentRepository.findByIdWithTransactions(paymentId) }
      }
    }
  }

  given("refundPayment이 주어졌을 때") {
    val paymentId = 1L
    val request = PaymentRefundRequest(reason = "단순 변심")

    `when`("완료된 결제를 환불하면") {
      val payment = PaymentFixtures.createPayment(
        id = paymentId,
        status = PaymentStatus.COMPLETED,
        pgPaymentKey = "toss_key_123"
      )
      val refundResponse = PaymentGatewayResponse(
        success = true,
        transactionId = "REFUND-TXN-001",
        paymentKey = "toss_key_123",
        amount = payment.amount,
        responseCode = "0000",
        responseMessage = "환불 성공"
      )
      val transaction = PaymentFixtures.createSuccessTransaction(transactionType = TransactionType.REFUND)

      every { paymentRepository.findByIdWithTransactions(paymentId) } returns payment
      every { gatewayAdapter.refund(any(), any(), any()) } returns refundResponse
      every { transactionFactory.createFromGatewayResponse(any(), any(), any()) } returns transaction
      every { paymentRepository.save(any()) } returns payment
      every { eventPublisher.publishPaymentRefunded(any(), any()) } just runs

      then("환불이 처리되고 환불 이벤트가 발행되어야 한다") {
        val result = service.refundPayment(paymentId, request)

        result.status shouldBe PaymentStatus.REFUNDED

        verify(exactly = 1) { paymentRepository.findByIdWithTransactions(paymentId) }
        verify(exactly = 1) { gatewayAdapter.refund("toss_key_123", payment.amount, request.reason) }
        verify(exactly = 1) { transactionFactory.createFromGatewayResponse(TransactionType.REFUND, payment.amount, refundResponse) }
        verify(exactly = 1) { paymentRepository.save(any()) }
        verify(exactly = 1) { eventPublisher.publishPaymentRefunded(any(), request.reason) }
      }
    }

    `when`("PG 환불이 실패하면") {
      val payment = PaymentFixtures.createPayment(
        id = paymentId,
        status = PaymentStatus.COMPLETED,
        pgPaymentKey = "toss_key_123"
      )
      val failureResponse = PaymentGatewayResponse(
        success = false,
        transactionId = null,
        paymentKey = null,
        amount = payment.amount,
        responseCode = "9999",
        responseMessage = "환불 처리 실패"
      )
      val transaction = PaymentFixtures.createFailureTransaction(transactionType = TransactionType.REFUND)

      every { paymentRepository.findByIdWithTransactions(paymentId) } returns payment
      every { gatewayAdapter.refund(any(), any(), any()) } returns failureResponse
      every { transactionFactory.createFromGatewayResponse(any(), any(), any()) } returns transaction

      then("PaymentRefundException이 발생해야 한다") {
        val exception = shouldThrow<PaymentRefundException> {
          service.refundPayment(paymentId, request)
        }
        exception.message shouldBe "PG 환불 실패: 환불 처리 실패"

        verify(exactly = 1) { paymentRepository.findByIdWithTransactions(paymentId) }
        verify(exactly = 1) { gatewayAdapter.refund(any(), any(), any()) }
        verify(exactly = 0) { paymentRepository.save(any()) }
      }
    }

    `when`("완료되지 않은 결제를 환불하려고 하면") {
      val pendingPayment = PaymentFixtures.createPayment(
        id = paymentId,
        status = PaymentStatus.PENDING
      )
      every { paymentRepository.findByIdWithTransactions(paymentId) } returns pendingPayment

      then("PaymentRefundException이 발생해야 한다") {
        shouldThrow<PaymentRefundException> {
          service.refundPayment(paymentId, request)
        }

        verify(exactly = 1) { paymentRepository.findByIdWithTransactions(paymentId) }
        verify(exactly = 0) { gatewayAdapter.refund(any(), any(), any()) }
      }
    }

    `when`("pgPaymentKey가 없는 결제를 환불하려고 하면") {
      val payment = PaymentFixtures.createPayment(
        id = paymentId,
        status = PaymentStatus.COMPLETED,
        pgPaymentKey = null
      )
      every { paymentRepository.findByIdWithTransactions(paymentId) } returns payment

      then("PaymentRefundException이 발생해야 한다") {
        val exception = shouldThrow<PaymentRefundException> {
          service.refundPayment(paymentId, request)
        }
        exception.message shouldBe "PG 결제 키가 없어 환불을 진행할 수 없습니다"

        verify(exactly = 1) { paymentRepository.findByIdWithTransactions(paymentId) }
      }
    }
  }

  given("failPayment이 주어졌을 때") {
    val paymentId = 1L
    val reason = "PG 통신 오류"

    `when`("결제를 실패 처리하면") {
      val payment = spyk(PaymentFixtures.createPayment(id = paymentId, status = PaymentStatus.PENDING))
      val transaction = PaymentFixtures.createFailureTransaction()

      every { paymentRepository.findByIdWithTransactions(paymentId) } returns payment
      every { transactionFactory.createFailureTransaction(any(), any(), any(), any(), any()) } returns transaction
      every { paymentRepository.save(any()) } returns payment
      every { eventPublisher.publishPaymentFailed(any(), any()) } just runs

      then("결제가 실패 상태로 변경되고 실패 이벤트가 발행되어야 한다") {
        val result = service.failPayment(paymentId, reason)

        result.status shouldBe PaymentStatus.FAILED

        verify(exactly = 1) { paymentRepository.findByIdWithTransactions(paymentId) }
        verify(exactly = 1) { transactionFactory.createFailureTransaction(TransactionType.AUTH, any(), null, "FAILED", reason) }
        verify(exactly = 1) { paymentRepository.save(any()) }
        verify(exactly = 1) { eventPublisher.publishPaymentFailed(any(), reason) }
      }
    }
  }
})
