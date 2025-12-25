package com.ecommerce.entity

import com.ecommerce.enums.OrderStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDateTime

class OrderItemTest : BehaviorSpec({

    lateinit var order: Order
    lateinit var orderItem: OrderItem

    beforeEach {
        order = Order(
            id = 236372517419679744L,
            orderNumber = "ORD-20250128-000001",
            userId = "user123",
            status = OrderStatus.PENDING,
            totalAmount = BigDecimal("100000"),
            shippingAddress = "서울시 강남구",
            shippingName = "홍길동",
            shippingPhone = "010-1234-5678",
            orderedAt = LocalDateTime.now()
        )

        orderItem = OrderItem(
            productId = "PRODUCT-001",
            productName = "노트북",
            price = BigDecimal("800000"),
            quantity = 1
        )
    }

    given("OrderItem 엔티티가 주어졌을 때") {

        `when`("소계를 계산하면") {
            then("가격 * 수량이 계산되어야 한다") {
                val result = orderItem.subtotal

                result shouldBe BigDecimal("800000")
            }

            then("수량이 여러 개인 경우 정확히 계산되어야 한다") {
                val item = OrderItem(
                    productId = "PRODUCT-002",
                    productName = "마우스",
                    price = BigDecimal("40000"),
                    quantity = 3
                )

                val result = item.subtotal

                result shouldBe BigDecimal("120000")
            }

            then("소수점이 있는 가격도 정확히 계산되어야 한다") {
                val item = OrderItem(
                    productId = "PRODUCT-003",
                    productName = "케이블",
                    price = BigDecimal("15500.50"),
                    quantity = 2
                )

                val result = item.subtotal

                result shouldBe BigDecimal("31001.00")
            }
        }

        `when`("Order와 연결하면") {
            then("order 참조가 설정되어야 한다") {
                order.addItem(orderItem)

                orderItem.order shouldBe order
            }

            then("Order의 items 리스트에도 추가되어야 한다") {
                order.addItem(orderItem)

                order.items.contains(orderItem) shouldBe true
                order.items.size shouldBe 1
            }
        }

        `when`("toString을 호출하면") {
            then("주문 항목 정보가 문자열로 반환되어야 한다") {
                order.addItem(orderItem)

                val result = orderItem.toString()

                result shouldBe "OrderItem(id=0, productId='PRODUCT-001', productName='노트북', price=800000, quantity=1, subtotal=800000)"
            }
        }

        `when`("여러 OrderItem이 생성되면") {
            then("각각 독립적인 객체여야 한다") {
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

                item1.productId shouldBe "PRODUCT-001"
                item2.productId shouldBe "PRODUCT-002"
                item1.subtotal shouldBe BigDecimal("800000")
                item2.subtotal shouldBe BigDecimal("80000")
            }
        }

        `when`("수량이 0인 경우") {
            then("소계는 0이어야 한다") {
                val item = OrderItem(
                    productId = "PRODUCT-001",
                    productName = "테스트 상품",
                    price = BigDecimal("10000"),
                    quantity = 0
                )

                item.subtotal shouldBe BigDecimal.ZERO
            }
        }

        `when`("가격이 0인 경우") {
            then("소계는 0이어야 한다") {
                val item = OrderItem(
                    productId = "PRODUCT-001",
                    productName = "무료 상품",
                    price = BigDecimal.ZERO,
                    quantity = 10
                )

                item.subtotal shouldBe BigDecimal.ZERO
            }
        }
    }
})
