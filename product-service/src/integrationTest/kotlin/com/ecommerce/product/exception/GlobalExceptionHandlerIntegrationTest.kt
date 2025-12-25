package com.ecommerce.product.exception

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post


@WebMvcTest(controllers = [ExceptionTestController::class])
@Import(GlobalExceptionHandler::class, ExceptionTestController::class)
class GlobalExceptionHandlerIntegrationTest : DescribeSpec() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    init {
        extension(SpringExtension)

        describe("GlobalExceptionHandler 통합 테스트") {

            context("ProductNotFoundException 발생 시") {
                it("404 NOT_FOUND와 에러 응답을 반환한다") {
                    val result = mockMvc.get("/test/exception/product-not-found")
                        .andExpect {
                            status { isNotFound() }
                            content { contentType(MediaType.APPLICATION_JSON) }
                        }
                        .andReturn()

                    val response = objectMapper.readValue<ErrorResponse>(result.response.contentAsString)

                    response.code shouldBe "PRODUCT_NOT_FOUND"
                    response.message shouldBe "상품을 찾을 수 없습니다: 999"
                }
            }

            context("CategoryNotFoundException 발생 시") {
                it("404 NOT_FOUND와 에러 응답을 반환한다") {
                    val result = mockMvc.get("/test/exception/category-not-found")
                        .andExpect {
                            status { isNotFound() }
                            content { contentType(MediaType.APPLICATION_JSON) }
                        }
                        .andReturn()

                    val response = objectMapper.readValue<ErrorResponse>(result.response.contentAsString)

                    response.code shouldBe "CATEGORY_NOT_FOUND"
                    response.message shouldBe "카테고리를 찾을 수 없습니다: 999"
                }
            }

            context("InvalidProductPriceException 발생 시") {
                it("400 BAD_REQUEST와 에러 응답을 반환한다") {
                    val result = mockMvc.get("/test/exception/invalid-price")
                        .andExpect {
                            status { isBadRequest() }
                            content { contentType(MediaType.APPLICATION_JSON) }
                        }
                        .andReturn()

                    val response = objectMapper.readValue<ErrorResponse>(result.response.contentAsString)

                    response.code shouldBe "INVALID_PRICE"
                    response.message shouldBe "판매가는 원가보다 클 수 없습니다"
                }
            }

            context("InvalidProductStatusException 발생 시") {
                it("400 BAD_REQUEST와 에러 응답을 반환한다") {
                    val result = mockMvc.get("/test/exception/invalid-status")
                        .andExpect {
                            status { isBadRequest() }
                            content { contentType(MediaType.APPLICATION_JSON) }
                        }
                        .andReturn()

                    val response = objectMapper.readValue<ErrorResponse>(result.response.contentAsString)

                    response.code shouldBe "INVALID_STATUS"
                    response.message shouldBe "유효하지 않은 상품 상태입니다"
                }
            }

            context("IllegalArgumentException 발생 시") {
                it("400 BAD_REQUEST와 에러 응답을 반환한다") {
                    val result = mockMvc.get("/test/exception/illegal-argument")
                        .andExpect {
                            status { isBadRequest() }
                            content { contentType(MediaType.APPLICATION_JSON) }
                        }
                        .andReturn()

                    val response = objectMapper.readValue<ErrorResponse>(result.response.contentAsString)

                    response.code shouldBe "INVALID_REQUEST"
                    response.message shouldBe "잘못된 요청입니다"
                }
            }

            context("MethodArgumentNotValidException (Validation) 발생 시") {
                it("400 BAD_REQUEST와 검증 실패 메시지를 반환한다") {
                    // 변경된 DTO 사용
                    val invalidRequest = ExceptionTestRequest(name = "")

                    val result = mockMvc.post("/test/exception/validation") {
                        contentType = MediaType.APPLICATION_JSON
                        content = objectMapper.writeValueAsString(invalidRequest)
                    }.andExpect {
                        status { isBadRequest() }
                        content { contentType(MediaType.APPLICATION_JSON) }
                    }.andReturn()

                    val response = objectMapper.readValue<ErrorResponse>(result.response.contentAsString)

                    response.code shouldBe "VALIDATION_FAILED"
                    response.message shouldNotBe null
                }
            }

            context("일반 Exception 발생 시") {
                it("500 INTERNAL_SERVER_ERROR와 에러 응답을 반환한다") {
                    val result = mockMvc.get("/test/exception/generic-error")
                        .andExpect {
                            status { isInternalServerError() }
                            content { contentType(MediaType.APPLICATION_JSON) }
                        }
                        .andReturn()

                    val response = objectMapper.readValue<ErrorResponse>(result.response.contentAsString)

                    response.code shouldBe "INTERNAL_SERVER_ERROR"
                    response.message shouldBe "서버 내부 오류가 발생했습니다"
                }
            }
        }
    }
}