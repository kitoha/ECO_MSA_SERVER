package com.ecommerce.product.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  @ExceptionHandler(ProductNotFoundException::class)
  fun handleProductNotFound(ex: ProductNotFoundException): ResponseEntity<ErrorResponse> {
    log.warn("Product not found: {}", ex.message)
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
      .body(ErrorResponse(
        message = ex.message ?: "상품을 찾을 수 없습니다",
        code = "PRODUCT_NOT_FOUND"
      ))
  }

  @ExceptionHandler(CategoryNotFoundException::class)
  fun handleCategoryNotFound(ex: CategoryNotFoundException): ResponseEntity<ErrorResponse> {
    log.warn("Category not found: {}", ex.message)
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
      .body(ErrorResponse(
        message = ex.message ?: "카테고리를 찾을 수 없습니다",
        code = "CATEGORY_NOT_FOUND"
      ))
  }

  @ExceptionHandler(InvalidProductPriceException::class)
  fun handleInvalidPrice(ex: InvalidProductPriceException): ResponseEntity<ErrorResponse> {
    log.warn("Invalid product price: {}", ex.message)
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body(ErrorResponse(
        message = ex.message ?: "유효하지 않은 가격입니다",
        code = "INVALID_PRICE"
      ))
  }

  @ExceptionHandler(InvalidProductStatusException::class)
  fun handleInvalidStatus(ex: InvalidProductStatusException): ResponseEntity<ErrorResponse> {
    log.warn("Invalid product status: {}", ex.message)
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body(ErrorResponse(
        message = ex.message ?: "유효하지 않은 상품 상태입니다",
        code = "INVALID_STATUS"
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
