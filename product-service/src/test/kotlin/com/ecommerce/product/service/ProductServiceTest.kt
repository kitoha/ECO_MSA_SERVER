package com.ecommerce.product.service

import com.ecommerce.product.dto.ProductImageData
import com.ecommerce.product.dto.ProductSearchRequest
import com.ecommerce.product.dto.RegisterProductRequest
import com.ecommerce.product.dto.UpdateProductRequest
import com.ecommerce.product.entity.Category
import com.ecommerce.product.entity.Product
import com.ecommerce.product.enums.CategoryStatus
import com.ecommerce.product.enums.ProductStatus
import com.ecommerce.product.repository.CategoryRepository
import com.ecommerce.product.repository.ProductRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal

class ProductServiceTest : BehaviorSpec({

    val productRepository = mockk<ProductRepository>()
    val categoryRepository = mockk<CategoryRepository>()
    val idGenerator = mockk<com.ecommerce.product.generator.TsidGenerator>()
    val productService = ProductService(productRepository, categoryRepository, idGenerator)

    beforeEach {
        clearMocks(productRepository, categoryRepository ,idGenerator, answers = false)
    }

    given("ProductService의 registerProduct 메서드가 주어졌을 때") {
        val category = Category(
            id = 1L,
            name = "전자제품",
            slug = "electronics",
            status = CategoryStatus.ACTIVE
        )

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

        `when`("유효한 요청으로 상품을 등록하면") {
            val productId = 236372517419679744L
            val productIdString = "0C6JNH3N3B8G0"
            val savedProduct = Product(
                id = productId,
                name = request.name,
                description = request.description,
                category = category,
                originalPrice = request.originalPrice,
                salePrice = request.salePrice,
                status = request.status
            )

            every { idGenerator.generate() } returns productId
            every { idGenerator.encode(productId) } returns productIdString
            every { categoryRepository.findByIdAndNotDeleted(1L) } returns category
            every { productRepository.save(any()) } returns savedProduct

            then("상품이 정상적으로 등록되어야 한다") {
                val response = productService.registerProduct(request)

                response.name shouldBe "노트북"
                response.originalPrice shouldBe BigDecimal("1000000")
                response.salePrice shouldBe BigDecimal("800000")
                response.status shouldBe ProductStatus.ACTIVE

                verify(exactly = 1) { idGenerator.generate() }
                verify(exactly = 1) { categoryRepository.findByIdAndNotDeleted(1L) }
                verify(exactly = 1) { productRepository.save(any()) }
            }
        }

        `when`("존재하지 않는 카테고리로 상품을 등록하면") {
            every { categoryRepository.findByIdAndNotDeleted(999L) } returns null

            then("예외가 발생해야 한다") {
                val invalidRequest = request.copy(categoryId = 999L)

                val exception = shouldThrow<IllegalArgumentException> {
                    productService.registerProduct(invalidRequest)
                }
                exception.message shouldBe "존재하지 않는 카테고리입니다: 999"
            }
        }
    }

    given("ProductService의 getProduct 메서드가 주어졌을 때") {
        val productId = 236372517419679744L
        val productIdString = "0C6JNH3N3B8G0"
        val category = Category(
            id = 1L,
            name = "전자제품",
            slug = "electronics",
            status = CategoryStatus.ACTIVE
        )

        val product = Product(
            id = productId,
            name = "노트북",
            description = "고성능 노트북",
            category = category,
            originalPrice = BigDecimal("1000000"),
            salePrice = BigDecimal("800000"),
            status = ProductStatus.ACTIVE
        )

        `when`("존재하는 상품 ID로 조회하면") {
            every { idGenerator.decode(productIdString) } returns productId
            every { idGenerator.encode(productId) } returns productIdString
            every { productRepository.findByIdAndNotDeleted(productId) } returns product

            then("상품 정보가 반환되어야 한다") {
                val response = productService.getProduct(productIdString)

                response.name shouldBe "노트북"
                response.categoryId shouldBe 1L

                verify(exactly = 1) { idGenerator.decode(productIdString) }
                verify(exactly = 1) { idGenerator.encode(productId) }
                verify(exactly = 1) { productRepository.findByIdAndNotDeleted(productId) }
            }
        }

        `when`("존재하지 않는 상품 ID로 조회하면") {
            val invalidIdString = "INVALID"
            val invalidId = 999L
            every { idGenerator.decode(invalidIdString) } returns invalidId
            every { productRepository.findByIdAndNotDeleted(invalidId) } returns null

            then("예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    productService.getProduct(invalidIdString)
                }
                exception.message shouldBe "존재하지 않는 상품입니다: $invalidId"
            }
        }
    }

    given("ProductService의 getAllProducts 메서드가 주어졌을 때") {
        val productId1 = 236372517419679744L
        val productId2 = 236372517419679745L
        val productIdString1 = "0C6JNH3N3B8G0"
        val productIdString2 = "0C6JNH3N3B8G1"
        val category = Category(
            id = 1L,
            name = "전자제품",
            slug = "electronics",
            status = CategoryStatus.ACTIVE
        )

        val products = listOf(
            Product(
                id = productId1,
                name = "노트북",
                description = "고성능 노트북",
                category = category,
                originalPrice = BigDecimal("1000000"),
                salePrice = BigDecimal("800000"),
                status = ProductStatus.ACTIVE
            ),
            Product(
                id = productId2,
                name = "마우스",
                description = "무선 마우스",
                category = category,
                originalPrice = BigDecimal("50000"),
                salePrice = BigDecimal("40000"),
                status = ProductStatus.ACTIVE
            )
        )

        `when`("모든 상품을 조회하면") {
            every { idGenerator.encode(productId1) } returns productIdString1
            every { idGenerator.encode(productId2) } returns productIdString2
            every { productRepository.findAllNotDeleted() } returns products

            then("모든 상품 목록이 반환되어야 한다") {
                val response = productService.getAllProducts()

                response shouldHaveSize 2
                response[0].name shouldBe "노트북"
                response[1].name shouldBe "마우스"

                verify(exactly = 1) { productRepository.findAllNotDeleted() }
            }
        }
    }

    given("ProductService의 searchProducts 메서드가 주어졌을 때") {
        val productId = 236372517419679744L
        val productIdString = "0C6JNH3N3B8G0"
        val category = Category(
            id = 1L,
            name = "전자제품",
            slug = "electronics",
            status = CategoryStatus.ACTIVE
        )

        val searchRequest = ProductSearchRequest(
            categoryId = 1L,
            keyword = "노트북",
            minPrice = BigDecimal("500000"),
            maxPrice = BigDecimal("1500000"),
            status = ProductStatus.ACTIVE
        )

        val searchResults = listOf(
            Product(
                id = productId,
                name = "노트북",
                description = "고성능 노트북",
                category = category,
                originalPrice = BigDecimal("1000000"),
                salePrice = BigDecimal("800000"),
                status = ProductStatus.ACTIVE
            )
        )

        `when`("검색 조건으로 상품을 검색하면") {
            every { idGenerator.encode(productId) } returns productIdString
            every {
                productRepository.searchProducts(
                    categoryId = 1L,
                    keyword = "노트북",
                    minPrice = BigDecimal("500000"),
                    maxPrice = BigDecimal("1500000"),
                    status = ProductStatus.ACTIVE
                )
            } returns searchResults

            then("검색 결과가 반환되어야 한다") {
                val response = productService.searchProducts(searchRequest)

                response shouldHaveSize 1
                response[0].name shouldBe "노트북"

                verify(exactly = 1) {
                    productRepository.searchProducts(
                        categoryId = 1L,
                        keyword = "노트북",
                        minPrice = BigDecimal("500000"),
                        maxPrice = BigDecimal("1500000"),
                        status = ProductStatus.ACTIVE
                    )
                }
            }
        }
    }

    given("ProductService의 updateProduct 메서드가 주어졌을 때") {
        val productId = 236372517419679744L
        val productIdString = "0C6JNH3N3B8G0"
        val category = Category(
            id = 1L,
            name = "전자제품",
            slug = "electronics",
            status = CategoryStatus.ACTIVE
        )

        val product = Product(
            id = productId,
            name = "노트북",
            description = "고성능 노트북",
            category = category,
            originalPrice = BigDecimal("1000000"),
            salePrice = BigDecimal("800000"),
            status = ProductStatus.ACTIVE
        )

        `when`("상품 정보를 업데이트하면") {
            val updateRequest = UpdateProductRequest(
                name = "프리미엄 노트북",
                description = "최고급 노트북",
                originalPrice = BigDecimal("1500000"),
                salePrice = BigDecimal("1200000"),
                status = ProductStatus.ACTIVE
            )

            every { idGenerator.decode(productIdString) } returns productId
            every { idGenerator.encode(productId) } returns productIdString
            every { productRepository.findByIdAndNotDeleted(productId) } returns product

            then("상품 정보가 업데이트되어야 한다") {
                val response = productService.updateProduct(productIdString, updateRequest)

                response.name shouldBe "프리미엄 노트북"
                response.description shouldBe "최고급 노트북"
                response.originalPrice shouldBe BigDecimal("1500000")
                response.salePrice shouldBe BigDecimal("1200000")

                verify(exactly = 1) { idGenerator.decode(productIdString) }
                verify(exactly = 1) { idGenerator.encode(productId) }
                verify(exactly = 1) { productRepository.findByIdAndNotDeleted(productId) }
            }
        }

        `when`("카테고리를 변경하면") {
            val newCategory = Category(
                id = 2L,
                name = "컴퓨터",
                slug = "computers",
                status = CategoryStatus.ACTIVE
            )

            val updateRequest = UpdateProductRequest(categoryId = 2L)

            every { idGenerator.decode(productIdString) } returns productId
            every { idGenerator.encode(productId) } returns productIdString
            every { productRepository.findByIdAndNotDeleted(productId) } returns product
            every { categoryRepository.findByIdAndNotDeleted(2L) } returns newCategory

            then("카테고리가 변경되어야 한다") {
                val response = productService.updateProduct(productIdString, updateRequest)

                response.categoryId shouldBe 2L
                response.categoryName shouldBe "컴퓨터"

                verify(exactly = 1) { idGenerator.decode(productIdString) }
                verify(exactly = 1) { idGenerator.encode(productId) }
                verify(exactly = 1) { categoryRepository.findByIdAndNotDeleted(2L) }
            }
        }

        `when`("존재하지 않는 상품을 업데이트하면") {
            val invalidIdString = "INVALID"
            val invalidId = 999L
            every { idGenerator.decode(invalidIdString) } returns invalidId
            every { productRepository.findByIdAndNotDeleted(invalidId) } returns null

            then("예외가 발생해야 한다") {
                val updateRequest = UpdateProductRequest(name = "새 이름")

                val exception = shouldThrow<IllegalArgumentException> {
                    productService.updateProduct(invalidIdString, updateRequest)
                }
                exception.message shouldBe "존재하지 않는 상품입니다: $invalidId"
            }
        }

        `when`("존재하지 않는 카테고리로 변경하면") {
            every { idGenerator.decode(productIdString) } returns productId
            every { productRepository.findByIdAndNotDeleted(productId) } returns product
            every { categoryRepository.findByIdAndNotDeleted(999L) } returns null

            then("예외가 발생해야 한다") {
                val updateRequest = UpdateProductRequest(categoryId = 999L)

                val exception = shouldThrow<IllegalArgumentException> {
                    productService.updateProduct(productIdString, updateRequest)
                }
                exception.message shouldBe "존재하지 않는 카테고리입니다: 999"
            }
        }
    }

    given("ProductService의 deleteProduct 메서드가 주어졌을 때") {
        val productId = 236372517419679744L
        val productIdString = "0C6JNH3N3B8G0"
        val category = Category(
            id = 1L,
            name = "전자제품",
            slug = "electronics",
            status = CategoryStatus.ACTIVE
        )

        val product = Product(
            id = productId,
            name = "노트북",
            description = "고성능 노트북",
            category = category,
            originalPrice = BigDecimal("1000000"),
            salePrice = BigDecimal("800000"),
            status = ProductStatus.ACTIVE
        )

        `when`("존재하는 상품을 삭제하면") {
            every { idGenerator.decode(productIdString) } returns productId
            every { productRepository.findByIdAndNotDeleted(productId) } returns product

            then("상품이 소프트 삭제되어야 한다") {
                productService.deleteProduct(productIdString)

                verify(exactly = 1) { idGenerator.decode(productIdString) }
                verify(exactly = 1) { productRepository.findByIdAndNotDeleted(productId) }
                product.isDeleted() shouldBe true
            }
        }

        `when`("존재하지 않는 상품을 삭제하면") {
            val invalidIdString = "INVALID"
            val invalidId = 999L
            every { idGenerator.decode(invalidIdString) } returns invalidId
            every { productRepository.findByIdAndNotDeleted(invalidId) } returns null

            then("예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    productService.deleteProduct(invalidIdString)
                }
                exception.message shouldBe "존재하지 않는 상품입니다: $invalidId"
            }
        }
    }
})
