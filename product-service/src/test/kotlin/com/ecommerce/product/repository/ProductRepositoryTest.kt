package com.ecommerce.product.repository

import com.ecommerce.product.dto.CategoryProductStats
import com.ecommerce.product.entity.Category
import com.ecommerce.product.entity.Product
import com.ecommerce.product.enums.ProductStatus
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal

class ProductRepositoryTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val jpaRepository: ProductJpaRepository = mockk()
    val queryRepository: ProductQueryRepository = mockk()
    val productRepository = ProductRepository(jpaRepository, queryRepository)

    given("ProductRepository의 save 메서드") {
        `when`("상품을 저장하면") {
            val productId = 236372517419679744L
            val product = Product(
                id = productId,
                name = "Test Product",
                description = "Test Description",
                category = Category(name = "Test Category", slug = "test-category", status = com.ecommerce.product.enums.CategoryStatus.ACTIVE),
                originalPrice = BigDecimal("100.00"),
                salePrice = BigDecimal("90.00"),
                status = ProductStatus.ACTIVE
            )

            val savedProduct = Product(
                id = productId,
                name = "Test Product",
                description = "Test Description",
                category = Category(name = "Test Category", slug = "test-category", status = com.ecommerce.product.enums.CategoryStatus.ACTIVE),
                originalPrice = BigDecimal("100.00"),
                salePrice = BigDecimal("90.00"),
                status = ProductStatus.ACTIVE
            )

            every { jpaRepository.save(product) } returns savedProduct

            then("jpaRepository의 save 메서드가 호출되고 저장된 상품을 반환한다") {
                val result = productRepository.save(product)
                result shouldBe savedProduct
                verify(exactly = 1) { jpaRepository.save(product) }
            }
        }
    }

    given("ProductRepository의 findByIdAndNotDeleted 메서드") {
        `when`("ID로 상품을 조회하면") {
            val productId = 1L
            val product = Product(
                id = productId,
                name = "Test Product",
                description = "Test Description",
                category = Category(name = "Test Category", slug = "test-category", status = com.ecommerce.product.enums.CategoryStatus.ACTIVE),
                originalPrice = BigDecimal("100.00"),
                salePrice = BigDecimal("90.00"),
                status = ProductStatus.ACTIVE
            )

            every { jpaRepository.findByIdWithDetails(productId) } returns product

            then("jpaRepository의 findByIdWithDetails 메서드가 호출되고 상품을 반환한다") {
                val result = productRepository.findByIdAndNotDeleted(productId)
                result shouldBe product
                verify(exactly = 1) { jpaRepository.findByIdWithDetails(productId) }
            }
        }

        `when`("존재하지 않는 ID로 상품을 조회하면") {
            val productId = 999L

            every { jpaRepository.findByIdWithDetails(productId) } returns null

            then("jpaRepository의 findByIdWithDetails 메서드가 호출되고 null을 반환한다") {
                val result = productRepository.findByIdAndNotDeleted(productId)
                result shouldBe null
                verify(exactly = 1) { jpaRepository.findByIdWithDetails(productId) }
            }
        }
    }

    given("ProductRepository의 findAllNotDeleted 메서드") {
        `when`("모든 상품을 조회하면") {
            val product1 = Product(
                id = 1L,
                name = "Test Product1",
                description = "Desc1",
                category = Category(name = "Test Category", slug = "test-category", status = com.ecommerce.product.enums.CategoryStatus.ACTIVE),
                originalPrice = BigDecimal("100.00"),
                salePrice = BigDecimal("90.00"),
                status = ProductStatus.ACTIVE
            )
            val product2 = Product(
                id = 2L,
                name = "Test Product2",
                description = "Desc2",
                category = Category(name = "Test Category", slug = "test-category", status = com.ecommerce.product.enums.CategoryStatus.ACTIVE),
                originalPrice = BigDecimal("200.00"),
                salePrice = BigDecimal("180.00"),
                status = ProductStatus.ACTIVE
            )
            val productList = listOf(product1, product2)

            every { jpaRepository.findAllWithDetails() } returns productList

            then("jpaRepository의 findAllWithDetails 메서드가 호출되고 상품 리스트를 반환한다") {
                val result = productRepository.findAllNotDeleted()
                result shouldBe productList
                verify(exactly = 1) { jpaRepository.findAllWithDetails() }
            }
        }

        `when`("상품이 없으면") {
            every { jpaRepository.findAllWithDetails() } returns emptyList()

            then("jpaRepository의 findAllWithDetails 메서드가 호출되고 빈 리스트를 반환한다") {
                val result = productRepository.findAllNotDeleted()
                result shouldBe emptyList()
                verify(exactly = 1) { jpaRepository.findAllWithDetails() }
            }
        }
    }

    given("ProductRepository의 findByCategoryId 메서드") {
        `when`("카테고리 ID로 상품을 조회하면") {
            val categoryId = 1L
            val product1 = Product(
                id = 1L,
                name = "Test Product1",
                description = "Desc1",
                category = Category(id = categoryId, name = "Test Category", slug = "test-category", status = com.ecommerce.product.enums.CategoryStatus.ACTIVE),
                originalPrice = BigDecimal("100.00"),
                salePrice = BigDecimal("90.00"),
                status = ProductStatus.ACTIVE
            )
            val productList = listOf(product1)

            every { jpaRepository.findByCategoryIdWithDetails(categoryId) } returns productList

            then("jpaRepository의 findByCategoryIdWithDetails 메서드가 호출되고 해당 카테고리 상품 리스트를 반환한다") {
                val result = productRepository.findByCategoryId(categoryId)
                result shouldBe productList
                verify(exactly = 1) { jpaRepository.findByCategoryIdWithDetails(categoryId) }
            }
        }

        `when`("해당 카테고리에 상품이 없으면") {
            val categoryId = 999L

            every { jpaRepository.findByCategoryIdWithDetails(categoryId) } returns emptyList()

            then("jpaRepository의 findByCategoryIdWithDetails 메서드가 호출되고 빈 리스트를 반환한다") {
                val result = productRepository.findByCategoryId(categoryId)
                result shouldBe emptyList()
                verify(exactly = 1) { jpaRepository.findByCategoryIdWithDetails(categoryId) }
            }
        }
    }

    given("ProductRepository의 existsByIdAndNotDeleted 메서드") {
        `when`("ID로 상품 존재 여부를 확인하면") {
            val productId = 1L

            every { jpaRepository.existsByIdAndNotDeleted(productId) } returns true

            then("jpaRepository의 existsByIdAndNotDeleted 메서드가 호출되고 true를 반환한다") {
                val result = productRepository.existsByIdAndNotDeleted(productId)
                result shouldBe true
                verify(exactly = 1) { jpaRepository.existsByIdAndNotDeleted(productId) }
            }
        }

        `when`("존재하지 않는 ID로 상품 존재 여부를 확인하면") {
            val productId = 999L

            every { jpaRepository.existsByIdAndNotDeleted(productId) } returns false

            then("jpaRepository의 existsByIdAndNotDeleted 메서드가 호출되고 false를 반환한다") {
                val result = productRepository.existsByIdAndNotDeleted(productId)
                result shouldBe false
                verify(exactly = 1) { jpaRepository.existsByIdAndNotDeleted(productId) }
            }
        }
    }

    given("ProductRepository의 count 메서드") {
        `when`("상품 개수를 조회하면") {
            val count = 5L

            every { jpaRepository.count() } returns count

            then("jpaRepository의 count 메서드가 호출되고 상품 개수를 반환한다") {
                val result = productRepository.count()
                result shouldBe count
                verify(exactly = 1) { jpaRepository.count() }
            }
        }
    }

    given("ProductRepository의 delete 메서드") {
        `when`("상품을 삭제하면") {
            val product = Product(
                id = 1L,
                name = "Test Product",
                description = "Test Description",
                category = Category(name = "Test Category", slug = "test-category", status = com.ecommerce.product.enums.CategoryStatus.ACTIVE),
                originalPrice = BigDecimal("100.00"),
                salePrice = BigDecimal("90.00"),
                status = ProductStatus.ACTIVE
            )

            every { jpaRepository.delete(product) } returns Unit

            then("jpaRepository의 delete 메서드가 호출된다") {
                productRepository.delete(product)
                verify(exactly = 1) { jpaRepository.delete(product) }
            }
        }
    }

    given("ProductRepository의 searchProducts 메서드") {
        `when`("검색 조건으로 상품을 조회하면") {
            val categoryId = 1L
            val keyword = "Test"
            val minPrice = BigDecimal("50.00")
            val maxPrice = BigDecimal("150.00")
            val status = ProductStatus.ACTIVE
            val productList = listOf(Product(
                id = 1L,
                name = "Test Product",
                description = "Test Description",
                category = Category(id = categoryId, name = "Test Category", slug = "test-category", status = com.ecommerce.product.enums.CategoryStatus.ACTIVE),
                originalPrice = BigDecimal("100.00"),
                salePrice = BigDecimal("90.00"),
                status = ProductStatus.ACTIVE
            ))

            every {
                queryRepository.searchProducts(
                    categoryId = categoryId,
                    keyword = keyword,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    status = status
                )
            } returns productList

            then("queryRepository의 searchProducts 메서드가 호출되고 검색 결과 리스트를 반환한다") {
                val result = productRepository.searchProducts(
                    categoryId = categoryId,
                    keyword = keyword,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    status = status
                )
                result shouldBe productList
                verify(exactly = 1) {
                    queryRepository.searchProducts(
                        categoryId = categoryId,
                        keyword = keyword,
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        status = status
                    )
                }
            }
        }
    }

    given("ProductRepository의 getProductStatsByCategory 메서드") {
        `when`("카테고리별 상품 통계를 조회하면") {
            val statsList = listOf(CategoryProductStats(1L, "Test Category", 5L, BigDecimal("123.45")))

            every { queryRepository.getProductStatsByCategory() } returns statsList

            then("queryRepository의 getProductStatsByCategory 메서드가 호출되고 통계 리스트를 반환한다") {
                val result = productRepository.getProductStatsByCategory()
                result shouldBe statsList
                verify(exactly = 1) { queryRepository.getProductStatsByCategory() }
            }
        }
    }
})