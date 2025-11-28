package com.ecommerce.entity

import com.ecommerce.enums.OrderStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDateTime

class OrderTest : BehaviorSpec({

    lateinit var order: Order

    beforeEach {
        order = Order(
            orderNumber = "ORD-20250128-000001",
            userId = "user123",
            status = OrderStatus.PENDING,
            totalAmount = BigDecimal("100000"),
            shippingAddress = "서울시 강남구",
            shippingName = "홍길동",
            shippingPhone = "010-1234-5678",
            orderedAt = LocalDateTime.now()
        )
    }

    given("Order 엔티티가 주어졌을 때") {

        `when`("주문 항목을 추가하면") {
            then("items 리스트에 추가되어야 한다") {
                val orderItem = OrderItem(
                    productId = "PRODUCT-001",
                    productName = "노트북",
                    price = BigDecimal("800000"),
                    quantity = 1
                )

                order.addItem(orderItem)

                order.items.size shouldBe 1
                order.items[0] shouldBe orderItem
                orderItem.order shouldBe order
            }

            then("여러 항목을 추가할 수 있어야 한다") {
                val item1 = OrderItem(
                    productId = "PRODUCT-001",
                    productName = "노트북",
                    price = BigDecimal("800000"),
                    quantity = 1
                )
                val item2 = OrderItem(
                    productId = "PRODUCT-002",
                    productName = "마우스",
                    price = BigDecimal("40000"),
                    quantity = 2
                )

                order.addItem(item1)
                order.addItem(item2)

                order.items.size shouldBe 2
            }
        }

        `when`("총 금액을 재계산하면") {
            then("모든 항목의 subtotal 합이 계산되어야 한다") {
                val item1 = OrderItem(
                    productId = "PRODUCT-001",
                    productName = "노트북",
                    price = BigDecimal("800000"),
                    quantity = 1
                )
                val item2 = OrderItem(
                    productId = "PRODUCT-002",
                    productName = "마우스",
                    price = BigDecimal("40000"),
                    quantity = 2
                )

                order.addItem(item1)
                order.addItem(item2)
                order.recalculateTotalAmount()

                order.totalAmount shouldBe BigDecimal("880000")
            }

            then("항목이 없으면 0이어야 한다") {
                order.recalculateTotalAmount()

                order.totalAmount shouldBe BigDecimal.ZERO
            }
        }

        `when`("주문을 확정하면") {
            then("PENDING 상태에서 CONFIRMED로 변경되어야 한다") {
                order.confirm()

                order.status shouldBe OrderStatus.CONFIRMED
            }

            then("PENDING이 아닌 상태에서는 예외가 발생해야 한다") {
                order.status = OrderStatus.CONFIRMED

                val exception = shouldThrow<IllegalArgumentException> {
                    order.confirm()
                }
                exception.message shouldBe "확정 가능한 상태가 아닙니다: CONFIRMED"
            }
        }

        `when`("주문을 취소하면") {
            then("PENDING 상태에서 취소할 수 있어야 한다") {
                order.cancel()

                order.status shouldBe OrderStatus.CANCELLED
            }

            then("CONFIRMED 상태에서 취소할 수 있어야 한다") {
                order.status = OrderStatus.CONFIRMED

                order.cancel()

                order.status shouldBe OrderStatus.CANCELLED
            }

            then("SHIPPED 상태에서는 취소할 수 없어야 한다") {
                order.status = OrderStatus.SHIPPED

                val exception = shouldThrow<IllegalArgumentException> {
                    order.cancel()
                }
                exception.message shouldBe "취소 가능한 상태가 아닙니다: SHIPPED"
            }

            then("DELIVERED 상태에서는 취소할 수 없어야 한다") {
                order.status = OrderStatus.DELIVERED

                val exception = shouldThrow<IllegalArgumentException> {
                    order.cancel()
                }
                exception.message shouldBe "취소 가능한 상태가 아닙니다: DELIVERED"
            }
        }

        `when`("배송을 시작하면") {
            then("CONFIRMED 상태에서 SHIPPED로 변경되어야 한다") {
                order.status = OrderStatus.CONFIRMED

                order.ship()

                order.status shouldBe OrderStatus.SHIPPED
            }

            then("CONFIRMED가 아닌 상태에서는 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    order.ship()
                }
                exception.message shouldBe "배송 시작 가능한 상태가 아닙니다: PENDING"
            }
        }

        `when`("배송을 완료하면") {
            then("SHIPPED 상태에서 DELIVERED로 변경되어야 한다") {
                order.status = OrderStatus.SHIPPED

                order.deliver()

                order.status shouldBe OrderStatus.DELIVERED
            }

            then("SHIPPED가 아닌 상태에서는 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    order.deliver()
                }
                exception.message shouldBe "배송 완료 가능한 상태가 아닙니다: PENDING"
            }
        }

        `when`("주문 상태를 변경하면") {
            then("유효한 전이인 경우 상태가 변경되어야 한다") {
                order.changeStatus(OrderStatus.CONFIRMED)
                order.status shouldBe OrderStatus.CONFIRMED

                order.changeStatus(OrderStatus.SHIPPED)
                order.status shouldBe OrderStatus.SHIPPED

                order.changeStatus(OrderStatus.DELIVERED)
                order.status shouldBe OrderStatus.DELIVERED
            }

            then("유효하지 않은 전이인 경우 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    order.changeStatus(OrderStatus.SHIPPED)
                }
                exception.message shouldBe "주문 상태를 PENDING 에서 SHIPPED 로 변경할 수 없습니다"
            }

            then("DELIVERED 상태에서는 어떤 상태로도 변경할 수 없어야 한다") {
                order.status = OrderStatus.DELIVERED

                val exception = shouldThrow<IllegalArgumentException> {
                    order.changeStatus(OrderStatus.CANCELLED)
                }
                exception.message shouldBe "주문 상태를 DELIVERED 에서 CANCELLED 로 변경할 수 없습니다"
            }

            then("CANCELLED 상태에서는 어떤 상태로도 변경할 수 없어야 한다") {
                order.status = OrderStatus.CANCELLED

                val exception = shouldThrow<IllegalArgumentException> {
                    order.changeStatus(OrderStatus.PENDING)
                }
                exception.message shouldBe "주문 상태를 CANCELLED 에서 PENDING 로 변경할 수 없습니다"
            }
        }

        `when`("취소 가능 여부를 확인하면") {
            then("PENDING 상태에서는 true를 반환해야 한다") {
                order.isCancellable().shouldBeTrue()
            }

            then("CONFIRMED 상태에서는 true를 반환해야 한다") {
                order.status = OrderStatus.CONFIRMED

                order.isCancellable().shouldBeTrue()
            }

            then("SHIPPED 상태에서는 false를 반환해야 한다") {
                order.status = OrderStatus.SHIPPED

                order.isCancellable().shouldBeFalse()
            }

            then("DELIVERED 상태에서는 false를 반환해야 한다") {
                order.status = OrderStatus.DELIVERED

                order.isCancellable().shouldBeFalse()
            }

            then("CANCELLED 상태에서는 false를 반환해야 한다") {
                order.status = OrderStatus.CANCELLED

                order.isCancellable().shouldBeFalse()
            }
        }
    }
})
