package com.ecommerce.cart.client

import com.ecommerce.cart.dto.external.ProductResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

class ProductClientTest : BehaviorSpec({

    val webClientBuilder = mockk<WebClient.Builder>()
    val webClient = mockk<WebClient>()
    val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
    val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
    val responseSpec = mockk<WebClient.ResponseSpec>()

    beforeEach {
        clearMocks(
            webClientBuilder,
            webClient,
            requestHeadersUriSpec,
            requestHeadersSpec,
            responseSpec,
            answers = false
        )

        every { webClientBuilder.baseUrl(any()) } returns webClientBuilder
        every { webClientBuilder.build() } returns webClient
    }

    given("ProductClient의 getProductById 메서드가 주어졌을 때") {
        val productClient = ProductClient(webClientBuilder)
        val productId = "test-product-id"

        `when`("상품이 존재하면") {
            val mockProduct = ProductResponse(
                id = productId,
                name = "테스트 상품",
                description = "테스트 설명",
                categoryId = 1L,
                categoryName = "테스트 카테고리",
                originalPrice = BigDecimal("12000"),
                salePrice = BigDecimal("10000"),
                status = "ACTIVE",
                discountRate = BigDecimal("16.67"),
                images = emptyList(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri(any<String>(), any<String>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(ProductResponse::class.java) } returns Mono.just(mockProduct)

            then("상품 정보를 반환해야 한다") {
                val result = productClient.getProductById(productId)

                result shouldBe mockProduct
                result?.id shouldBe productId
                result?.name shouldBe "테스트 상품"
                result?.status shouldBe "ACTIVE"
            }
        }

        `when`("상품이 존재하지 않으면 (404)") {
            val notFoundException = WebClientResponseException.NotFound.create(
                404,
                "Not Found",
                null,
                null,
                null
            )

            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri(any<String>(), any<String>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(ProductResponse::class.java) } returns Mono.error(notFoundException)

            then("null을 반환해야 한다") {
                val result = productClient.getProductById(productId)

                result shouldBe null
            }
        }

        `when`("API 호출 중 예외가 발생하면") {
            every { webClient.get() } throws RuntimeException("Connection failed")

            then("ProductClientException을 발생시켜야 한다") {
                val exception = shouldThrow<ProductClientException> {
                    productClient.getProductById(productId)
                }
                exception.message shouldBe "상품 조회 실패: Connection failed"
            }
        }
    }

    given("ProductClient의 isProductAvailable 메서드가 주어졌을 때") {
        val productClient = ProductClient(webClientBuilder)
        val productId = "test-product-id"

        `when`("상품이 ACTIVE 상태이면") {
            val mockProduct = ProductResponse(
                id = productId,
                name = "테스트 상품",
                description = "테스트 설명",
                categoryId = 1L,
                categoryName = "테스트 카테고리",
                originalPrice = BigDecimal("12000"),
                salePrice = BigDecimal("10000"),
                status = "ACTIVE",
                discountRate = BigDecimal("16.67"),
                images = emptyList(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri(any<String>(), any<String>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(ProductResponse::class.java) } returns Mono.just(mockProduct)

            then("true를 반환해야 한다") {
                val result = productClient.isProductAvailable(productId)

                result shouldBe true
            }
        }

        `when`("상품이 INACTIVE 상태이면") {
            val mockProduct = ProductResponse(
                id = productId,
                name = "테스트 상품",
                description = "테스트 설명",
                categoryId = 1L,
                categoryName = "테스트 카테고리",
                originalPrice = BigDecimal("12000"),
                salePrice = BigDecimal("10000"),
                status = "INACTIVE",
                discountRate = BigDecimal("16.67"),
                images = emptyList(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri(any<String>(), any<String>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(ProductResponse::class.java) } returns Mono.just(mockProduct)

            then("false를 반환해야 한다") {
                val result = productClient.isProductAvailable(productId)

                result shouldBe false
            }
        }

        `when`("상품이 존재하지 않으면") {
            val notFoundException = WebClientResponseException.NotFound.create(
                404,
                "Not Found",
                null,
                null,
                null
            )

            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri(any<String>(), any<String>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(ProductResponse::class.java) } returns Mono.error(notFoundException)

            then("false를 반환해야 한다") {
                val result = productClient.isProductAvailable(productId)

                result shouldBe false
            }
        }

        `when`("예외가 발생하면") {
            every { webClient.get() } throws RuntimeException("Connection failed")

            then("ProductClientException을 발생시켜야 한다") {
                shouldThrow<ProductClientException> {
                    productClient.isProductAvailable(productId)
                }
            }
        }
    }
})
