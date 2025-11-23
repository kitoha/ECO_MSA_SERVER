package com.ecommerce.product.exception

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test/exception")
class ExceptionTestController {

  @GetMapping("/product-not-found")
  fun throwProductNotFound() {
    throw ProductNotFoundException(999L)
  }

  @GetMapping("/category-not-found")
  fun throwCategoryNotFound() {
    throw CategoryNotFoundException(999L)
  }

  @GetMapping("/invalid-price")
  fun throwInvalidPrice() {
    throw InvalidProductPriceException("판매가는 원가보다 클 수 없습니다")
  }

  @GetMapping("/invalid-status")
  fun throwInvalidStatus() {
    throw InvalidProductStatusException("유효하지 않은 상품 상태입니다")
  }

  @GetMapping("/illegal-argument")
  fun throwIllegalArgument() {
    throw IllegalArgumentException("잘못된 요청입니다")
  }

  @PostMapping("/validation")
  fun throwValidationError(@Valid @RequestBody request: ExceptionTestRequest) {
    // Validation 통과 시 아무것도 안 함
  }

  @GetMapping("/generic-error")
  fun throwGenericError() {
    throw RuntimeException("Unexpected error")
  }
}