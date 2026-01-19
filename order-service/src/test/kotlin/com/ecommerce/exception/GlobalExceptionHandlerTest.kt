package com.ecommerce.exception

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
        every { request.getDescription(false) } returns "uri=/api/v1/orders"
    }

    given("GlobalExceptionHandler의 handleOrderNotFoundException가 주어졌을 때") {
        `when`("OrderNotFoundException이 발생하면") {
            val exception = OrderException.OrderNotFoundException(100L)

            then("404 NOT_FOUND 응답을 반환해야 한다") {
                val response = handler.handleOrderNotFoundException(exception, request)

                response.statusCode shouldBe HttpStatus.NOT_FOUND
                response.body?.status shouldBe 404
                response.body?.error shouldBe "Order Not Found"
                response.body?.message shouldBe "주문을 찾을 수 없습니다. orderId=100"
                response.body?.path shouldBe "/api/v1/orders"
            }
        }
    }

    given("GlobalExceptionHandler의 handleOrderNotFoundByNumberException가 주어졌을 때") {
        `when`("OrderNotFoundByNumberException이 발생하면") {
            val exception = OrderException.OrderNotFoundByNumberException("ORD-20240101-001")

            then("404 NOT_FOUND 응답을 반환해야 한다") {
                val response = handler.handleOrderNotFoundByNumberException(exception, request)

                response.statusCode shouldBe HttpStatus.NOT_FOUND
                response.body?.status shouldBe 404
                response.body?.error shouldBe "Order Not Found"
                response.body?.message shouldBe "주문을 찾을 수 없습니다. orderNumber=ORD-20240101-001"
                response.body?.path shouldBe "/api/v1/orders"
            }
        }
    }

    given("GlobalExceptionHandler의 handleOrderItemNotFoundException가 주어졌을 때") {
        `when`("OrderItemNotFoundException이 발생하면") {
            val exception = OrderException.OrderItemNotFoundException(1L)

            then("404 NOT_FOUND 응답을 반환해야 한다") {
                val response = handler.handleOrderItemNotFoundException(exception, request)

                response.statusCode shouldBe HttpStatus.NOT_FOUND
                response.body?.status shouldBe 404
                response.body?.error shouldBe "Order Item Not Found"
                response.body?.message shouldBe "주문 항목을 찾을 수 없습니다. itemId=1"
                response.body?.path shouldBe "/api/v1/orders"
            }
        }
    }

    given("GlobalExceptionHandler의 handleInvalidOrderStatusTransitionException가 주어졌을 때") {
        `when`("InvalidOrderStatusTransitionException이 발생하면") {
            val exception = OrderException.InvalidOrderStatusTransitionException("PENDING", "DELIVERED")

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleInvalidOrderStatusTransitionException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Invalid Status Transition"
                response.body?.message shouldBe "주문 상태를 PENDING 에서 DELIVERED 로 변경할 수 없습니다"
                response.body?.path shouldBe "/api/v1/orders"
            }
        }
    }

    given("GlobalExceptionHandler의 handleOrderNotCancellableException가 주어졌을 때") {
        `when`("OrderNotCancellableException이 발생하면") {
            val exception = OrderException.OrderNotCancellableException("ORD-001", "SHIPPED")

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleOrderNotCancellableException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Order Not Cancellable"
                response.body?.message shouldBe "취소 가능한 상태가 아닙니다. orderNumber=ORD-001, status=SHIPPED"
                response.body?.path shouldBe "/api/v1/orders"
            }
        }
    }

    given("GlobalExceptionHandler의 handleInvalidQuantityException가 주어졌을 때") {
        `when`("InvalidQuantityException이 발생하면") {
            val exception = OrderException.InvalidQuantityException(-1)

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleInvalidQuantityException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Invalid Quantity"
                response.body?.message shouldBe "유효하지 않은 수량입니다: -1"
                response.body?.path shouldBe "/api/v1/orders"
            }
        }
    }

    given("GlobalExceptionHandler의 handleProductNotAvailableException가 주어졌을 때") {
        `when`("ProductNotAvailableException이 발생하면") {
            val exception = OrderException.ProductNotAvailableException("PROD-001")

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleProductNotAvailableException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Product Not Available"
                response.body?.message shouldBe "상품을 사용할 수 없습니다. productId=PROD-001"
                response.body?.path shouldBe "/api/v1/orders"
            }
        }
    }

    given("GlobalExceptionHandler의 handleInsufficientStockException가 주어졌을 때") {
        `when`("InsufficientStockException이 발생하면") {
            val exception = OrderException.InsufficientStockException("PROD-001", 10, 5)

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleInsufficientStockException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Insufficient Stock"
                response.body?.message shouldBe "재고가 부족합니다. productId=PROD-001, 요청=10, 가용=5"
                response.body?.path shouldBe "/api/v1/orders"
            }
        }
    }

    given("GlobalExceptionHandler의 handleOrderAlreadyExistsException가 주어졌을 때") {
        `when`("OrderAlreadyExistsException이 발생하면") {
            val exception = OrderException.OrderAlreadyExistsException("ORD-001")

            then("409 CONFLICT 응답을 반환해야 한다") {
                val response = handler.handleOrderAlreadyExistsException(exception, request)

                response.statusCode shouldBe HttpStatus.CONFLICT
                response.body?.status shouldBe 409
                response.body?.error shouldBe "Order Already Exists"
                response.body?.message shouldBe "이미 존재하는 주문번호입니다. orderNumber=ORD-001"
                response.body?.path shouldBe "/api/v1/orders"
            }
        }
    }

    given("GlobalExceptionHandler의 handlePaymentRequiredException가 주어졌을 때") {
        `when`("PaymentRequiredException이 발생하면") {
            val exception = OrderException.PaymentRequiredException("ORD-001")

            then("402 PAYMENT_REQUIRED 응답을 반환해야 한다") {
                val response = handler.handlePaymentRequiredException(exception, request)

                response.statusCode shouldBe HttpStatus.PAYMENT_REQUIRED
                response.body?.status shouldBe 402
                response.body?.error shouldBe "Payment Required"
                response.body?.message shouldBe "결제가 필요합니다. orderNumber=ORD-001"
                response.body?.path shouldBe "/api/v1/orders"
            }
        }
    }

    given("GlobalExceptionHandler의 handleValidationException가 주어졌을 때") {
        `when`("MethodArgumentNotValidException이 발생하면") {
            val bindingResult = mockk<BindingResult>()
            val fieldError1 = FieldError("createOrderRequest", "userId", "사용자 ID는 필수입니다")
            val fieldError2 = FieldError("createOrderRequest", "shippingAddress", "배송지 주소는 필수입니다")

            every { bindingResult.fieldErrors } returns listOf(fieldError1, fieldError2)

            val exception = mockk<MethodArgumentNotValidException>()
            every { exception.bindingResult } returns bindingResult

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleValidationException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Validation Failed"
                response.body?.message shouldBe "userId: 사용자 ID는 필수입니다, shippingAddress: 배송지 주소는 필수입니다"
                response.body?.path shouldBe "/api/v1/orders"
            }
        }
    }

    given("GlobalExceptionHandler의 handleIllegalArgumentException가 주어졌을 때") {
        `when`("IllegalArgumentException이 발생하면") {
            val exception = IllegalArgumentException("존재하지 않는 주문입니다: 100")

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleIllegalArgumentException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Bad Request"
                response.body?.message shouldBe "존재하지 않는 주문입니다: 100"
                response.body?.path shouldBe "/api/v1/orders"
            }
        }
    }

    given("GlobalExceptionHandler의 handleIllegalStateException가 주어졌을 때") {
        `when`("IllegalStateException이 발생하면") {
            val exception = IllegalStateException("취소 가능한 상태가 아닙니다: SHIPPED")

            then("409 CONFLICT 응답을 반환해야 한다") {
                val response = handler.handleIllegalStateException(exception, request)

                response.statusCode shouldBe HttpStatus.CONFLICT
                response.body?.status shouldBe 409
                response.body?.error shouldBe "Conflict"
                response.body?.message shouldBe "취소 가능한 상태가 아닙니다: SHIPPED"
                response.body?.path shouldBe "/api/v1/orders"
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
                response.body?.path shouldBe "/api/v1/orders"
            }
        }
    }
})
