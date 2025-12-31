package com.ecommerce.cart.exception

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.context.request.WebRequest

class GlobalExceptionHandlerTest : BehaviorSpec({

    val handler = GlobalExceptionHandler()
    val request = mockk<WebRequest>()

    beforeEach {
        every { request.getDescription(false) } returns "uri=/api/v1/cart"
    }

    given("GlobalExceptionHandler의 handleCartNotFoundException가 주어졌을 때") {
        `when`("CartNotFoundException이 발생하면") {
            val exception = CartException.CartNotFoundException(100L)

            then("404 NOT_FOUND 응답을 반환해야 한다") {
                val response = handler.handleCartNotFoundException(exception, request)

                response.statusCode shouldBe HttpStatus.NOT_FOUND
                response.body?.status shouldBe 404
                response.body?.error shouldBe "Cart Not Found"
                response.body?.message shouldBe "장바구니를 찾을 수 없습니다. userId=100"
                response.body?.path shouldBe "/api/v1/cart"
            }
        }
    }

    given("GlobalExceptionHandler의 handleCartItemNotFoundException가 주어졌을 때") {
        `when`("CartItemNotFoundException이 발생하면") {
            val exception = CartException.CartItemNotFoundException(1L)

            then("404 NOT_FOUND 응답을 반환해야 한다") {
                val response = handler.handleCartItemNotFoundException(exception, request)

                response.statusCode shouldBe HttpStatus.NOT_FOUND
                response.body?.status shouldBe 404
                response.body?.error shouldBe "Cart Item Not Found"
                response.body?.message shouldBe "장바구니 아이템을 찾을 수 없습니다. itemId=1"
                response.body?.path shouldBe "/api/v1/cart"
            }
        }
    }

    given("GlobalExceptionHandler의 handleProductNotAvailableException가 주어졌을 때") {
        `when`("ProductNotAvailableException이 발생하면") {
            val exception = CartException.ProductNotAvailableException(10L)

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleProductNotAvailableException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Product Not Available"
                response.body?.message shouldBe "상품을 사용할 수 없습니다. productId=10"
                response.body?.path shouldBe "/api/v1/cart"
            }
        }
    }

    given("GlobalExceptionHandler의 handleInvalidQuantityException가 주어졌을 때") {
        `when`("InvalidQuantityException이 발생하면") {
            val exception = CartException.InvalidQuantityException(-1)

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleInvalidQuantityException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Invalid Quantity"
                response.body?.message shouldBe "유효하지 않은 수량입니다: -1"
                response.body?.path shouldBe "/api/v1/cart"
            }
        }
    }

    given("GlobalExceptionHandler의 handleCartAlreadyExistsException가 주어졌을 때") {
        `when`("CartAlreadyExistsException이 발생하면") {
            val exception = CartException.CartAlreadyExistsException(100L)

            then("409 CONFLICT 응답을 반환해야 한다") {
                val response = handler.handleCartAlreadyExistsException(exception, request)

                response.statusCode shouldBe HttpStatus.CONFLICT
                response.body?.status shouldBe 409
                response.body?.error shouldBe "Cart Already Exists"
                response.body?.message shouldBe "이미 장바구니가 존재합니다. userId=100"
                response.body?.path shouldBe "/api/v1/cart"
            }
        }
    }

    given("GlobalExceptionHandler의 handleValidationException가 주어졌을 때") {
        `when`("MethodArgumentNotValidException이 발생하면") {
            val bindingResult = mockk<BindingResult>()
            val fieldError1 = FieldError("addItemRequest", "productId", "상품 ID는 필수입니다")
            val fieldError2 = FieldError("addItemRequest", "quantity", "수량은 1 이상이어야 합니다")
            
            every { bindingResult.fieldErrors } returns listOf(fieldError1, fieldError2)
            
            val exception = mockk<MethodArgumentNotValidException>()
            every { exception.bindingResult } returns bindingResult

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleValidationException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Validation Failed"
                response.body?.message shouldBe "productId: 상품 ID는 필수입니다, quantity: 수량은 1 이상이어야 합니다"
                response.body?.path shouldBe "/api/v1/cart"
            }
        }
    }

    given("GlobalExceptionHandler의 handleIllegalArgumentException가 주어졌을 때") {
        `when`("IllegalArgumentException이 발생하면") {
            val exception = IllegalArgumentException("수량은 1 이상이어야 합니다")

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleIllegalArgumentException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Bad Request"
                response.body?.message shouldBe "수량은 1 이상이어야 합니다"
                response.body?.path shouldBe "/api/v1/cart"
            }
        }
    }

    given("GlobalExceptionHandler의 handleGenericException가 주어졌을 때") {
        `when`("일반 Exception이 발생하면") {
            val exception = RuntimeException("Unexpected error")

            then("500 INTERNAL_SERVER_ERROR 응답을 반환해야 한다") {
                val response = handler.handleGenericException(exception, request)

                response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                response.body?.status shouldBe 500
                response.body?.error shouldBe "Internal Server Error"
                response.body?.message shouldBe "서버 내부 오류가 발생했습니다"
                response.body?.path shouldBe "/api/v1/cart"
            }
        }
    }
})
