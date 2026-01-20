package com.ecommerce.inventory.exception

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

    @ExceptionHandler(InventoryException.InventoryNotFoundException::class)
    fun handleInventoryNotFoundException(
        ex: InventoryException.InventoryNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Inventory not found: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Inventory Not Found",
            message = ex.message ?: "재고 정보를 찾을 수 없습니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(InventoryException.InventoryNotFoundByIdException::class)
    fun handleInventoryNotFoundByIdException(
        ex: InventoryException.InventoryNotFoundByIdException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Inventory not found by id: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Inventory Not Found",
            message = ex.message ?: "재고 정보를 찾을 수 없습니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(InventoryException.ReservationNotFoundException::class)
    fun handleReservationNotFoundException(
        ex: InventoryException.ReservationNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Reservation not found: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Reservation Not Found",
            message = ex.message ?: "예약 정보를 찾을 수 없습니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(InventoryException.InsufficientStockException::class)
    fun handleInsufficientStockException(
        ex: InventoryException.InsufficientStockException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Insufficient stock: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Insufficient Stock",
            message = ex.message ?: "사용 가능한 재고가 부족합니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(InventoryException.InsufficientReservedStockException::class)
    fun handleInsufficientReservedStockException(
        ex: InventoryException.InsufficientReservedStockException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Insufficient reserved stock: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Insufficient Reserved Stock",
            message = ex.message ?: "예약된 재고가 부족합니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(InventoryException.InvalidQuantityException::class)
    fun handleInvalidQuantityException(
        ex: InventoryException.InvalidQuantityException,
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

    @ExceptionHandler(InventoryException.ReservationExpiredException::class)
    fun handleReservationExpiredException(
        ex: InventoryException.ReservationExpiredException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Reservation expired: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.GONE.value(),
            error = "Reservation Expired",
            message = ex.message ?: "만료된 예약입니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.GONE).body(error)
    }

    @ExceptionHandler(InventoryException.ReservationAlreadyCompletedException::class)
    fun handleReservationAlreadyCompletedException(
        ex: InventoryException.ReservationAlreadyCompletedException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Reservation already completed: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = "Reservation Already Completed",
            message = ex.message ?: "이미 완료된 예약입니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    @ExceptionHandler(InventoryException.ReservationAlreadyCancelledException::class)
    fun handleReservationAlreadyCancelledException(
    ex: InventoryException.ReservationAlreadyCancelledException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Reservation already cancelled: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = "Reservation Already Cancelled",
            message = ex.message ?: "이미 취소된 예약입니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    @ExceptionHandler(InventoryException.InvalidReservationStatusException::class)
    fun handleInvalidReservationStatusException(
        ex: InventoryException.InvalidReservationStatusException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid reservation status: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Invalid Reservation Status",
            message = ex.message ?: "예약 상태가 올바르지 않습니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(InventoryException.DuplicateReservationException::class)
    fun handleDuplicateReservationException(
        ex: InventoryException.DuplicateReservationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Duplicate reservation: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = "Duplicate Reservation",
            message = ex.message ?: "이미 존재하는 예약입니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    @ExceptionHandler(InventoryException.InvalidInventoryChangeTypeException::class)
    fun handleInvalidInventoryChangeTypeException(
        ex: InventoryException.InvalidInventoryChangeTypeException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid inventory change type: {}", ex.message)
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Invalid Change Type",
            message = ex.message ?: "유효하지 않은 재고 변경 타입입니다",
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
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
