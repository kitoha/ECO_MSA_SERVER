package com.ecommerce.exception

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

class GlobalExceptionHandlerTest : BehaviorSpec({
  isolationMode = IsolationMode.InstancePerLeaf

  val handler = GlobalExceptionHandler()

  given("GlobalExceptionHandler가 주어졌을 때") {

    `when`("PaymentNotFoundException이 발생하면") {
      val exception = PaymentNotFoundException(1L)

      then("404 NOT_FOUND를 반환해야 한다") {
        val response = handler.handlePaymentNotFound(exception)

        response.statusCode shouldBe HttpStatus.NOT_FOUND
        response.body?.code shouldBe "PAYMENT_NOT_FOUND"
        response.body?.message shouldBe "결제 정보를 찾을 수 없습니다: 1"
      }
    }

    `when`("PaymentNotFoundByOrderIdException이 발생하면") {
      val exception = PaymentNotFoundByOrderIdException("ORDER-001")

      then("404 NOT_FOUND를 반환해야 한다") {
        val response = handler.handlePaymentNotFoundByOrderId(exception)

        response.statusCode shouldBe HttpStatus.NOT_FOUND
        response.body?.code shouldBe "PAYMENT_NOT_FOUND_BY_ORDER_ID"
        response.body?.message shouldBe "주문 ID로 결제 정보를 찾을 수 없습니다: ORDER-001"
      }
    }

    `when`("PaymentNotFoundByPgPaymentKeyException이 발생하면") {
      val exception = PaymentNotFoundByPgPaymentKeyException("PG-KEY-001")

      then("404 NOT_FOUND를 반환해야 한다") {
        val response = handler.handlePaymentNotFoundByPgPaymentKey(exception)

        response.statusCode shouldBe HttpStatus.NOT_FOUND
        response.body?.code shouldBe "PAYMENT_NOT_FOUND_BY_PG_KEY"
        response.body?.message shouldBe "PG 결제 키로 결제 정보를 찾을 수 없습니다: PG-KEY-001"
      }
    }

    `when`("InvalidPaymentStateException이 발생하면") {
      val exception = InvalidPaymentStateException("잘못된 상태 전이")

      then("400 BAD_REQUEST를 반환해야 한다") {
        val response = handler.handleInvalidPaymentState(exception)

        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body?.code shouldBe "INVALID_PAYMENT_STATE"
        response.body?.message shouldBe "잘못된 상태 전이"
      }
    }

    `when`("InvalidPaymentAmountException이 발생하면") {
      val exception = InvalidPaymentAmountException("금액이 0보다 작습니다")

      then("400 BAD_REQUEST를 반환해야 한다") {
        val response = handler.handleInvalidPaymentAmount(exception)

        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body?.code shouldBe "INVALID_PAYMENT_AMOUNT"
        response.body?.message shouldBe "금액이 0보다 작습니다"
      }
    }

    `when`("PaymentProcessingException이 발생하면") {
      val exception = PaymentProcessingException("PG 통신 오류")

      then("500 INTERNAL_SERVER_ERROR를 반환해야 한다") {
        val response = handler.handlePaymentProcessing(exception)

        response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        response.body?.code shouldBe "PAYMENT_PROCESSING_ERROR"
        response.body?.message shouldBe "PG 통신 오류"
      }
    }

    `when`("PaymentAlreadyCompletedException이 발생하면") {
      val exception = PaymentAlreadyCompletedException(1L)

      then("409 CONFLICT를 반환해야 한다") {
        val response = handler.handlePaymentAlreadyCompleted(exception)

        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body?.code shouldBe "PAYMENT_ALREADY_COMPLETED"
        response.body?.message shouldBe "이미 완료된 결제입니다: 1"
      }
    }

    `when`("PaymentAlreadyCancelledException이 발생하면") {
      val exception = PaymentAlreadyCancelledException(1L)

      then("409 CONFLICT를 반환해야 한다") {
        val response = handler.handlePaymentAlreadyCancelled(exception)

        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body?.code shouldBe "PAYMENT_ALREADY_CANCELLED"
        response.body?.message shouldBe "이미 취소된 결제입니다: 1"
      }
    }

    `when`("PaymentRefundException이 발생하면") {
      val exception = PaymentRefundException("환불 처리 실패")

      then("500 INTERNAL_SERVER_ERROR를 반환해야 한다") {
        val response = handler.handlePaymentRefund(exception)

        response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        response.body?.code shouldBe "PAYMENT_REFUND_ERROR"
        response.body?.message shouldBe "환불 처리 실패"
      }
    }

    `when`("DuplicateOrderPaymentException이 발생하면") {
      val exception = DuplicateOrderPaymentException("ORDER-001")

      then("409 CONFLICT를 반환해야 한다") {
        val response = handler.handleDuplicateOrderPayment(exception)

        response.statusCode shouldBe HttpStatus.CONFLICT
        response.body?.code shouldBe "DUPLICATE_ORDER_PAYMENT"
        response.body?.message shouldBe "주문 ID에 대한 결제가 이미 존재합니다: ORDER-001"
      }
    }

    `when`("IllegalArgumentException이 발생하면") {
      val exception = IllegalArgumentException("잘못된 인자")

      then("400 BAD_REQUEST를 반환해야 한다") {
        val response = handler.handleIllegalArgument(exception)

        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body?.code shouldBe "INVALID_REQUEST"
        response.body?.message shouldBe "잘못된 인자"
      }
    }

    `when`("PaymentCancellationException이 발생하면") {
      val exception = PaymentCancellationException("취소 실패")

      then("500 INTERNAL_SERVER_ERROR를 반환해야 한다") {
        val response = handler.handlePaymentCancellation(exception)

        response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        response.body?.code shouldBe "PAYMENT_CANCELLATION_ERROR"
        response.body?.message shouldBe "취소 실패"
      }
    }

    `when`("PaymentGatewayException이 발생하면") {
      val exception = PaymentGatewayException("PG 게이트웨이 오류")

      then("500 INTERNAL_SERVER_ERROR를 반환해야 한다") {
        val response = handler.handlePaymentGateway(exception)

        response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        response.body?.code shouldBe "PAYMENT_GATEWAY_ERROR"
        response.body?.message shouldBe "PG 게이트웨이 오류"
      }
    }

    `when`("일반 Exception이 발생하면") {
      val exception = Exception("예상치 못한 오류")

      then("500 INTERNAL_SERVER_ERROR를 반환해야 한다") {
        val response = handler.handleGenericException(exception)

        response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        response.body?.code shouldBe "INTERNAL_SERVER_ERROR"
        response.body?.message shouldBe "서버 내부 오류가 발생했습니다"
      }
    }
  }
})
