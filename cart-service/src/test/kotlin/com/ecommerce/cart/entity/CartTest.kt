package com.ecommerce.cart.entity

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class CartTest : BehaviorSpec({

    lateinit var cart: Cart

    beforeEach {
        cart = Cart(
            id = 1L,
            userId = 100L
        )
    }

    given("Cart 엔티티가 주어졌을 때") {

        `when`("장바구니에 상품을 추가할 때") {
            then("정상적으로 상품이 추가되어야 한다") {
                cart.addItem(
                    productId = 1L,
                    productName = "테스트 상품",
                    price = BigDecimal("10000"),
                    quantity = 2
                )

                cart.getActiveItems() shouldHaveSize 1
                val item = cart.getActiveItems()[0]
                item.productId shouldBe 1L
                item.productName shouldBe "테스트 상품"
                item.price shouldBe BigDecimal("10000")
                item.quantity shouldBe 2
            }

            then("같은 상품을 추가하면 수량이 증가해야 한다") {
                cart.addItem(
                    productId = 1L,
                    productName = "테스트 상품",
                    price = BigDecimal("10000"),
                    quantity = 2
                )
                cart.addItem(
                    productId = 1L,
                    productName = "테스트 상품",
                    price = BigDecimal("10000"),
                    quantity = 3
                )

                cart.getActiveItems() shouldHaveSize 1
                cart.getActiveItems()[0].quantity shouldBe 5
            }

            then("수량이 0 이하면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    cart.addItem(
                        productId = 1L,
                        productName = "테스트 상품",
                        price = BigDecimal("10000"),
                        quantity = 0
                    )
                }
                exception.message shouldBe "수량은 1 이상이어야 합니다"
            }

            then("가격이 음수면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    cart.addItem(
                        productId = 1L,
                        productName = "테스트 상품",
                        price = BigDecimal("-1000"),
                        quantity = 1
                    )
                }
                exception.message shouldBe "가격은 0 이상이어야 합니다"
            }
        }

        `when`("장바구니 아이템 수량을 변경할 때") {
            then("정상적으로 수량이 변경되어야 한다") {
                cart.addItem(
                    productId = 1L,
                    productName = "테스트 상품",
                    price = BigDecimal("10000"),
                    quantity = 2
                )

                val itemId = cart.getActiveItems()[0].id
                cart.updateItemQuantity(itemId, 5)

                cart.getActiveItems()[0].quantity shouldBe 5
            }

            then("존재하지 않는 아이템이면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    cart.updateItemQuantity(999L, 5)
                }
                exception.message shouldBe "장바구니 아이템을 찾을 수 없습니다: 999"
            }

            then("수량이 0 이하면 예외가 발생해야 한다") {
                cart.addItem(
                    productId = 1L,
                    productName = "테스트 상품",
                    price = BigDecimal("10000"),
                    quantity = 2
                )

                val itemId = cart.getActiveItems()[0].id
                val exception = shouldThrow<IllegalArgumentException> {
                    cart.updateItemQuantity(itemId, 0)
                }
                exception.message shouldBe "수량은 1 이상이어야 합니다"
            }
        }

        `when`("장바구니 아이템을 삭제할 때") {
            then("정상적으로 소프트 삭제되어야 한다") {
                cart.addItem(
                    productId = 1L,
                    productName = "테스트 상품",
                    price = BigDecimal("10000"),
                    quantity = 2
                )

                val itemId = cart.getActiveItems()[0].id
                cart.removeItem(itemId)

                cart.getActiveItems().shouldBeEmpty()
            }

            then("존재하지 않는 아이템이면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    cart.removeItem(999L)
                }
                exception.message shouldBe "장바구니 아이템을 찾을 수 없습니다: 999"
            }
        }

        `when`("장바구니를 비울 때") {
            then("모든 아이템이 소프트 삭제되어야 한다") {
                cart.addItem(
                    productId = 1L,
                    productName = "상품1",
                    price = BigDecimal("10000"),
                    quantity = 1
                )
                cart.addItem(
                    productId = 2L,
                    productName = "상품2",
                    price = BigDecimal("20000"),
                    quantity = 2
                )

                cart.clear()

                cart.getActiveItems().shouldBeEmpty()
            }
        }

        `when`("총 가격을 계산할 때") {
            then("모든 활성 아이템의 가격 합계가 반환되어야 한다") {
                cart.addItem(
                    productId = 1L,
                    productName = "상품1",
                    price = BigDecimal("10000"),
                    quantity = 2
                )
                cart.addItem(
                    productId = 2L,
                    productName = "상품2",
                    price = BigDecimal("20000"),
                    quantity = 1
                )

                cart.getTotalPrice() shouldBe BigDecimal("40000")
            }

            then("아이템이 없으면 0이어야 한다") {
                cart.getTotalPrice() shouldBe BigDecimal.ZERO
            }

            then("삭제된 아이템은 포함하지 않아야 한다") {
                cart.addItem(
                    productId = 1L,
                    productName = "상품1",
                    price = BigDecimal("10000"),
                    quantity = 2
                )
                cart.addItem(
                    productId = 2L,
                    productName = "상품2",
                    price = BigDecimal("20000"),
                    quantity = 1
                )

                val itemId = cart.getActiveItems()[0].id
                cart.removeItem(itemId)

                cart.getTotalPrice() shouldBe BigDecimal("20000")
            }
        }

        `when`("총 아이템 수량을 계산할 때") {
            then("모든 활성 아이템의 수량 합계가 반환되어야 한다") {
                cart.addItem(
                    productId = 1L,
                    productName = "상품1",
                    price = BigDecimal("10000"),
                    quantity = 2
                )
                cart.addItem(
                    productId = 2L,
                    productName = "상품2",
                    price = BigDecimal("20000"),
                    quantity = 3
                )

                cart.getTotalItemCount() shouldBe 5
            }

            then("아이템이 없으면 0이어야 한다") {
                cart.getTotalItemCount() shouldBe 0
            }

            then("삭제된 아이템은 포함하지 않아야 한다") {
                cart.addItem(
                    productId = 1L,
                    productName = "상품1",
                    price = BigDecimal("10000"),
                    quantity = 2
                )
                cart.addItem(
                    productId = 2L,
                    productName = "상품2",
                    price = BigDecimal("20000"),
                    quantity = 3
                )

                val itemId = cart.getActiveItems()[0].id
                cart.removeItem(itemId)

                cart.getTotalItemCount() shouldBe 3
            }
        }

        `when`("활성 아이템을 조회할 때") {
            then("삭제되지 않은 아이템만 반환되어야 한다") {
                cart.addItem(
                    productId = 1L,
                    productName = "상품1",
                    price = BigDecimal("10000"),
                    quantity = 1
                )
                cart.addItem(
                    productId = 2L,
                    productName = "상품2",
                    price = BigDecimal("20000"),
                    quantity = 1
                )

                val itemId = cart.getActiveItems()[0].id
                cart.removeItem(itemId)

                cart.getActiveItems() shouldHaveSize 1
                cart.getActiveItems()[0].productId shouldBe 2L
            }
        }
    }
})
