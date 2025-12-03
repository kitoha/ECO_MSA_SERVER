package com.ecommerce.product.entity

import com.ecommerce.product.enums.CategoryStatus
import com.ecommerce.product.enums.ProductStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class ProductTest : BehaviorSpec({

    lateinit var category: Category
    lateinit var product: Product

    beforeEach {
        category = Category(
            id = 1L,
            name = "전자제품",
            slug = "electronics",
            status = CategoryStatus.ACTIVE
        )

        product = Product(
            id = 1L,
            name = "노트북",
            description = "고성능 노트북",
            category = category,
            originalPrice = BigDecimal("1000000"),
            salePrice = BigDecimal("800000"),
            status = ProductStatus.ACTIVE
        )
    }

    given("Product 엔티티가 주어졌을 때") {

        `when`("가격을 업데이트할 때") {
            then("정상적인 가격으로 업데이트되어야 한다") {
                product.updatePrice(BigDecimal("1200000"), BigDecimal("1000000"))

                product.originalPrice shouldBe BigDecimal("1200000")
                product.salePrice shouldBe BigDecimal("1000000")
            }

            then("원가가 0 미만이면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    product.updatePrice(BigDecimal("-100"), BigDecimal("0"))
                }
                exception.message shouldBe "원가는 0 이상이어야 합니다"
            }

            then("판매가가 0 미만이면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    product.updatePrice(BigDecimal("1000"), BigDecimal("-100"))
                }
                exception.message shouldBe "판매가는 0 이상이어야 합니다"
            }

            then("판매가가 원가보다 크면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    product.updatePrice(BigDecimal("1000"), BigDecimal("1500"))
                }
                exception.message shouldBe "판매가는 원가보다 클 수 없습니다"
            }

            then("원가와 판매가가 같으면 업데이트되어야 한다") {
                product.updatePrice(BigDecimal("1000"), BigDecimal("1000"))

                product.originalPrice shouldBe BigDecimal("1000")
                product.salePrice shouldBe BigDecimal("1000")
            }
        }

        `when`("할인율을 계산할 때") {
            then("정상적으로 할인율이 계산되어야 한다") {
                // originalPrice: 1000000, salePrice: 800000
                val discountRate = product.getDiscountRate()

                discountRate shouldBe BigDecimal("20.00")
            }

            then("원가가 0일 때 할인율은 0이어야 한다") {
                val zeroProduct = Product(
                    id = 236372517419679745L,
                    name = "무료 상품",
                    description = "무료",
                    category = category,
                    originalPrice = BigDecimal.ZERO,
                    salePrice = BigDecimal.ZERO,
                    status = ProductStatus.ACTIVE
                )

                zeroProduct.getDiscountRate() shouldBe BigDecimal.ZERO
            }

            then("판매가와 원가가 같으면 할인율은 0이어야 한다") {
                val noDiscountProduct = Product(
                    id = 236372517419679746L,
                    name = "할인 없음",
                    description = "할인 없음",
                    category = category,
                    originalPrice = BigDecimal("1000"),
                    salePrice = BigDecimal("1000"),
                    status = ProductStatus.ACTIVE
                )

                noDiscountProduct.getDiscountRate() shouldBe BigDecimal("0.00")
            }

            then("소수점 둘째 자리까지 반올림되어야 한다") {
                val discountProduct = Product(
                    id = 236372517419679747L,
                    name = "할인 상품",
                    description = "할인 상품",
                    category = category,
                    originalPrice = BigDecimal("999"),
                    salePrice = BigDecimal("666"),
                    status = ProductStatus.ACTIVE
                )

                discountProduct.getDiscountRate() shouldBe BigDecimal("33.33")
            }
        }

        `when`("세일 여부를 확인할 때") {
            then("판매가가 원가보다 낮으면 true를 반환해야 한다") {
                product.isOnSale().shouldBeTrue()
            }

            then("판매가와 원가가 같으면 false를 반환해야 한다") {
                val noSaleProduct = Product(
                    id = 236372517419679748L,
                    name = "세일 없음",
                    description = "세일 없음",
                    category = category,
                    originalPrice = BigDecimal("1000"),
                    salePrice = BigDecimal("1000"),
                    status = ProductStatus.ACTIVE
                )

                noSaleProduct.isOnSale().shouldBeFalse()
            }
        }

        `when`("카테고리를 변경할 때") {
            then("새로운 카테고리로 변경되어야 한다") {
                val newCategory = Category(
                    id = 2L,
                    name = "컴퓨터",
                    slug = "computers",
                    status = CategoryStatus.ACTIVE
                )

                product.changeCategory(newCategory)

                product.category shouldBe newCategory
                product.categoryId shouldBe 2L
            }
        }

        `when`("상품명을 변경할 때") {
            then("새로운 이름으로 변경되어야 한다") {
                product.updateName("고급 노트북")

                product.name shouldBe "고급 노트북"
            }
        }

        `when`("상품 설명을 변경할 때") {
            then("새로운 설명으로 변경되어야 한다") {
                product.updateDescription("최신 고성능 노트북")

                product.description shouldBe "최신 고성능 노트북"
            }
        }

        `when`("상품 상태를 변경할 때") {
            then("새로운 상태로 변경되어야 한다") {
                product.changeStatus(ProductStatus.SOLD_OUT)

                product.status shouldBe ProductStatus.SOLD_OUT
            }
        }

        `when`("상품을 삭제할 때") {
            then("소프트 삭제되어야 한다") {
                product.delete()

                product.isDeleted().shouldBeTrue()
                product.deletedAt.shouldNotBeNull()
            }
        }

        `when`("상품 이미지를 추가할 때") {
            then("이미지가 추가되어야 한다") {
                val image = ProductImage(
                    product = product,
                    imageUrl = "http://example.com/image1.jpg",
                    displayOrder = 1,
                    isThumbnail = true
                )

                product.addImage(image)

                product.images.size shouldBe 1
                product.images[0] shouldBe image
            }

            then("여러 이미지를 추가할 수 있어야 한다") {
                val image1 = ProductImage(
                    product = product,
                    imageUrl = "http://example.com/image1.jpg",
                    displayOrder = 1,
                    isThumbnail = true
                )
                val image2 = ProductImage(
                    product = product,
                    imageUrl = "http://example.com/image2.jpg",
                    displayOrder = 2,
                    isThumbnail = false
                )

                product.addImage(image1)
                product.addImage(image2)

                product.images.size shouldBe 2
                product.images[0] shouldBe image1
                product.images[1] shouldBe image2
            }
        }
    }
})
