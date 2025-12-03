package com.ecommerce.product.controller

import com.ecommerce.product.dto.*
import com.ecommerce.product.enums.ProductStatus
import com.ecommerce.product.service.ProductService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDateTime

class ProductControllerTest : BehaviorSpec({

    val productService = mockk<ProductService>()
    val productController = ProductController(productService)

    given("ProductController의 registerProduct 엔드포인트가 주어졌을 때") {
        val request = RegisterProductRequest(
            name = "노트북",
            description = "고성능 노트북",
            categoryId = 1L,
            originalPrice = BigDecimal("1000000"),
            salePrice = BigDecimal("800000"),
            status = ProductStatus.ACTIVE,
            images = listOf(
                ProductImageData(
                    imageUrl = "http://example.com/image1.jpg",
                    displayOrder = 1,
                    isThumbnail = true
                )
            )
        )

        val expectedResponse = ProductResponse(
            id = "0C6JNH3N3B8G0",
            name = "노트북",
            description = "고성능 노트북",
            categoryId = 1L,
            categoryName = "전자제품",
            originalPrice = BigDecimal("1000000"),
            salePrice = BigDecimal("800000"),
            discountRate = BigDecimal("20.00"),
            status = ProductStatus.ACTIVE,
            images = listOf(
                ProductImageResponse(
                    id = 1L,
                    imageUrl = "http://example.com/image1.jpg",
                    displayOrder = 1,
                    isThumbnail = true
                )
            ),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`("유효한 요청으로 상품을 등록하면") {
            every { productService.registerProduct(request) } returns expectedResponse

            then("상품이 등록되고 201 CREATED 응답을 반환해야 한다") {
                val response = productController.registerProduct(request)

                response.statusCode shouldBe HttpStatus.CREATED
                response.body shouldBe expectedResponse
                response.body?.name shouldBe "노트북"
                response.body?.originalPrice shouldBe BigDecimal("1000000")

                verify(exactly = 1) { productService.registerProduct(request) }
            }
        }
    }

    given("ProductController의 getProduct 엔드포인트가 주어졌을 때") {
        val productIdString = "0C6JNH3N3B8G0"
        val expectedResponse = ProductResponse(
            id = "0C6JNH3N3B8G0",
            name = "노트북",
            description = "고성능 노트북",
            categoryId = 1L,
            categoryName = "전자제품",
            originalPrice = BigDecimal("1000000"),
            salePrice = BigDecimal("800000"),
            discountRate = BigDecimal("20.00"),
            status = ProductStatus.ACTIVE,
            images = emptyList(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`("상품 ID로 조회하면") {
            every { productService.getProduct(productIdString) } returns expectedResponse

            then("상품 정보를 반환하고 200 OK 응답을 반환해야 한다") {
                val response = productController.getProduct(productIdString)

                response.statusCode shouldBe HttpStatus.OK
                response.body shouldBe expectedResponse
                response.body?.id shouldBe productIdString
                response.body?.name shouldBe "노트북"

                verify(exactly = 1) { productService.getProduct(productIdString) }
            }
        }
    }

    given("ProductController의 getAllProducts 엔드포인트가 주어졌을 때") {
        val expectedResponses = listOf(
            ProductResponse(
                id = "0C6JNH3N3B8G0",
                name = "노트북",
                description = "고성능 노트북",
                categoryId = 1L,
                categoryName = "전자제품",
                originalPrice = BigDecimal("1000000"),
                salePrice = BigDecimal("800000"),
                discountRate = BigDecimal("20.00"),
                status = ProductStatus.ACTIVE,
                images = emptyList(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            ProductResponse(
                id = "0C6JNH3N3B8G1",
                name = "마우스",
                description = "무선 마우스",
                categoryId = 1L,
                categoryName = "전자제품",
                originalPrice = BigDecimal("50000"),
                salePrice = BigDecimal("40000"),
                discountRate = BigDecimal("20.00"),
                status = ProductStatus.ACTIVE,
                images = emptyList(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        `when`("모든 상품을 조회하면") {
            every { productService.getAllProducts() } returns expectedResponses

            then("상품 목록을 반환하고 200 OK 응답을 반환해야 한다") {
                val response = productController.getAllProducts()

                response.statusCode shouldBe HttpStatus.OK
                response.body shouldHaveSize 2
                response.body?.get(0)?.name shouldBe "노트북"
                response.body?.get(1)?.name shouldBe "마우스"

                verify(exactly = 1) { productService.getAllProducts() }
            }
        }
    }

    given("ProductController의 searchProducts 엔드포인트가 주어졌을 때") {
        val searchRequest = ProductSearchRequest(
            categoryId = 1L,
            keyword = "노트북",
            minPrice = BigDecimal("500000"),
            maxPrice = BigDecimal("1500000"),
            status = ProductStatus.ACTIVE
        )

        val expectedResponses = listOf(
            ProductResponse(
                id = "0C6JNH3N3B8G0",
                name = "노트북",
                description = "고성능 노트북",
                categoryId = 1L,
                categoryName = "전자제품",
                originalPrice = BigDecimal("1000000"),
                salePrice = BigDecimal("800000"),
                discountRate = BigDecimal("20.00"),
                status = ProductStatus.ACTIVE,
                images = emptyList(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        `when`("검색 조건으로 상품을 검색하면") {
            every { productService.searchProducts(searchRequest) } returns expectedResponses

            then("검색 결과를 반환하고 200 OK 응답을 반환해야 한다") {
                val response = productController.searchProducts(searchRequest)

                response.statusCode shouldBe HttpStatus.OK
                response.body shouldHaveSize 1
                response.body?.get(0)?.name shouldBe "노트북"

                verify(exactly = 1) { productService.searchProducts(searchRequest) }
            }
        }
    }

    given("ProductController의 updateProduct 엔드포인트가 주어졌을 때") {
        val productIdString = "0C6JNH3N3B8G0"
        val updateRequest = UpdateProductRequest(
            name = "프리미엄 노트북",
            description = "최고급 노트북",
            originalPrice = BigDecimal("1500000"),
            salePrice = BigDecimal("1200000"),
            status = ProductStatus.ACTIVE
        )

        val expectedResponse = ProductResponse(
            id = "0C6JNH3N3B8G0",
            name = "프리미엄 노트북",
            description = "최고급 노트북",
            categoryId = 1L,
            categoryName = "전자제품",
            originalPrice = BigDecimal("1500000"),
            salePrice = BigDecimal("1200000"),
            discountRate = BigDecimal("20.00"),
            status = ProductStatus.ACTIVE,
            images = emptyList(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`("상품 정보를 업데이트하면") {
            every { productService.updateProduct(productIdString, updateRequest) } returns expectedResponse

            then("업데이트된 상품 정보를 반환하고 200 OK 응답을 반환해야 한다") {
                val response = productController.updateProduct(productIdString, updateRequest)

                response.statusCode shouldBe HttpStatus.OK
                response.body shouldBe expectedResponse
                response.body?.name shouldBe "프리미엄 노트북"
                response.body?.originalPrice shouldBe BigDecimal("1500000")

                verify(exactly = 1) { productService.updateProduct(productIdString, updateRequest) }
            }
        }
    }

    given("ProductController의 deleteProduct 엔드포인트가 주어졌을 때") {
        val productIdString = "0C6JNH3N3B8G0"

        `when`("상품을 삭제하면") {
            every { productService.deleteProduct(productIdString) } just runs

            then("204 NO CONTENT 응답을 반환해야 한다") {
                val response = productController.deleteProduct(productIdString)

                response.statusCode shouldBe HttpStatus.NO_CONTENT
                response.body shouldBe null

                verify(exactly = 1) { productService.deleteProduct(productIdString) }
            }
        }
    }
})
