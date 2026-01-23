package com.ecommerce.cart.controller

import com.ecommerce.cart.dto.AddItemRequest
import com.ecommerce.cart.dto.UpdateQuantityRequest
import com.ecommerce.cart.entity.Cart
import com.ecommerce.cart.generator.TsidGenerator
import com.ecommerce.cart.service.CartService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import java.math.BigDecimal

class CartControllerTest : BehaviorSpec({

    val cartService = mockk<CartService>()
    val cartController = CartController(cartService)

    beforeEach {
        clearMocks(cartService, answers = false)
    }

    given("CartController의 getCart 엔드포인트가 주어졌을 때") {
        val userId = 100L
        val cart = Cart(
            id = 1L,
            userId = userId
        )

        `when`("장바구니를 조회하면") {
            every { cartService.getOrCreateCart(userId) } returns cart

            then("장바구니 정보를 반환하고 200 OK 응답을 반환해야 한다") {
                val response = cartController.getCart(userId)

                response.statusCode shouldBe HttpStatus.OK
                response.body?.userId shouldBe userId

                verify(exactly = 1) { cartService.getOrCreateCart(userId) }
            }
        }
    }

    given("CartController의 addItemToCart 엔드포인트가 주어졌을 때") {
        val userId = 100L
        val productIdLong = 10L
        val productIdString = TsidGenerator.encode(productIdLong)
        val request = AddItemRequest(
            productId = productIdString,
            quantity = 2
        )

        val cart = Cart(
            id = 1L,
            userId = userId
        )

        `when`("장바구니에 상품을 추가하면") {
            cart.addItem(
                productId = productIdLong,
                productName = "테스트 상품",
                price = BigDecimal("10000"),
                quantity = request.quantity
            )

            every { cartService.addItemToCart(userId, productIdString, request.quantity) } returns cart

            then("상품이 추가되고 200 OK 응답을 반환해야 한다") {
                val response = cartController.addItemToCart(userId, request)

                response.statusCode shouldBe HttpStatus.OK
                response.body?.userId shouldBe userId
                response.body?.items?.shouldHaveSize(1)
                response.body?.items?.get(0)?.let { item ->
                    item.productId shouldBe productIdString
                    item.quantity shouldBe request.quantity
                }

                verify(exactly = 1) { cartService.addItemToCart(userId, productIdString, request.quantity) }
            }
        }
    }

    given("CartController의 updateItemQuantity 엔드포인트가 주어졌을 때") {
        val userId = 100L
        val itemId = 5L
        val request = UpdateQuantityRequest(quantity = 3)

        val cart = Cart(
            id = 1L,
            userId = userId
        )

        `when`("장바구니 아이템 수량을 변경하면") {
            cart.addItem(
                productId = 10L,
                productName = "테스트 상품",
                price = BigDecimal("10000"),
                quantity = 2
            )
            val cartItemId = cart.getActiveItems()[0].id
            cart.updateItemQuantity(cartItemId, request.quantity)

            every { cartService.updateItemQuantity(userId, itemId, request.quantity) } returns cart

            then("수량이 변경되고 200 OK 응답을 반환해야 한다") {
                val response = cartController.updateItemQuantity(userId, itemId, request)

                response.statusCode shouldBe HttpStatus.OK
                response.body?.userId shouldBe userId
                response.body?.items?.get(0)?.quantity shouldBe request.quantity

                verify(exactly = 1) { cartService.updateItemQuantity(userId, itemId, request.quantity) }
            }
        }
    }

    given("CartController의 removeItemFromCart 엔드포인트가 주어졌을 때") {
        val userId = 100L
        val itemId = 5L

        val cart = Cart(
            id = 1L,
            userId = userId
        )

        `when`("장바구니 아이템을 삭제하면") {
            every { cartService.removeItemFromCart(userId, itemId) } returns cart

            then("아이템이 삭제되고 200 OK 응답을 반환해야 한다") {
                val response = cartController.removeItemFromCart(userId, itemId)

                response.statusCode shouldBe HttpStatus.OK
                response.body?.userId shouldBe userId

                verify(exactly = 1) { cartService.removeItemFromCart(userId, itemId) }
            }
        }
    }

    given("CartController의 clearCart 엔드포인트가 주어졌을 때") {
        val userId = 100L

        val cart = Cart(
            id = 1L,
            userId = userId
        )

        `when`("장바구니를 비우면") {
            every { cartService.clearCart(userId) } returns cart

            then("장바구니가 비워지고 200 OK 응답을 반환해야 한다") {
                val response = cartController.clearCart(userId)

                response.statusCode shouldBe HttpStatus.OK
                response.body?.userId shouldBe userId
                response.body?.items?.shouldHaveSize(0)

                verify(exactly = 1) { cartService.clearCart(userId) }
            }
        }
    }
})
