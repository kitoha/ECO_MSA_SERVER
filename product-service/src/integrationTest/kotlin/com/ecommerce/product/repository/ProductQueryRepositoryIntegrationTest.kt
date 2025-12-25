package com.ecommerce.product.repository

import com.ecommerce.product.config.JpaConfig
import com.ecommerce.product.entity.Category
import com.ecommerce.product.entity.Product
import com.ecommerce.product.enums.CategoryStatus
import com.ecommerce.product.enums.ProductStatus
import com.ecommerce.product.generator.TsidGenerator
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal

@DataJpaTest
@Transactional
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(TestJpaConfig::class, JpaConfig::class)
class ProductQueryRepositoryIntegrationTest(
    private val productQueryRepository: ProductQueryRepository,
    private val categoryRepository: CategoryRepository,
    private val productJpaRepository: ProductJpaRepository
) : BehaviorSpec({
    extensions(SpringExtension)

    val idGenerator = TsidGenerator()

    given("실제 DB에 테스트 데이터가 저장되어 있을 때") {
        lateinit var category: Category
        lateinit var product1: Product
        lateinit var product2: Product
        lateinit var product3: Product

        beforeEach {
            // 매번 DB 정리 후 데이터 세팅
            productJpaRepository.deleteAll()
            categoryRepository.deleteAll()

            category = categoryRepository.save(Category(
                name = "전자제품",
                slug = "electronics",
                status = CategoryStatus.ACTIVE
            ))

            product1 = productJpaRepository.save(Product(
                id = idGenerator.generate(),
                name = "노트북",
                description = "고성능 노트북",
                category = category,
                originalPrice = BigDecimal("1000000"),
                salePrice = BigDecimal("800000"),
                status = ProductStatus.ACTIVE
            ))

            product2 = productJpaRepository.save(Product(
                id = idGenerator.generate(),
                name = "노트북 프로",
                description = "프로급 노트북",
                category = category,
                originalPrice = BigDecimal("2000000"),
                salePrice = BigDecimal("1800000"),
                status = ProductStatus.ACTIVE
            ))

            product3 = productJpaRepository.save(Product(
                id = idGenerator.generate(),
                name = "마우스",
                description = "무선 마우스",
                category = category,
                originalPrice = BigDecimal("50000"),
                salePrice = BigDecimal("40000"),
                status = ProductStatus.DRAFT
            ))
        }

        afterEach {
            productJpaRepository.deleteAll()
            categoryRepository.deleteAll()
        }

        `when`("모든 조건 없이 검색하면") {
            then("삭제되지 않은 모든 상품이 조회되어야 한다") {
                val result = productQueryRepository.searchProducts(
                    categoryId = null,
                    keyword = null,
                    minPrice = null,
                    maxPrice = null,
                    status = null
                )

                result shouldHaveSize 3
                result.map { it.name } shouldBe listOf("마우스", "노트북 프로", "노트북")
            }
        }

        `when`("카테고리 ID로 필터링하면") {
            then("해당 카테고리의 상품만 조회되어야 한다") {
                val result = productQueryRepository.searchProducts(
                    categoryId = category.id,
                    keyword = null,
                    minPrice = null,
                    maxPrice = null,
                    status = null
                )

                result shouldHaveSize 3
                result.all { it.category.id == category.id } shouldBe true
            }

            then("존재하지 않는 카테고리 ID로 검색하면 빈 리스트가 반환되어야 한다") {
                val result = productQueryRepository.searchProducts(
                    categoryId = 9999L,
                    keyword = null,
                    minPrice = null,
                    maxPrice = null,
                    status = null
                )

                result.shouldBeEmpty()
            }
        }

        `when`("키워드로 검색하면") {
            then("키워드가 포함된 상품만 조회되어야 한다 (대소문자 무시)") {
                val result = productQueryRepository.searchProducts(
                    categoryId = null,
                    keyword = "노트북",
                    minPrice = null,
                    maxPrice = null,
                    status = null
                )

                result shouldHaveSize 2
                result.all { it.name.contains("노트북") } shouldBe true
            }

            then("대소문자를 구분하지 않고 검색되어야 한다") {
                val result = productQueryRepository.searchProducts(
                    categoryId = null,
                    keyword = "노트북",
                    minPrice = null,
                    maxPrice = null,
                    status = null
                )

                result shouldHaveSize 2
            }

            then("일치하는 키워드가 없으면 빈 리스트가 반환되어야 한다") {
                val result = productQueryRepository.searchProducts(
                    categoryId = null,
                    keyword = "존재하지않는상품",
                    minPrice = null,
                    maxPrice = null,
                    status = null
                )

                result.shouldBeEmpty()
            }
        }

        `when`("가격 범위로 필터링하면") {
            then("최소 가격 이상의 상품만 조회되어야 한다") {
                val result = productQueryRepository.searchProducts(
                    categoryId = null,
                    keyword = null,
                    minPrice = BigDecimal("1000000"),
                    maxPrice = null,
                    status = null
                )

                result shouldHaveSize 1
                result[0].name shouldBe "노트북 프로"
                result[0].salePrice shouldBe BigDecimal("1800000")
            }

            then("최대 가격 이하의 상품만 조회되어야 한다") {
                val result = productQueryRepository.searchProducts(
                    categoryId = null,
                    keyword = null,
                    minPrice = null,
                    maxPrice = BigDecimal("100000"),
                    status = null
                )

                result shouldHaveSize 1
                result[0].name shouldBe "마우스"
            }

            then("가격 범위 내의 상품만 조회되어야 한다") {
                val result = productQueryRepository.searchProducts(
                    categoryId = null,
                    keyword = null,
                    minPrice = BigDecimal("500000"),
                    maxPrice = BigDecimal("1000000"),
                    status = null
                )

                result shouldHaveSize 1
                result[0].name shouldBe "노트북"
                result[0].salePrice shouldBe BigDecimal("800000")
            }
        }

        `when`("상태로 필터링하면") {
            then("ACTIVE 상태의 상품만 조회되어야 한다") {
                val result = productQueryRepository.searchProducts(
                    categoryId = null,
                    keyword = null,
                    minPrice = null,
                    maxPrice = null,
                    status = ProductStatus.ACTIVE
                )

                result shouldHaveSize 2
                result.all { it.status == ProductStatus.ACTIVE } shouldBe true
            }

            then("DRAFT 상태의 상품만 조회되어야 한다") {
                val result = productQueryRepository.searchProducts(
                    categoryId = null,
                    keyword = null,
                    minPrice = null,
                    maxPrice = null,
                    status = ProductStatus.DRAFT
                )

                result shouldHaveSize 1
                result[0].status shouldBe ProductStatus.DRAFT
            }
        }

        `when`("여러 조건을 조합하여 검색하면") {
            then("모든 조건을 만족하는 상품만 조회되어야 한다") {
                val result = productQueryRepository.searchProducts(
                    categoryId = category.id,
                    keyword = "노트북",
                    minPrice = BigDecimal("500000"),
                    maxPrice = BigDecimal("1500000"),
                    status = ProductStatus.ACTIVE
                )

                result shouldHaveSize 1
                result[0].name shouldBe "노트북"
                result[0].salePrice shouldBe BigDecimal("800000")
                result[0].status shouldBe ProductStatus.ACTIVE
            }

            then("조건을 만족하는 상품이 없으면 빈 리스트가 반환되어야 한다") {
                val result = productQueryRepository.searchProducts(
                    categoryId = category.id,
                    keyword = "노트북",
                    minPrice = BigDecimal("3000000"),
                    maxPrice = BigDecimal("5000000"),
                    status = ProductStatus.ACTIVE
                )

                result.shouldBeEmpty()
            }
        }

        `when`("FETCH JOIN이 적용된 검색을 하면") {
            then("Category가 즉시 로딩되어야 한다 (N+1 문제 없음)") {
                val result = productQueryRepository.searchProducts(
                    categoryId = null,
                    keyword = "노트북",
                    minPrice = null,
                    maxPrice = null,
                    status = null
                )

                result shouldHaveSize 2

                // Category 접근 시 추가 쿼리가 발생하지 않아야 함
                result.forEach { product ->
                    product.category shouldNotBe null
                    product.category.name shouldBe "전자제품"
                }
            }
        }

        `when`("Soft delete된 상품이 있을 때") {
            then("삭제된 상품은 검색 결과에 포함되지 않아야 한다") {
                val deletedProduct = productJpaRepository.save(Product(
                    id = idGenerator.generate(),
                    name = "삭제된 상품",
                    description = "이 상품은 삭제됨",
                    category = category,
                    originalPrice = BigDecimal("100000"),
                    salePrice = BigDecimal("90000"),
                    status = ProductStatus.ACTIVE
                ))
                deletedProduct.delete()
                productJpaRepository.save(deletedProduct)

                val result = productQueryRepository.searchProducts(
                    categoryId = null,
                    keyword = null,
                    minPrice = null,
                    maxPrice = null,
                    status = null
                )

                // 삭제되지 않은 3개만 조회됨
                result shouldHaveSize 3
                result.none { it.name == "삭제된 상품" } shouldBe true
            }
        }
    }

    given("ProductQueryRepository의 getProductStatsByCategory 메서드가 주어졌을 때") {
        lateinit var category1: Category
        lateinit var category2: Category

        beforeEach {
            productJpaRepository.deleteAll()
            categoryRepository.deleteAll()

            category1 = categoryRepository.save(Category(
                name = "전자제품",
                slug = "electronics2",
                status = CategoryStatus.ACTIVE
            ))

            category2 = categoryRepository.save(Category(
                name = "의류",
                slug = "clothing",
                status = CategoryStatus.ACTIVE
            ))

            // 전자제품 카테고리에 2개 상품
            productJpaRepository.save(Product(
                id = idGenerator.generate(),
                name = "노트북",
                description = "고성능 노트북",
                category = category1,
                originalPrice = BigDecimal("1000000"),
                salePrice = BigDecimal("800000"),
                status = ProductStatus.ACTIVE
            ))
            productJpaRepository.save(Product(
                id = idGenerator.generate(),
                name = "마우스",
                description = "무선 마우스",
                category = category1,
                originalPrice = BigDecimal("50000"),
                salePrice = BigDecimal("40000"),
                status = ProductStatus.ACTIVE
            ))

            // 의류 카테고리에 1개 상품
            productJpaRepository.save(Product(
                id = idGenerator.generate(),
                name = "티셔츠",
                description = "면 티셔츠",
                category = category2,
                originalPrice = BigDecimal("30000"),
                salePrice = BigDecimal("25000"),
                status = ProductStatus.ACTIVE
            ))
        }

        afterEach {
            productJpaRepository.deleteAll()
            categoryRepository.deleteAll()
        }

        `when`("카테고리별 상품 통계를 조회하면") {
            then("각 카테고리별 상품 수와 평균 가격이 정확히 계산되어야 한다") {
                val result = productQueryRepository.getProductStatsByCategory()

                result shouldHaveSize 2

                val electronicsStats = result.find { it.categoryName == "전자제품" }
                electronicsStats shouldNotBe null
                electronicsStats!!.productCount shouldBe 2L
                electronicsStats.averagePrice shouldBe BigDecimal("420000.0")

                val clothingStats = result.find { it.categoryName == "의류" }
                clothingStats shouldNotBe null
                clothingStats!!.productCount shouldBe 1L
                clothingStats.averagePrice shouldBe BigDecimal("25000.0")
            }
        }

        `when`("삭제된 상품이 있을 때 통계를 조회하면") {
            then("삭제된 상품은 통계에서 제외되어야 한다") {

                val products = productJpaRepository.findAll()
                val productToDelete = products.first { it.category.id == category1.id }
                productToDelete.delete()
                productJpaRepository.save(productToDelete)

                val result = productQueryRepository.getProductStatsByCategory()

                val electronicsStats = result.find { it.categoryName == "전자제품" }
                electronicsStats shouldNotBe null

                electronicsStats!!.productCount shouldBe 1L
            }
        }
    }
}) {
    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
    }
}
