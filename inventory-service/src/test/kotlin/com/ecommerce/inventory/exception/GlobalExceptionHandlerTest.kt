package com.ecommerce.inventory.exception

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
        every { request.getDescription(false) } returns "uri=/api/v1/inventory"
    }

    given("GlobalExceptionHandler의 handleInventoryNotFoundException가 주어졌을 때") {
        `when`("InventoryNotFoundException이 발생하면") {
            val exception = InventoryException.InventoryNotFoundException("PROD-001")

            then("404 NOT_FOUND 응답을 반환해야 한다") {
                val response = handler.handleInventoryNotFoundException(exception, request)

                response.statusCode shouldBe HttpStatus.NOT_FOUND
                response.body?.status shouldBe 404
                response.body?.error shouldBe "Inventory Not Found"
                response.body?.message shouldBe "재고 정보를 찾을 수 없습니다. productId=PROD-001"
                response.body?.path shouldBe "/api/v1/inventory"
            }
        }
    }

    given("GlobalExceptionHandler의 handleInventoryNotFoundByIdException가 주어졌을 때") {
        `when`("InventoryNotFoundByIdException이 발생하면") {
            val exception = InventoryException.InventoryNotFoundByIdException(100L)

            then("404 NOT_FOUND 응답을 반환해야 한다") {
                val response = handler.handleInventoryNotFoundByIdException(exception, request)

                response.statusCode shouldBe HttpStatus.NOT_FOUND
                response.body?.status shouldBe 404
                response.body?.error shouldBe "Inventory Not Found"
                response.body?.message shouldBe "재고 정보를 찾을 수 없습니다. inventoryId=100"
                response.body?.path shouldBe "/api/v1/inventory"
            }
        }
    }

    given("GlobalExceptionHandler의 handleReservationNotFoundException가 주어졌을 때") {
        `when`("ReservationNotFoundException이 발생하면") {
            val exception = InventoryException.ReservationNotFoundException(456L)

            then("404 NOT_FOUND 응답을 반환해야 한다") {
                val response = handler.handleReservationNotFoundException(exception, request)

                response.statusCode shouldBe HttpStatus.NOT_FOUND
                response.body?.status shouldBe 404
                response.body?.error shouldBe "Reservation Not Found"
                response.body?.message shouldBe "예약 정보를 찾을 수 없습니다. reservationId=456"
                response.body?.path shouldBe "/api/v1/inventory"
            }
        }
    }

    given("GlobalExceptionHandler의 handleInsufficientStockException가 주어졌을 때") {
        `when`("InsufficientStockException이 발생하면") {
            val exception = InventoryException.InsufficientStockException("PROD-001", 10, 5)

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleInsufficientStockException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Insufficient Stock"
                response.body?.message shouldBe "사용 가능한 재고가 부족합니다. productId=PROD-001, 요청=10, 가용=5"
                response.body?.path shouldBe "/api/v1/inventory"
            }
        }
    }

    given("GlobalExceptionHandler의 handleInsufficientReservedStockException가 주어졌을 때") {
        `when`("InsufficientReservedStockException이 발생하면") {
            val exception = InventoryException.InsufficientReservedStockException("PROD-001", 10, 5)

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleInsufficientReservedStockException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Insufficient Reserved Stock"
                response.body?.message shouldBe "예약된 재고가 부족합니다. productId=PROD-001, 요청=10, 예약=5"
                response.body?.path shouldBe "/api/v1/inventory"
            }
        }
    }

    given("GlobalExceptionHandler의 handleInvalidQuantityException가 주어졌을 때") {
        `when`("InvalidQuantityException이 발생하면") {
            val exception = InventoryException.InvalidQuantityException(-1)

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleInvalidQuantityException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Invalid Quantity"
                response.body?.message shouldBe "유효하지 않은 수량입니다: -1"
                response.body?.path shouldBe "/api/v1/inventory"
            }
        }
    }

    given("GlobalExceptionHandler의 handleReservationExpiredException가 주어졌을 때") {
        `when`("ReservationExpiredException이 발생하면") {
            val exception = InventoryException.ReservationExpiredException(789L)

            then("410 GONE 응답을 반환해야 한다") {
                val response = handler.handleReservationExpiredException(exception, request)

                response.statusCode shouldBe HttpStatus.GONE
                response.body?.status shouldBe 410
                response.body?.error shouldBe "Reservation Expired"
                response.body?.message shouldBe "만료된 예약입니다. reservationId=789"
                response.body?.path shouldBe "/api/v1/inventory"
            }
        }
    }

    given("GlobalExceptionHandler의 handleReservationAlreadyCompletedException가 주어졌을 때") {
        `when`("ReservationAlreadyCompletedException이 발생하면") {
            val exception = InventoryException.ReservationAlreadyCompletedException(101L)

            then("409 CONFLICT 응답을 반환해야 한다") {
                val response = handler.handleReservationAlreadyCompletedException(exception, request)

                response.statusCode shouldBe HttpStatus.CONFLICT
                response.body?.status shouldBe 409
                response.body?.error shouldBe "Reservation Already Completed"
                response.body?.message shouldBe "이미 완료된 예약입니다. reservationId=101"
                response.body?.path shouldBe "/api/v1/inventory"
            }
        }
    }

    given("GlobalExceptionHandler의 handleReservationAlreadyCancelledException가 주어졌을 때") {
        `when`("ReservationAlreadyCancelledException이 발생하면") {
            val exception = InventoryException.ReservationAlreadyCancelledException(202L)

            then("409 CONFLICT 응답을 반환해야 한다") {
                val response = handler.handleReservationAlreadyCancelledException(exception, request)

                response.statusCode shouldBe HttpStatus.CONFLICT
                response.body?.status shouldBe 409
                response.body?.error shouldBe "Reservation Already Cancelled"
                response.body?.message shouldBe "이미 취소된 예약입니다. reservationId=202"
                response.body?.path shouldBe "/api/v1/inventory"
            }
        }
    }

    given("GlobalExceptionHandler의 handleInvalidReservationStatusException가 주어졌을 때") {
        `when`("InvalidReservationStatusException이 발생하면") {
            val exception = InventoryException.InvalidReservationStatusException(303L, "PENDING", "COMPLETED")

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleInvalidReservationStatusException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Invalid Reservation Status"
                response.body?.message shouldBe "예약 상태가 올바르지 않습니다. reservationId=303, 현재=PENDING, 기대=COMPLETED"
                response.body?.path shouldBe "/api/v1/inventory"
            }
        }
    }

    given("GlobalExceptionHandler의 handleDuplicateReservationException가 주어졌을 때") {
        `when`("DuplicateReservationException이 발생하면") {
            val exception = InventoryException.DuplicateReservationException("ORD-001", "PROD-001")

            then("409 CONFLICT 응답을 반환해야 한다") {
                val response = handler.handleDuplicateReservationException(exception, request)

                response.statusCode shouldBe HttpStatus.CONFLICT
                response.body?.status shouldBe 409
                response.body?.error shouldBe "Duplicate Reservation"
                response.body?.message shouldBe "이미 존재하는 예약입니다. orderId=ORD-001, productId=PROD-001"
                response.body?.path shouldBe "/api/v1/inventory"
            }
        }
    }

    given("GlobalExceptionHandler의 handleInvalidInventoryChangeTypeException가 주어졌을 때") {
        `when`("InvalidInventoryChangeTypeException이 발생하면") {
            val exception = InventoryException.InvalidInventoryChangeTypeException("INVALID_TYPE")

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleInvalidInventoryChangeTypeException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Invalid Change Type"
                response.body?.message shouldBe "유효하지 않은 재고 변경 타입입니다: INVALID_TYPE"
                response.body?.path shouldBe "/api/v1/inventory"
            }
        }
    }

    given("GlobalExceptionHandler의 handleValidationException가 주어졌을 때") {
        `when`("MethodArgumentNotValidException이 발생하면") {
            val bindingResult = mockk<BindingResult>()
            val fieldError1 = FieldError("inventoryRequest", "productId", "상품 ID는 필수입니다")
            val fieldError2 = FieldError("inventoryRequest", "quantity", "수량은 1 이상이어야 합니다")

            every { bindingResult.fieldErrors } returns listOf(fieldError1, fieldError2)

            val exception = mockk<MethodArgumentNotValidException>()
            every { exception.bindingResult } returns bindingResult

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleValidationException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Validation Failed"
                response.body?.message shouldBe "productId: 상품 ID는 필수입니다, quantity: 수량은 1 이상이어야 합니다"
                response.body?.path shouldBe "/api/v1/inventory"
            }
        }
    }

    given("GlobalExceptionHandler의 handleIllegalArgumentException가 주어졌을 때") {
        `when`("IllegalArgumentException이 발생하면") {
            val exception = IllegalArgumentException("유효하지 않은 재고 수량입니다")

            then("400 BAD_REQUEST 응답을 반환해야 한다") {
                val response = handler.handleIllegalArgumentException(exception, request)

                response.statusCode shouldBe HttpStatus.BAD_REQUEST
                response.body?.status shouldBe 400
                response.body?.error shouldBe "Bad Request"
                response.body?.message shouldBe "유효하지 않은 재고 수량입니다"
                response.body?.path shouldBe "/api/v1/inventory"
            }
        }
    }

    given("GlobalExceptionHandler의 handleIllegalStateException가 주어졌을 때") {
        `when`("IllegalStateException이 발생하면") {
            val exception = IllegalStateException("예약 가능한 상태가 아닙니다")

            then("409 CONFLICT 응답을 반환해야 한다") {
                val response = handler.handleIllegalStateException(exception, request)

                response.statusCode shouldBe HttpStatus.CONFLICT
                response.body?.status shouldBe 409
                response.body?.error shouldBe "Conflict"
                response.body?.message shouldBe "예약 가능한 상태가 아닙니다"
                response.body?.path shouldBe "/api/v1/inventory"
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
                response.body?.path shouldBe "/api/v1/inventory"
            }
        }
    }
})
