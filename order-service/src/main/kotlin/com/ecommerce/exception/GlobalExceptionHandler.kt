package com.ecommerce.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(OrderException.OrderNotFoundException::class)
    fun handleOrderNotFoundException(
        ex: OrderException.OrderNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Order not found: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Order Not Found",
            message = ex.message ?: "주문을 찾을 수 없습니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(OrderException.OrderNotFoundByNumberException::class)
    fun handleOrderNotFoundByNumberException(
        ex: OrderException.OrderNotFoundByNumberException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Order not found by number: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Order Not Found",
            message = ex.message ?: "주문을 찾을 수 없습니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(OrderException.OrderItemNotFoundException::class)
    fun handleOrderItemNotFoundException(
        ex: OrderException.OrderItemNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Order item not found: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Order Item Not Found",
            message = ex.message ?: "주문 항목을 찾을 수 없습니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(OrderException.InvalidOrderStatusTransitionException::class)
    fun handleInvalidOrderStatusTransitionException(
        ex: OrderException.InvalidOrderStatusTransitionException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid order status transition: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Invalid Status Transition",
            message = ex.message ?: "주문 상태를 변경할 수 없습니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(OrderException.OrderNotCancellableException::class)
    fun handleOrderNotCancellableException(
        ex: OrderException.OrderNotCancellableException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Order not cancellable: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Order Not Cancellable",
            message = ex.message ?: "취소 가능한 상태가 아닙니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(OrderException.InvalidQuantityException::class)
    fun handleInvalidQuantityException(
        ex: OrderException.InvalidQuantityException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid quantity: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Invalid Quantity",
            message = ex.message ?: "유효하지 않은 수량입니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(OrderException.ProductNotAvailableException::class)
    fun handleProductNotAvailableException(
        ex: OrderException.ProductNotAvailableException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Product not available: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Product Not Available",
            message = ex.message ?: "상품을 사용할 수 없습니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(OrderException.InsufficientStockException::class)
    fun handleInsufficientStockException(
        ex: OrderException.InsufficientStockException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Insufficient stock: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Insufficient Stock",
            message = ex.message ?: "재고가 부족합니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(OrderException.OrderAlreadyExistsException::class)
    fun handleOrderAlreadyExistsException(
        ex: OrderException.OrderAlreadyExistsException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Order already exists: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = "Order Already Exists",
            message = ex.message ?: "이미 존재하는 주문입니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    @ExceptionHandler(OrderException.PaymentRequiredException::class)
    fun handlePaymentRequiredException(
        ex: OrderException.PaymentRequiredException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Payment required: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.PAYMENT_REQUIRED.value(),
            error = "Payment Required",
            message = ex.message ?: "결제가 필요합니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(error)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors
            .map { "${it.field}: ${it.defaultMessage}" }
            .joinToString(", ")

        logger.warn("Validation failed: {}", errors)

        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = errors,
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Illegal argument: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "잘못된 요청입니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
        ex: IllegalStateException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Illegal state: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = ex.message ?: "요청을 처리할 수 없는 상태입니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error occurred", ex)
        val error = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "서버 내부 오류가 발생했습니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}
