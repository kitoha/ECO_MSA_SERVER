package com.ecommerce.cart

import com.ecommerce.cart.client.ProductClient
import com.ecommerce.cart.dto.external.ProductResponse
import com.ecommerce.cart.exception.CartException
import com.ecommerce.cart.generator.TsidGenerator
import com.ecommerce.cart.repository.CartItemRepository
import com.ecommerce.cart.repository.CartRepository
import com.ecommerce.cart.service.CartService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
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
import java.time.LocalDateTime

@DataJpaTest
@Transactional
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(TestConfig::class, CartService::class)
class CartServiceIntegrationTest(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val productClient: ProductClient,
    private val cartService: CartService
) : BehaviorSpec({
    extensions(SpringExtension)

    val userId = 100L
    val productId = TsidGenerator.encode(10L)

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

    beforeEach {
        clearMocks(productClient, answers = false)
        cartRepository.deleteAll()
        cartItemRepository.deleteAll()
    }

    afterEach {
        cartRepository.deleteAll()
        cartItemRepository.deleteAll()
    }

    given("실제 DB에서 CartService의 getOrCreateCart 메서드가 주어졌을 때") {
        `when`("장바구니가 존재하지 않으면") {
            then("새 장바구니를 생성해야 한다") {
                val cart = cartService.getOrCreateCart(userId)

                cart.userId shouldBe userId
                cartRepository.findAll() shouldHaveSize 1
            }
        }

        `when`("장바구니가 이미 존재하면") {
            then("기존 장바구니를 반환해야 한다") {
                val firstCart = cartService.getOrCreateCart(userId)
                val secondCart = cartService.getOrCreateCart(userId)

                firstCart.id shouldBe secondCart.id
                cartRepository.findAll() shouldHaveSize 1
            }
        }
    }

    given("실제 DB에서 CartService의 addItemToCart 메서드가 주어졌을 때") {
        `when`("유효한 상품을 장바구니에 추가하면") {
            every { productClient.getProductById(any()) } returns mockProduct

            then("상품이 장바구니에 추가되어야 한다") {
                val cart = cartService.addItemToCart(userId, productId, 2)

                cart.getActiveItems() shouldHaveSize 1
                cart.getActiveItems()[0].productId shouldBe TsidGenerator.decode(productId)
                cart.getActiveItems()[0].quantity shouldBe 2
                cart.getTotalPrice() shouldBe BigDecimal("20000")

                val savedCart = cartRepository.findByUserIdWithItems(userId).orElseThrow()
                savedCart.getActiveItems() shouldHaveSize 1
            }
        }

        `when`("같은 상품을 여러 번 추가하면") {
            every { productClient.getProductById(any()) } returns mockProduct

            then("수량이 증가해야 한다") {
                cartService.addItemToCart(userId, productId, 2)
                val cart = cartService.addItemToCart(userId, productId, 3)

                cart.getActiveItems() shouldHaveSize 1
                cart.getActiveItems()[0].quantity shouldBe 5
                cart.getTotalPrice() shouldBe BigDecimal("50000")
            }
        }

        `when`("존재하지 않는 상품을 추가하려고 하면") {
            every { productClient.getProductById(any()) } returns null

            then("ProductNotAvailableException이 발생해야 한다") {
                val exception = shouldThrow<CartException.ProductNotAvailableException> {
                    cartService.addItemToCart(userId, productId, 2)
                }
                exception.message shouldBe "상품을 사용할 수 없습니다. productId=${TsidGenerator.decode(productId)}"
            }
        }
    }

    given("실제 DB에서 CartService의 updateItemQuantity 메서드가 주어졌을 때") {
        `when`("아이템 수량을 변경하면") {
            every { productClient.getProductById(any()) } returns mockProduct

            then("수량이 업데이트되어야 한다") {
                val cart = cartService.addItemToCart(userId, productId, 2)
                val itemId = cart.getActiveItems()[0].id

                val updatedCart = cartService.updateItemQuantity(userId, itemId, 5)

                updatedCart.getActiveItems()[0].quantity shouldBe 5
                updatedCart.getTotalPrice() shouldBe BigDecimal("50000")

                val savedCart = cartRepository.findByUserIdWithItems(userId).orElseThrow()
                savedCart.getActiveItems()[0].quantity shouldBe 5
            }
        }

        `when`("존재하지 않는 장바구니의 아이템을 변경하려고 하면") {
            then("CartNotFoundException이 발생해야 한다") {
                val exception = shouldThrow<CartException.CartNotFoundException> {
                    cartService.updateItemQuantity(999L, 1L, 5)
                }
                exception.message shouldBe "장바구니를 찾을 수 없습니다. userId=999"
            }
        }
    }

    given("실제 DB에서 CartService의 removeItemFromCart 메서드가 주어졌을 때") {
        `when`("아이템을 삭제하면") {
            every { productClient.getProductById(any()) } returns mockProduct

            then("아이템이 소프트 삭제되어야 한다") {
                val cart = cartService.addItemToCart(userId, productId, 2)
                val itemId = cart.getActiveItems()[0].id

                val updatedCart = cartService.removeItemFromCart(userId, itemId)

                updatedCart.getActiveItems().shouldBeEmpty()
                updatedCart.getTotalPrice() shouldBe BigDecimal.ZERO

                val savedCart = cartRepository.findByUserIdWithItems(userId).orElseThrow()
                savedCart.getActiveItems().shouldBeEmpty()
            }
        }
    }

    given("실제 DB에서 CartService의 clearCart 메서드가 주어졌을 때") {
        `when`("장바구니를 비우면") {
            every { productClient.getProductById(any()) } returns mockProduct

            then("모든 아이템이 삭제되어야 한다") {
                cartService.addItemToCart(userId, TsidGenerator.encode(10L), 1)
                cartService.addItemToCart(userId, TsidGenerator.encode(20L), 2)

                val clearedCart = cartService.clearCart(userId)

                clearedCart.getActiveItems().shouldBeEmpty()
                clearedCart.getTotalPrice() shouldBe BigDecimal.ZERO

                val savedCart = cartRepository.findByUserIdWithItems(userId).orElseThrow()
                savedCart.getActiveItems().shouldBeEmpty()
            }
        }
    }

    given("실제 DB에서 CartService의 getCart 메서드가 주어졌을 때") {
        `when`("장바구니가 존재하면") {
            every { productClient.getProductById(any()) } returns mockProduct

            then("장바구니를 반환해야 한다") {
                cartService.addItemToCart(userId, productId, 2)

                val cart = cartService.getCart(userId)

                cart.userId shouldBe userId
                cart.getActiveItems() shouldHaveSize 1
            }
        }

        `when`("장바구니가 존재하지 않으면") {
            then("CartNotFoundException이 발생해야 한다") {
                val exception = shouldThrow<CartException.CartNotFoundException> {
                    cartService.getCart(999L)
                }
                exception.message shouldBe "장바구니를 찾을 수 없습니다. userId=999"
            }
        }
    }
}) {
    companion object {
        @Container
        @ServiceConnection
        val postgreSQLContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("cart_test_db")
            .withUsername("test")
            .withPassword("test")
    }
}
