package com.ecommerce.cart.service

import com.ecommerce.cart.client.ProductClient
import com.ecommerce.cart.dto.external.ProductResponse
import com.ecommerce.cart.entity.Cart
import com.ecommerce.cart.exception.CartException
import com.ecommerce.cart.generator.TsidGenerator
import com.ecommerce.cart.repository.CartItemRepository
import com.ecommerce.cart.repository.CartRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class CartServiceTest : BehaviorSpec({

    val cartRepository = mockk<CartRepository>()
    val productClient = mockk<ProductClient>()
    val idGenerator = mockk<TsidGenerator>()
    val cartService = CartService(cartRepository, productClient, idGenerator)

    beforeEach {
        clearMocks(cartRepository, productClient, idGenerator, answers = false)
    }

    given("CartService의 getOrCreateCart 메서드가 주어졌을 때") {
        val userId = 100L

        `when`("장바구니가 이미 존재하면") {
            val existingCart = Cart(
                id = 1L,
                userId = userId
            )

            every { cartRepository.findByUserIdWithItems(userId) } returns Optional.of(existingCart)

            then("기존 장바구니를 반환해야 한다") {
                val result = cartService.getOrCreateCart(userId)

                result.id shouldBe 1L
                result.userId shouldBe userId

                verify(exactly = 1) { cartRepository.findByUserIdWithItems(userId) }
                verify(exactly = 0) { cartRepository.save(any()) }
            }
        }

        `when`("장바구니가 존재하지 않으면") {
            val newCartId = 2L
            val newCart = Cart(
                id = newCartId,
                userId = userId
            )

            every { cartRepository.findByUserIdWithItems(userId) } returns Optional.empty()
            every { idGenerator.generate() } returns newCartId
            every { cartRepository.save(any()) } returns newCart

            then("새 장바구니를 생성해야 한다") {
                val result = cartService.getOrCreateCart(userId)

                result.id shouldBe newCartId
                result.userId shouldBe userId

                verify(exactly = 1) { cartRepository.findByUserIdWithItems(userId) }
                verify(exactly = 1) { idGenerator.generate() }
                verify(exactly = 1) { cartRepository.save(any()) }
            }
        }
    }

    given("CartService의 addItemToCart 메서드가 주어졌을 때") {
        val userId = 100L
        val productIdLong = 10L
        val productIdString = "0P8SKS9BSP5YR"
        val quantity = 2
        val cart = Cart(
            id = 1L,
            userId = userId
        )

        val product = ProductResponse(
            id = productIdString,
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

        `when`("유효한 상품을 장바구니에 추가하면") {
            every { idGenerator.decode(productIdString) } returns productIdLong
            every { productClient.getProductById(productIdString) } returns product
            every { cartRepository.findByUserIdWithItems(userId) } returns Optional.of(cart)
            every { cartRepository.save(any()) } returns cart

            then("상품이 장바구니에 추가되어야 한다") {
                val result = cartService.addItemToCart(userId, productIdString, quantity)

                result.getActiveItems() shouldHaveSize 1
                result.getActiveItems()[0].productId shouldBe productIdLong
                result.getActiveItems()[0].quantity shouldBe quantity

                verify(exactly = 1) { idGenerator.decode(productIdString) }
                verify(exactly = 1) { productClient.getProductById(productIdString) }
                verify(exactly = 1) { cartRepository.save(any()) }
            }
        }

        `when`("존재하지 않는 상품을 추가하려고 하면") {
            every { idGenerator.decode(productIdString) } returns productIdLong
            every { cartRepository.findByUserIdWithItems(userId) } returns Optional.of(cart)
            every { productClient.getProductById(productIdString) } returns null

            then("ProductNotAvailableException이 발생해야 한다") {
                val exception = shouldThrow<CartException.ProductNotAvailableException> {
                    cartService.addItemToCart(userId, productIdString, quantity)
                }
                exception.message shouldBe "상품을 사용할 수 없습니다. productId=$productIdLong"
            }
        }

        `when`("비활성 상품을 추가하려고 하면") {
            val inactiveProduct = product.copy(status = "INACTIVE")

            every { idGenerator.decode(productIdString) } returns productIdLong
            every { cartRepository.findByUserIdWithItems(userId) } returns Optional.of(cart)
            every { productClient.getProductById(productIdString) } returns inactiveProduct

            then("ProductNotAvailableException이 발생해야 한다") {
                val exception = shouldThrow<CartException.ProductNotAvailableException> {
                    cartService.addItemToCart(userId, productIdString, quantity)
                }
                exception.message shouldBe "상품을 사용할 수 없습니다. productId=$productIdLong"
            }
        }

        `when`("수량이 0 이하면") {
            then("IllegalArgumentException이 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    cartService.addItemToCart(userId, productIdString, 0)
                }
                exception.message shouldBe "수량은 1 이상이어야 합니다"
            }
        }
    }

    given("CartService의 updateItemQuantity 메서드가 주어졌을 때") {
        val userId = 100L
        val cart = Cart(
            id = 1L,
            userId = userId
        )

        `when`("유효한 수량으로 업데이트하면") {
            cart.addItem(
                productId = 10L,
                productName = "테스트 상품",
                price = BigDecimal("10000"),
                quantity = 2
            )
            val itemId = cart.getActiveItems()[0].id

            every { cartRepository.findByUserIdWithItems(userId) } returns Optional.of(cart)
            every { cartRepository.save(any()) } returns cart

            then("수량이 업데이트되어야 한다") {
                val result = cartService.updateItemQuantity(userId, itemId, 5)

                result.getActiveItems()[0].quantity shouldBe 5

                verify(exactly = 1) { cartRepository.findByUserIdWithItems(userId) }
                verify(exactly = 1) { cartRepository.save(any()) }
            }
        }

        `when`("장바구니가 존재하지 않으면") {
            every { cartRepository.findByUserIdWithItems(userId) } returns Optional.empty()

            then("CartNotFoundException이 발생해야 한다") {
                val exception = shouldThrow<CartException.CartNotFoundException> {
                    cartService.updateItemQuantity(userId, 1L, 5)
                }
                exception.message shouldBe "장바구니를 찾을 수 없습니다. userId=$userId"
            }
        }

        `when`("수량이 0 이하면") {
            every { cartRepository.findByUserIdWithItems(userId) } returns Optional.of(cart)

            then("IllegalArgumentException이 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    cartService.updateItemQuantity(userId, 1L, 0)
                }
                exception.message shouldBe "수량은 1 이상이어야 합니다"
            }
        }
    }

    given("CartService의 removeItemFromCart 메서드가 주어졌을 때") {
        val userId = 100L
        val cart = Cart(
            id = 1L,
            userId = userId
        )

        `when`("유효한 아이템을 삭제하면") {
            cart.addItem(
                productId = 10L,
                productName = "테스트 상품",
                price = BigDecimal("10000"),
                quantity = 2
            )
            val itemId = cart.getActiveItems()[0].id

            every { cartRepository.findByUserIdWithItems(userId) } returns Optional.of(cart)
            every { cartRepository.save(any()) } returns cart

            then("아이템이 삭제되어야 한다") {
                val result = cartService.removeItemFromCart(userId, itemId)

                result.getActiveItems() shouldHaveSize 0

                verify(exactly = 1) { cartRepository.findByUserIdWithItems(userId) }
                verify(exactly = 1) { cartRepository.save(any()) }
            }
        }

        `when`("장바구니가 존재하지 않으면") {
            every { cartRepository.findByUserIdWithItems(userId) } returns Optional.empty()

            then("CartNotFoundException이 발생해야 한다") {
                val exception = shouldThrow<CartException.CartNotFoundException> {
                    cartService.removeItemFromCart(userId, 1L)
                }
                exception.message shouldBe "장바구니를 찾을 수 없습니다. userId=$userId"
            }
        }
    }

    given("CartService의 clearCart 메서드가 주어졌을 때") {
        val userId = 100L
        val cart = Cart(
            id = 1L,
            userId = userId
        )

        `when`("장바구니를 비우면") {
            cart.addItem(
                productId = 10L,
                productName = "상품1",
                price = BigDecimal("10000"),
                quantity = 1
            )
            cart.addItem(
                productId = 20L,
                productName = "상품2",
                price = BigDecimal("20000"),
                quantity = 2
            )

            every { cartRepository.findByUserIdWithItems(userId) } returns Optional.of(cart)
            every { cartRepository.save(any()) } returns cart

            then("모든 아이템이 삭제되어야 한다") {
                val result = cartService.clearCart(userId)

                result.getActiveItems() shouldHaveSize 0

                verify(exactly = 1) { cartRepository.findByUserIdWithItems(userId) }
                verify(exactly = 1) { cartRepository.save(any()) }
            }
        }

        `when`("장바구니가 존재하지 않으면") {
            every { cartRepository.findByUserIdWithItems(userId) } returns Optional.empty()

            then("CartNotFoundException이 발생해야 한다") {
                val exception = shouldThrow<CartException.CartNotFoundException> {
                    cartService.clearCart(userId)
                }
                exception.message shouldBe "장바구니를 찾을 수 없습니다. userId=$userId"
            }
        }
    }

    given("CartService의 getCart 메서드가 주어졌을 때") {
        val userId = 100L

        `when`("장바구니가 존재하면") {
            val cart = Cart(
                id = 1L,
                userId = userId
            )

            every { cartRepository.findByUserIdWithItems(userId) } returns Optional.of(cart)

            then("장바구니를 반환해야 한다") {
                val result = cartService.getCart(userId)

                result.id shouldBe 1L
                result.userId shouldBe userId

                verify(exactly = 1) { cartRepository.findByUserIdWithItems(userId) }
            }
        }

        `when`("장바구니가 존재하지 않으면") {
            every { cartRepository.findByUserIdWithItems(userId) } returns Optional.empty()

            then("CartNotFoundException이 발생해야 한다") {
                val exception = shouldThrow<CartException.CartNotFoundException> {
                    cartService.getCart(userId)
                }
                exception.message shouldBe "장바구니를 찾을 수 없습니다. userId=$userId"
            }
        }
    }
})
