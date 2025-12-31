package com.ecommerce.cart.entity

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class CartItemTest : BehaviorSpec({

    lateinit var cart: Cart
    lateinit var cartItem: CartItem

    beforeEach {
        cart = Cart(
            id = 1L,
            userId = 100L
        )

        cartItem = CartItem(
            id = 1L,
            cart = cart,
            productId = 10L,
            productName = "테스트 상품",
            price = BigDecimal("10000"),
            quantity = 2
        )
    }

    given("CartItem 엔티티가 주어졌을 때") {

        `when`("수량을 변경할 때") {
            then("정상적으로 수량이 변경되어야 한다") {
                cartItem.updateQuantity(5)

                cartItem.quantity shouldBe 5
            }

            then("수량이 0 이하면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    cartItem.updateQuantity(0)
                }
                exception.message shouldBe "수량은 1 이상이어야 합니다"
            }

            then("음수 수량이면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    cartItem.updateQuantity(-1)
                }
                exception.message shouldBe "수량은 1 이상이어야 합니다"
            }
        }

        `when`("가격을 변경할 때") {
            then("정상적으로 가격이 변경되어야 한다") {
                cartItem.updatePrice(BigDecimal("15000"))

                cartItem.price shouldBe BigDecimal("15000")
            }

            then("가격이 음수면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    cartItem.updatePrice(BigDecimal("-1000"))
                }
                exception.message shouldBe "가격은 0 이상이어야 합니다"
            }

            then("가격이 0이면 정상적으로 변경되어야 한다") {
                cartItem.updatePrice(BigDecimal.ZERO)

                cartItem.price shouldBe BigDecimal.ZERO
            }
        }

        `when`("상품 정보를 변경할 때") {
            then("상품명과 가격이 모두 변경되어야 한다") {
                cartItem.updateProductInfo("새로운 상품명", BigDecimal("20000"))

                cartItem.productName shouldBe "새로운 상품명"
                cartItem.price shouldBe BigDecimal("20000")
            }

            then("가격이 음수면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    cartItem.updateProductInfo("새로운 상품명", BigDecimal("-5000"))
                }
                exception.message shouldBe "가격은 0 이상이어야 합니다"
            }
        }

        `when`("소계를 계산할 때") {
            then("가격 x 수량이 반환되어야 한다") {
                // price: 10000, quantity: 2
                val subtotal = cartItem.getSubtotal()

                subtotal shouldBe BigDecimal("20000")
            }

            then("수량이 1일 때 가격과 동일해야 한다") {
                cartItem.updateQuantity(1)

                cartItem.getSubtotal() shouldBe BigDecimal("10000")
            }

            then("가격이 0일 때 소계는 0이어야 한다") {
                cartItem.updatePrice(BigDecimal.ZERO)

                cartItem.getSubtotal() shouldBe BigDecimal.ZERO
            }

            then("수량 변경 시 소계도 변경되어야 한다") {
                cartItem.updateQuantity(5)

                cartItem.getSubtotal() shouldBe BigDecimal("50000")
            }

            then("가격 변경 시 소계도 변경되어야 한다") {
                cartItem.updatePrice(BigDecimal("20000"))

                cartItem.getSubtotal() shouldBe BigDecimal("40000")
            }
        }

        `when`("아이템을 소프트 삭제할 때") {
            then("삭제 상태가 되어야 한다") {
                cartItem.softDelete()

                cartItem.isDeleted().shouldBeTrue()
                cartItem.deletedAt.shouldNotBeNull()
            }
        }

        `when`("아이템을 복원할 때") {
            then("삭제 상태가 해제되어야 한다") {
                cartItem.softDelete()
                cartItem.restore()

                cartItem.isDeleted().shouldBeFalse()
            }
        }
    }
})
