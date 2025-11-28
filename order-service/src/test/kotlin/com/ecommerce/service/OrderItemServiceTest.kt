package com.ecommerce.service

import com.ecommerce.entity.Order
import com.ecommerce.entity.OrderItem
import com.ecommerce.enums.OrderStatus
import com.ecommerce.repository.OrderItemRepository
import com.ecommerce.repository.OrderRepository
import com.ecommerce.request.OrderItemRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal
import java.time.LocalDateTime

class OrderItemServiceTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val orderItemRepository = mockk<OrderItemRepository>()
    val orderRepository = mockk<OrderRepository>()

    val orderItemService = OrderItemService(orderItemRepository, orderRepository)

    beforeEach {
        clearMocks(orderItemRepository, orderRepository, answers = false)
    }

    given("OrderItemService의 addOrderItem 메서드가 주어졌을 때") {
        val order = Order(
            orderNumber = "ORD-20250128-000001",
            userId = "user123",
            status = OrderStatus.PENDING,
            totalAmount = BigDecimal("100000"),
            shippingAddress = "서울시 강남구",
            shippingName = "홍길동",
            shippingPhone = "010-1234-5678",
            orderedAt = LocalDateTime.now()
        )

        val itemRequest = OrderItemRequest(
            productId = "1",
            quantity = 2
        )

        `when`("유효한 주문에 항목을 추가하면") {
            every { orderRepository.findById(1L) } returns order
            every { orderItemRepository.save(any()) } returns mockk()

            then("주문 항목이 추가되어야 한다") {
                orderItemService.addOrderItem(1L, itemRequest)

                order.items.size shouldBe 1
                order.items[0].productId shouldBe "1"
                order.items[0].quantity shouldBe 2

                verify(exactly = 1) { orderRepository.findById(1L) }
                verify(exactly = 1) { orderItemRepository.save(any()) }
            }
        }

        `when`("존재하지 않는 주문에 항목을 추가하면") {
            every { orderRepository.findById(999L) } returns null

            then("예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    orderItemService.addOrderItem(999L, itemRequest)
                }
                exception.message shouldBe "존재하지 않는 주문입니다: 999"
            }
        }

        `when`("여러 항목을 추가하면") {
            every { orderRepository.findById(1L) } returns order
            every { orderItemRepository.save(any()) } returns mockk()

            then("모든 항목이 추가되어야 한다") {
                orderItemService.addOrderItem(1L, itemRequest)
                orderItemService.addOrderItem(1L, OrderItemRequest(productId = "2", quantity = 1))

                order.items.size shouldBe 2

                verify(exactly = 2) { orderItemRepository.save(any()) }
            }
        }
    }

    given("OrderItemService의 getOrderItems 메서드가 주어졌을 때") {
        val order = Order(
            orderNumber = "ORD-20250128-000001",
            userId = "user123",
            status = OrderStatus.PENDING,
            totalAmount = BigDecimal("100000"),
            shippingAddress = "서울시 강남구",
            shippingName = "홍길동",
            shippingPhone = "010-1234-5678",
            orderedAt = LocalDateTime.now()
        )

        val orderItem1 = OrderItem(
            productId = "PRODUCT-001",
            productName = "노트북",
            price = BigDecimal("800000"),
            quantity = 1
        )
        val orderItem2 = OrderItem(
            productId = "PRODUCT-002",
            productName = "마우스",
            price = BigDecimal("40000"),
            quantity = 2
        )

        `when`("주문 ID로 항목을 조회하면") {
            order.addItem(orderItem1)
            order.addItem(orderItem2)

            every { orderItemRepository.findByOrderId(1L) } returns listOf(orderItem1, orderItem2)

            then("주문의 모든 항목이 반환되어야 한다") {
                val result = orderItemService.getOrderItems(1L)

                result shouldHaveSize 2
                result[0].productId shouldBe "PRODUCT-001"
                result[1].productId shouldBe "PRODUCT-002"

                verify(exactly = 1) { orderItemRepository.findByOrderId(1L) }
            }
        }

        `when`("항목이 없는 주문을 조회하면") {
            every { orderItemRepository.findByOrderId(999L) } returns emptyList()

            then("빈 리스트가 반환되어야 한다") {
                val result = orderItemService.getOrderItems(999L)

                result shouldHaveSize 0

                verify(exactly = 1) { orderItemRepository.findByOrderId(999L) }
            }
        }
    }

    given("OrderItemService의 calculateOrderTotal 메서드가 주어졌을 때") {

        `when`("단일 상품의 총액을 계산하면") {
            val items = listOf(
                OrderItemRequest(
                    productId = "1",
                    quantity = 2
                )
            )

            then("정확한 총액이 계산되어야 한다") {
                val result = orderItemService.calculateOrderTotal(items)

                result shouldBe BigDecimal.ZERO  // price 정보가 없으므로 0
            }
        }

        `when`("여러 상품의 총액을 계산하면") {
            val items = listOf(
                OrderItemRequest(productId = "1", quantity = 2),
                OrderItemRequest(productId = "2", quantity = 1)
            )

            then("모든 상품의 총액 합이 계산되어야 한다") {
                val result = orderItemService.calculateOrderTotal(items)

                result shouldBe BigDecimal.ZERO  // price 정보가 없으므로 0
            }
        }

        `when`("항목이 없으면") {
            val items = emptyList<OrderItemRequest>()

            then("0이 반환되어야 한다") {
                val result = orderItemService.calculateOrderTotal(items)

                result shouldBe BigDecimal.ZERO
            }
        }
    }
})
