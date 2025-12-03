package com.ecommerce.service

import com.ecommerce.client.ProductClient
import com.ecommerce.dto.external.ProductResponse
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
    val productClient = mockk<ProductClient>()

    val orderItemService = OrderItemService(orderItemRepository, orderRepository, productClient)

    beforeEach {
        clearMocks(orderItemRepository, orderRepository, productClient, answers = false)
    }

    given("OrderItemService의 addOrderItem 메서드가 주어졌을 때") {
        val orderId = 236372517419679744L
        val order = Order(
            id = orderId,
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
            val mockProduct = ProductResponse(
                id = "1",
                name = "테스트 상품",
                description = "테스트 상품 설명",
                categoryId = 1L,
                categoryName = "전자제품",
                originalPrice = BigDecimal("60000"),
                salePrice = BigDecimal("50000"),
                discountRate = BigDecimal("16.67"),
                status = "ACTIVE",
                images = emptyList(),
                createdAt = null,
                updatedAt = null
            )

            every { orderRepository.findById(orderId) } returns order
            every { productClient.getProductById("1") } returns mockProduct
            every { orderItemRepository.save(any()) } returns mockk()

            then("주문 항목이 추가되어야 한다") {
                orderItemService.addOrderItem(orderId, itemRequest)

                order.items.size shouldBe 1
                order.items[0].productId shouldBe "1"
                order.items[0].quantity shouldBe 2
                order.items[0].productName shouldBe "테스트 상품"
                order.items[0].price shouldBe BigDecimal("50000")

                verify(exactly = 1) { orderRepository.findById(orderId) }
                verify(exactly = 1) { productClient.getProductById("1") }
                verify(exactly = 1) { orderItemRepository.save(any()) }
            }
        }

        `when`("존재하지 않는 주문에 항목을 추가하면") {
            val invalidId = 999L
            every { orderRepository.findById(invalidId) } returns null

            then("예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    orderItemService.addOrderItem(invalidId, itemRequest)
                }
                exception.message shouldBe "존재하지 않는 주문입니다: $invalidId"
            }
        }

        `when`("여러 항목을 추가하면") {
            val mockProduct1 = ProductResponse(
                id = "1",
                name = "테스트 상품1",
                description = "테스트 상품1 설명",
                categoryId = 1L,
                categoryName = "전자제품",
                originalPrice = BigDecimal("60000"),
                salePrice = BigDecimal("50000"),
                discountRate = BigDecimal("16.67"),
                status = "ACTIVE",
                images = emptyList(),
                createdAt = null,
                updatedAt = null
            )
            val mockProduct2 = ProductResponse(
                id = "2",
                name = "테스트 상품2",
                description = "테스트 상품2 설명",
                categoryId = 1L,
                categoryName = "전자제품",
                originalPrice = BigDecimal("35000"),
                salePrice = BigDecimal("30000"),
                discountRate = BigDecimal("14.29"),
                status = "ACTIVE",
                images = emptyList(),
                createdAt = null,
                updatedAt = null
            )

            every { orderRepository.findById(orderId) } returns order
            every { productClient.getProductById("1") } returns mockProduct1
            every { productClient.getProductById("2") } returns mockProduct2
            every { orderItemRepository.save(any()) } returns mockk()

            then("모든 항목이 추가되어야 한다") {
                orderItemService.addOrderItem(orderId, itemRequest)
                orderItemService.addOrderItem(orderId, OrderItemRequest(productId = "2", quantity = 1))

                order.items.size shouldBe 2

                verify(exactly = 2) { orderItemRepository.save(any()) }
                verify(exactly = 1) { productClient.getProductById("1") }
                verify(exactly = 1) { productClient.getProductById("2") }
            }
        }
    }

    given("OrderItemService의 getOrderItems 메서드가 주어졌을 때") {
        val orderId = 236372517419679744L
        val order = Order(
            id = orderId,
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

            every { orderItemRepository.findByOrderId(orderId) } returns listOf(orderItem1, orderItem2)

            then("주문의 모든 항목이 반환되어야 한다") {
                val result = orderItemService.getOrderItems(orderId)

                result shouldHaveSize 2
                result[0].productId shouldBe "PRODUCT-001"
                result[1].productId shouldBe "PRODUCT-002"

                verify(exactly = 1) { orderItemRepository.findByOrderId(orderId) }
            }
        }

        `when`("항목이 없는 주문을 조회하면") {
            val invalidId = 999L
            every { orderItemRepository.findByOrderId(invalidId) } returns emptyList()

            then("빈 리스트가 반환되어야 한다") {
                val result = orderItemService.getOrderItems(invalidId)

                result shouldHaveSize 0

                verify(exactly = 1) { orderItemRepository.findByOrderId(invalidId) }
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

            val mockProducts = listOf(
                ProductResponse(
                    id = "1",
                    name = "테스트 상품",
                    description = "테스트 상품 설명",
                    categoryId = 1L,
                    categoryName = "전자제품",
                    originalPrice = BigDecimal("60000"),
                    salePrice = BigDecimal("50000"),
                    discountRate = BigDecimal("16.67"),
                    status = "ACTIVE",
                    images = emptyList(),
                    createdAt = null,
                    updatedAt = null
                )
            )

            every { productClient.getProductsByIds(listOf("1")) } returns mockProducts

            then("정확한 총액이 계산되어야 한다") {
                val result = orderItemService.calculateOrderTotal(items)

                result shouldBe BigDecimal("100000")  // 50000 * 2
                verify(exactly = 1) { productClient.getProductsByIds(listOf("1")) }
            }
        }

        `when`("여러 상품의 총액을 계산하면") {
            val items = listOf(
                OrderItemRequest(productId = "1", quantity = 2),
                OrderItemRequest(productId = "2", quantity = 1)
            )

            val mockProducts = listOf(
                ProductResponse(
                    id = "1",
                    name = "테스트 상품1",
                    description = "테스트 상품1 설명",
                    categoryId = 1L,
                    categoryName = "전자제품",
                    originalPrice = BigDecimal("60000"),
                    salePrice = BigDecimal("50000"),
                    discountRate = BigDecimal("16.67"),
                    status = "ACTIVE",
                    images = emptyList(),
                    createdAt = null,
                    updatedAt = null
                ),
                ProductResponse(
                    id = "2",
                    name = "테스트 상품2",
                    description = "테스트 상품2 설명",
                    categoryId = 1L,
                    categoryName = "전자제품",
                    originalPrice = BigDecimal("35000"),
                    salePrice = BigDecimal("30000"),
                    discountRate = BigDecimal("14.29"),
                    status = "ACTIVE",
                    images = emptyList(),
                    createdAt = null,
                    updatedAt = null
                )
            )

            every { productClient.getProductsByIds(listOf("1", "2")) } returns mockProducts

            then("모든 상품의 총액 합이 계산되어야 한다") {
                val result = orderItemService.calculateOrderTotal(items)

                result shouldBe BigDecimal("130000")  // (50000 * 2) + (30000 * 1)
                verify(exactly = 1) { productClient.getProductsByIds(listOf("1", "2")) }
            }
        }

        `when`("항목이 없으면") {
            val items = emptyList<OrderItemRequest>()

            every { productClient.getProductsByIds(emptyList()) } returns emptyList()

            then("0이 반환되어야 한다") {
                val result = orderItemService.calculateOrderTotal(items)

                result shouldBe BigDecimal.ZERO
            }
        }
    }
})
