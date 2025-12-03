package com.ecommerce.client

import com.ecommerce.dto.external.ProductResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.net.URI
import java.time.LocalDateTime
import java.util.function.Function

class ProductClientTest : BehaviorSpec({

    val webClientBuilder = mockk<WebClient.Builder>()
    val webClient = mockk<WebClient>()
    val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
    val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
    val responseSpec = mockk<WebClient.ResponseSpec>()

    val productClient = ProductClient(webClientBuilder)

    beforeEach {
        every { webClientBuilder.baseUrl(any()) } returns webClientBuilder
        every { webClientBuilder.build() } returns webClient
    }

    given("ProductClient의 getProductById 메서드가 주어졌을 때") {
        val productId = "0C6JNH3N3B8G0"
        val mockProduct = ProductResponse(
            id = productId,
            name = "테스트 상품",
            description = "테스트 상품 설명",
            categoryId = 1L,
            categoryName = "전자제품",
            originalPrice = BigDecimal("60000"),
            salePrice = BigDecimal("50000"),
            discountRate = BigDecimal("16.67"),
            status = "ACTIVE",
            images = emptyList(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`("존재하는 상품 ID로 조회하면") {
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri("/api/v1/products/{id}", productId) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(ProductResponse::class.java) } returns Mono.just(mockProduct)

            then("상품 정보를 반환해야 한다") {
                val result = productClient.getProductById(productId)

                result shouldBe mockProduct
                result?.name shouldBe "테스트 상품"
                result?.status shouldBe "ACTIVE"
            }
        }

        `when`("존재하지 않는 상품 ID로 조회하면") {
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri("/api/v1/products/{id}", "INVALID") } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(ProductResponse::class.java) } returns Mono.error(
                WebClientResponseException.NotFound.create(404, "Not Found", null, null, null)
            )

            then("null을 반환해야 한다") {
                val result = productClient.getProductById("INVALID")

                result shouldBe null
            }
        }

        `when`("조회 중 예외가 발생하면") {
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri("/api/v1/products/{id}", productId) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(ProductResponse::class.java) } returns Mono.error(
                RuntimeException("Connection error")
            )

            then("ProductClientException이 발생해야 한다") {
                shouldThrow<ProductClientException> {
                    productClient.getProductById(productId)
                }
            }
        }
    }

    given("ProductClient의 getProductsByIds 메서드가 주어졌을 때") {
        val productIds = listOf("0C6JNH3N3B8G0", "0C6JNH3N3B8G1")
        val mockProducts = listOf(
            ProductResponse(
                id = "0C6JNH3N3B8G0",
                name = "상품1",
                description = "상품1 설명",
                categoryId = 1L,
                categoryName = "전자제품",
                originalPrice = BigDecimal("60000"),
                salePrice = BigDecimal("50000"),
                discountRate = BigDecimal("16.67"),
                status = "ACTIVE",
                images = emptyList(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            ProductResponse(
                id = "0C6JNH3N3B8G1",
                name = "상품2",
                description = "상품2 설명",
                categoryId = 1L,
                categoryName = "전자제품",
                originalPrice = BigDecimal("35000"),
                salePrice = BigDecimal("30000"),
                discountRate = BigDecimal("14.29"),
                status = "ACTIVE",
                images = emptyList(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        `when`("여러 상품을 조회하면") {
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri(any<Function<UriBuilder, URI>>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToFlux(ProductResponse::class.java) } returns Flux.fromIterable(mockProducts)

            then("상품 목록을 반환해야 한다") {
                val result = productClient.getProductsByIds(productIds)

                result shouldHaveSize 2
                result[0].name shouldBe "상품1"
                result[1].name shouldBe "상품2"
            }
        }

        `when`("최대 개수를 초과하면") {
            val tooManyIds = (1..101).map { "ID$it" }

            then("IllegalArgumentException이 발생해야 한다") {
                shouldThrow<IllegalArgumentException> {
                    productClient.getProductsByIds(tooManyIds)
                }
            }
        }
    }

    given("ProductClient의 isProductAvailable 메서드가 주어졌을 때") {
        val productId = "0C6JNH3N3B8G0"

        `when`("상품이 ACTIVE 상태이면") {
            val activeProduct = ProductResponse(
                id = productId,
                name = "테스트 상품",
                description = "테스트 상품 설명",
                categoryId = 1L,
                categoryName = "전자제품",
                originalPrice = BigDecimal("60000"),
                salePrice = BigDecimal("50000"),
                discountRate = BigDecimal("16.67"),
                status = "ACTIVE",
                images = emptyList(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri("/api/v1/products/{id}", productId) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(ProductResponse::class.java) } returns Mono.just(activeProduct)

            then("true를 반환해야 한다") {
                val result = productClient.isProductAvailable(productId)

                result shouldBe true
            }
        }

        `when`("상품이 INACTIVE 상태이면") {
            val inactiveProduct = ProductResponse(
                id = productId,
                name = "테스트 상품",
                description = "테스트 상품 설명",
                categoryId = 1L,
                categoryName = "전자제품",
                originalPrice = BigDecimal("60000"),
                salePrice = BigDecimal("50000"),
                discountRate = BigDecimal("16.67"),
                status = "DRAFT",
                images = emptyList(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri("/api/v1/products/{id}", productId) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(ProductResponse::class.java) } returns Mono.just(inactiveProduct)

            then("false를 반환해야 한다") {
                val result = productClient.isProductAvailable(productId)

                result shouldBe false
            }
        }
    }

    given("ProductClient의 getProductsByIdsChunked 메서드가 주어졌을 때") {
        `when`("빈 리스트를 전달하면") {
            then("빈 리스트를 반환해야 한다") {
                val result = productClient.getProductsByIdsChunked(emptyList())

                result shouldHaveSize 0
            }
        }
    }

    given("ProductClient의 searchProducts 메서드가 주어졌을 때") {
        val keyword = "노트북"
        val mockProducts = listOf(
            ProductResponse(
                id = "0C6JNH3N3B8G0",
                name = "노트북",
                description = "고성능 노트북",
                categoryId = 1L,
                categoryName = "전자제품",
                originalPrice = BigDecimal("1000000"),
                salePrice = BigDecimal("800000"),
                discountRate = BigDecimal("20.00"),
                status = "ACTIVE",
                images = emptyList(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            ProductResponse(
                id = "0C6JNH3N3B8G1",
                name = "노트북 프로",
                description = "프로급 노트북",
                categoryId = 1L,
                categoryName = "전자제품",
                originalPrice = BigDecimal("2000000"),
                salePrice = BigDecimal("1800000"),
                discountRate = BigDecimal("10.00"),
                status = "ACTIVE",
                images = emptyList(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        `when`("키워드로 상품을 검색하면") {
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri(any<Function<UriBuilder, URI>>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToFlux(ProductResponse::class.java) } returns Flux.fromIterable(mockProducts)

            then("검색 결과를 반환해야 한다") {
                val result = productClient.searchProducts(keyword)

                result shouldHaveSize 2
                result[0].name shouldBe "노트북"
                result[1].name shouldBe "노트북 프로"
            }
        }

        `when`("페이지와 사이즈를 지정하여 검색하면") {
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri(any<Function<UriBuilder, URI>>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToFlux(ProductResponse::class.java) } returns Flux.fromIterable(mockProducts)

            then("검색 결과를 반환해야 한다") {
                val result = productClient.searchProducts(keyword, page = 1, size = 10)

                result shouldHaveSize 2
                result[0].name shouldBe "노트북"
            }
        }

        `when`("검색 중 예외가 발생하면") {
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri(any<Function<UriBuilder, URI>>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToFlux(ProductResponse::class.java) } returns Flux.error(
                RuntimeException("Connection error")
            )

            then("ProductClientException이 발생해야 한다") {
                shouldThrow<ProductClientException> {
                    productClient.searchProducts(keyword)
                }
            }
        }

        `when`("검색 결과가 없으면") {
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri(any<Function<UriBuilder, URI>>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToFlux(ProductResponse::class.java) } returns Flux.empty()

            then("빈 리스트를 반환해야 한다") {
                val result = productClient.searchProducts("존재하지않는상품")

                result shouldHaveSize 0
            }
        }
    }
})
