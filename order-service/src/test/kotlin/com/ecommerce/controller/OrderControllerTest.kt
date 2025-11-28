package com.ecommerce.controller

import com.ecommerce.enums.OrderStatus
import com.ecommerce.request.CreateOrderRequest
import com.ecommerce.request.OrderItemRequest
import com.ecommerce.response.OrderItemResponse
import com.ecommerce.response.OrderResponse
import com.ecommerce.service.OrderService
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.math.BigDecimal
import java.time.LocalDateTime

class OrderControllerTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val orderService = mockk<OrderService>()
    val orderController = OrderController(orderService)

    beforeEach {
        clearMocks(orderService, answers = false)
    }

    given("OrderController의 createOrder 엔드포인트가 주어졌을 때") {
        val request = CreateOrderRequest(
            userId = "user123",
            items = listOf(
                OrderItemRequest(
                    productId = "1",
                    quantity = 2
                )
            ),
            shippingAddress = "서울시 강남구",
            shippingName = "홍길동",
            shippingPhone = "010-1234-5678"
        )

        val expectedResponse = OrderResponse(
            id = 1L,
            orderNumber = "ORD-20250128-000001",
            userId = "user123",
            status = OrderStatus.PENDING,
            totalAmount = BigDecimal("100000"),
            shippingAddress = "서울시 강남구",
            shippingName = "홍길동",
            shippingPhone = "010-1234-5678",
            items = listOf(
                OrderItemResponse(
                    id = 1L,
                    productId = "1",
                    productName = "테스트 상품",
                    price = BigDecimal("50000"),
                    quantity = 2,
                    subtotal = BigDecimal("100000")
                )
            ),
            orderedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`("POST /api/orders로 주문 생성 요청을 보내면") {
            every { orderService.createOrder(request) } returns expectedResponse

            then("주문이 생성되고 응답이 반환되어야 한다") {
                val response = orderController.createOrder(request)

                response.id shouldBe 1L
                response.orderNumber shouldBe "ORD-20250128-000001"
                response.userId shouldBe "user123"
                response.status shouldBe OrderStatus.PENDING
                response.totalAmount shouldBe BigDecimal("100000")

                verify(exactly = 1) { orderService.createOrder(request) }
            }
        }

        `when`("여러 상품을 포함한 주문 생성 요청을 보내면") {
            val multiItemRequest = CreateOrderRequest(
                userId = "user123",
                items = listOf(
                    OrderItemRequest(productId = "1", quantity = 2),
                    OrderItemRequest(productId = "2", quantity = 1)
                ),
                shippingAddress = "서울시 강남구",
                shippingName = "홍길동",
                shippingPhone = "010-1234-5678"
            )

            val multiItemResponse = expectedResponse.copy(
                items = listOf(
                    OrderItemResponse(
                        id = 1L,
                        productId = "1",
                        productName = "테스트 상품 1",
                        price = BigDecimal("50000"),
                        quantity = 2,
                        subtotal = BigDecimal("100000")
                    ),
                    OrderItemResponse(
                        id = 2L,
                        productId = "2",
                        productName = "테스트 상품 2",
                        price = BigDecimal("30000"),
                        quantity = 1,
                        subtotal = BigDecimal("30000")
                    )
                ),
                totalAmount = BigDecimal("130000")
            )

            every { orderService.createOrder(multiItemRequest) } returns multiItemResponse

            then("여러 상품이 포함된 주문이 생성되어야 한다") {
                val response = orderController.createOrder(multiItemRequest)

                response.items shouldHaveSize 2
                response.totalAmount shouldBe BigDecimal("130000")

                verify(exactly = 1) { orderService.createOrder(multiItemRequest) }
            }
        }
    }

    given("OrderController의 getOrder 엔드포인트가 주어졌을 때") {
        val expectedResponse = OrderResponse(
            id = 1L,
            orderNumber = "ORD-20250128-000001",
            userId = "user123",
            status = OrderStatus.PENDING,
            totalAmount = BigDecimal("100000"),
            shippingAddress = "서울시 강남구",
            shippingName = "홍길동",
            shippingPhone = "010-1234-5678",
            items = emptyList(),
            orderedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`("GET /api/orders/{orderId}로 주문 조회 요청을 보내면") {
            every { orderService.getOrder(1L) } returns expectedResponse

            then("주문 정보가 반환되어야 한다") {
                val response = orderController.getOrder(1L)

                response.id shouldBe 1L
                response.orderNumber shouldBe "ORD-20250128-000001"
                response.userId shouldBe "user123"

                verify(exactly = 1) { orderService.getOrder(1L) }
            }
        }

        `when`("여러 주문을 순차적으로 조회하면") {
            val response2 = expectedResponse.copy(id = 2L, orderNumber = "ORD-20250128-000002")

            every { orderService.getOrder(1L) } returns expectedResponse
            every { orderService.getOrder(2L) } returns response2

            then("각각의 주문 정보가 반환되어야 한다") {
                val result1 = orderController.getOrder(1L)
                val result2 = orderController.getOrder(2L)

                result1.id shouldBe 1L
                result2.id shouldBe 2L

                verify(exactly = 1) { orderService.getOrder(1L) }
                verify(exactly = 1) { orderService.getOrder(2L) }
            }
        }
    }

    given("OrderController의 getOrdersByUser 엔드포인트가 주어졌을 때") {
        val orderResponses = listOf(
            OrderResponse(
                id = 1L,
                orderNumber = "ORD-20250128-000001",
                userId = "user123",
                status = OrderStatus.PENDING,
                totalAmount = BigDecimal("100000"),
                shippingAddress = "서울시 강남구",
                shippingName = "홍길동",
                shippingPhone = "010-1234-5678",
                items = emptyList(),
                orderedAt = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            OrderResponse(
                id = 2L,
                orderNumber = "ORD-20250128-000002",
                userId = "user123",
                status = OrderStatus.CONFIRMED,
                totalAmount = BigDecimal("200000"),
                shippingAddress = "서울시 강남구",
                shippingName = "홍길동",
                shippingPhone = "010-1234-5678",
                items = emptyList(),
                orderedAt = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        `when`("GET /api/orders/user/{userId}로 사용자 주문 목록 조회 요청을 보내면") {
            every { orderService.getOrdersByUser("user123") } returns orderResponses

            then("사용자의 모든 주문 목록이 반환되어야 한다") {
                val response = orderController.getOrdersByUser("user123")

                response shouldHaveSize 2
                response[0].userId shouldBe "user123"
                response[1].userId shouldBe "user123"

                verify(exactly = 1) { orderService.getOrdersByUser("user123") }
            }
        }

        `when`("주문이 없는 사용자를 조회하면") {
            every { orderService.getOrdersByUser("user999") } returns emptyList()

            then("빈 리스트가 반환되어야 한다") {
                val response = orderController.getOrdersByUser("user999")

                response shouldHaveSize 0

                verify(exactly = 1) { orderService.getOrdersByUser("user999") }
            }
        }
    }

    given("OrderController의 cancelOrder 엔드포인트가 주어졌을 때") {

        `when`("POST /api/orders/{orderId}/cancel로 주문 취소 요청을 보내면") {
            every { orderService.cancelOrder(1L, any()) } just runs

            then("주문이 취소되어야 한다") {
                orderController.cancelOrder(1L)

                verify(exactly = 1) { orderService.cancelOrder(1L, any()) }
            }
        }

        `when`("여러 주문을 취소하면") {
            every { orderService.cancelOrder(any(), any()) } just runs

            then("각각의 주문이 취소되어야 한다") {
                orderController.cancelOrder(1L)
                orderController.cancelOrder(2L)

                verify(exactly = 1) { orderService.cancelOrder(1L, any()) }
                verify(exactly = 1) { orderService.cancelOrder(2L, any()) }
            }
        }
    }
})
