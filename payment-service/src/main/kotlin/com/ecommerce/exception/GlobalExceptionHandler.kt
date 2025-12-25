package com.ecommerce.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(PaymentNotFoundException::class)
    fun handlePaymentNotFound(ex: PaymentNotFoundException): ResponseEntity<ErrorResponse> {
        log.warn("Payment not found: {}", ex.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                message = ex.message ?: "결제 정보를 찾을 수 없습니다",
                code = "PAYMENT_NOT_FOUND"
            ))
    }

    @ExceptionHandler(PaymentNotFoundByOrderIdException::class)
    fun handlePaymentNotFoundByOrderId(ex: PaymentNotFoundByOrderIdException): ResponseEntity<ErrorResponse> {
        log.warn("Payment not found by order ID: {}", ex.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                message = ex.message ?: "주문 ID로 결제 정보를 찾을 수 없습니다",
                code = "PAYMENT_NOT_FOUND_BY_ORDER_ID"
            ))
    }

    @ExceptionHandler(PaymentNotFoundByPgPaymentKeyException::class)
    fun handlePaymentNotFoundByPgPaymentKey(ex: PaymentNotFoundByPgPaymentKeyException): ResponseEntity<ErrorResponse> {
        log.warn("Payment not found by PG payment key: {}", ex.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                message = ex.message ?: "PG 결제 키로 결제 정보를 찾을 수 없습니다",
                code = "PAYMENT_NOT_FOUND_BY_PG_KEY"
            ))
    }

    @ExceptionHandler(InvalidPaymentStateException::class)
    fun handleInvalidPaymentState(ex: InvalidPaymentStateException): ResponseEntity<ErrorResponse> {
        log.warn("Invalid payment state: {}", ex.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                message = ex.message ?: "유효하지 않은 결제 상태입니다",
                code = "INVALID_PAYMENT_STATE"
            ))
    }

    @ExceptionHandler(InvalidPaymentAmountException::class)
    fun handleInvalidPaymentAmount(ex: InvalidPaymentAmountException): ResponseEntity<ErrorResponse> {
        log.warn("Invalid payment amount: {}", ex.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                message = ex.message ?: "유효하지 않은 결제 금액입니다",
                code = "INVALID_PAYMENT_AMOUNT"
            ))
    }

    @ExceptionHandler(PaymentProcessingException::class)
    fun handlePaymentProcessing(ex: PaymentProcessingException): ResponseEntity<ErrorResponse> {
        log.error("Payment processing error: {}", ex.message)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                message = ex.message ?: "결제 처리 중 오류가 발생했습니다",
                code = "PAYMENT_PROCESSING_ERROR"
            ))
    }

    @ExceptionHandler(PaymentAlreadyCompletedException::class)
    fun handlePaymentAlreadyCompleted(ex: PaymentAlreadyCompletedException): ResponseEntity<ErrorResponse> {
        log.warn("Payment already completed: {}", ex.message)
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(
                message = ex.message ?: "이미 완료된 결제입니다",
                code = "PAYMENT_ALREADY_COMPLETED"
            ))
    }

    @ExceptionHandler(PaymentAlreadyCancelledException::class)
    fun handlePaymentAlreadyCancelled(ex: PaymentAlreadyCancelledException): ResponseEntity<ErrorResponse> {
        log.warn("Payment already cancelled: {}", ex.message)
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(
                message = ex.message ?: "이미 취소된 결제입니다",
                code = "PAYMENT_ALREADY_CANCELLED"
            ))
    }

    @ExceptionHandler(PaymentRefundException::class)
    fun handlePaymentRefund(ex: PaymentRefundException): ResponseEntity<ErrorResponse> {
        log.error("Payment refund error: {}", ex.message)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                message = ex.message ?: "환불 처리 중 오류가 발생했습니다",
                code = "PAYMENT_REFUND_ERROR"
            ))
    }

    @ExceptionHandler(DuplicateOrderPaymentException::class)
    fun handleDuplicateOrderPayment(ex: DuplicateOrderPaymentException): ResponseEntity<ErrorResponse> {
        log.warn("Duplicate payment for order: {}", ex.message)
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(
                message = ex.message ?: "주문에 대한 결제가 이미 존재합니다",
                code = "DUPLICATE_ORDER_PAYMENT"
            ))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        log.warn("Illegal argument: {}", ex.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                message = ex.message ?: "잘못된 요청입니다",
                code = "INVALID_REQUEST"
            ))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        log.warn("Validation failed: {}", errors)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                message = errors,
                code = "VALIDATION_FAILED"
            ))
    }

    @ExceptionHandler(PaymentCancellationException::class)
    fun handlePaymentCancellation(ex: PaymentCancellationException): ResponseEntity<ErrorResponse> {
        log.error("Payment cancellation error: {}", ex.message)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                message = ex.message ?: "결제 취소 중 오류가 발생했습니다",
                code = "PAYMENT_CANCELLATION_ERROR"
            ))
    }

    @ExceptionHandler(PaymentGatewayException::class)
    fun handlePaymentGateway(ex: PaymentGatewayException): ResponseEntity<ErrorResponse> {
        log.error("Payment gateway error: {}", ex.message)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                message = ex.message ?: "PG 게이트웨이 오류가 발생했습니다",
                code = "PAYMENT_GATEWAY_ERROR"
            ))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected error occurred", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                message = "서버 내부 오류가 발생했습니다",
                code = "INTERNAL_SERVER_ERROR"
            ))
    }
}
