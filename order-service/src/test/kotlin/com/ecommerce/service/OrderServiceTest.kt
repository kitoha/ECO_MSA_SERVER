package com.ecommerce.service

import com.ecommerce.dto.OrderItemDto
import com.ecommerce.entity.Order
import com.ecommerce.enums.OrderStatus
import com.ecommerce.generator.TsidGenerator
import com.ecommerce.repository.OrderRepository
import com.ecommerce.request.CreateOrderRequest
import com.ecommerce.request.OrderItemRequest
import com.ecommerce.util.OrderNumberGenerator
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.kafka.core.KafkaTemplate
import java.math.BigDecimal
import java.time.LocalDateTime

class OrderServiceTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val orderRepository = mockk<OrderRepository>()
    val orderItemService = mockk<OrderItemService>()
    val kafkaTemplate = mockk<KafkaTemplate<String, Any>>()
    val protoKafkaTemplate = mockk<KafkaTemplate<String, com.ecommerce.proto.order.OrderCreatedEvent>>()
    val orderNumberGenerator = mockk<OrderNumberGenerator>()
    val idGenerator = mockk<TsidGenerator>()

    val orderService = OrderService(orderRepository, orderItemService, kafkaTemplate, protoKafkaTemplate, orderNumberGenerator, idGenerator)

    beforeEach {
        clearMocks(orderRepository, orderItemService, kafkaTemplate, protoKafkaTemplate, idGenerator, answers = false)
    }

    given("OrderService의 createOrder 메서드가 주어졌을 때") {
        val userId = "user123"
        val request = CreateOrderRequest(
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

        val orderId = 236372517419679744L
        val savedOrder = Order(
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

        `when`("유효한 요청으로 주문을 생성하면") {
            every { idGenerator.generate() } returns orderId
            every { orderItemService.calculateOrderTotal(any()) } returns BigDecimal("100000")
            every { orderRepository.save(any()) } returns savedOrder
            every { orderItemService.addOrderItem(any(), any()) } just runs
            every { orderItemService.getOrderItems(any()) } returns listOf(
                OrderItemDto(
                    id = 1L,
                    orderId = savedOrder.id,
                    productId = "1",
                    productName = "테스트 상품",
                    price = BigDecimal("50000"),
                    quantity = 2,
                    subtotal = BigDecimal("100000")
                )
            )
            every { kafkaTemplate.send(any(), any(), any()) } returns mockk()
            every { protoKafkaTemplate.send(any(), any(), any()) } returns mockk()
            every { orderNumberGenerator.generate() } returns "ORD-20250128-000001"

            then("주문이 정상적으로 생성되어야 한다") {
                val response = orderService.createOrder(request, userId)

                response.userId shouldBe "user123"
                response.status shouldBe OrderStatus.PENDING
                response.totalAmount shouldBe BigDecimal("100000")
                response.shippingAddress shouldBe "서울시 강남구"

                verify(exactly = 1) { orderItemService.calculateOrderTotal(any()) }
                verify(exactly = 1) { orderRepository.save(any()) }
                verify(exactly = 1) { orderItemService.addOrderItem(any(), any()) }
                verify(exactly = 1) { orderItemService.getOrderItems(any()) }
                verify(exactly = 1) { kafkaTemplate.send("inventory-reservation-request", any(), any()) }
                verify(exactly = 1) { protoKafkaTemplate.send("order-created", any(), any()) }
            }
        }

        `when`("여러 상품을 포함한 주문을 생성하면") {
            val multiItemRequest = CreateOrderRequest(
                items = listOf(
                    OrderItemRequest(productId = "1", quantity = 2),
                    OrderItemRequest(productId = "2", quantity = 1)
                ),
                shippingAddress = "서울시 강남구",
                shippingName = "홍길동",
                shippingPhone = "010-1234-5678"
            )

            every { idGenerator.generate() } returns orderId
            every { orderItemService.calculateOrderTotal(any()) } returns BigDecimal("150000")
            every { orderRepository.save(any()) } returns savedOrder
            every { orderItemService.addOrderItem(any(), any()) } just runs
            every { orderItemService.getOrderItems(any()) } returns emptyList()
            every { kafkaTemplate.send(any(), any(), any()) } returns mockk()
            every { protoKafkaTemplate.send(any(), any(), any()) } returns mockk()
            every { orderNumberGenerator.generate() } returns "ORD-20250128-000002"

            then("모든 상품에 대해 재고 예약 요청이 발행되어야 한다") {
                orderService.createOrder(multiItemRequest, userId)

                verify(exactly = 2) { orderItemService.addOrderItem(any(), any()) }
                verify(exactly = 2) { kafkaTemplate.send("inventory-reservation-request", any(), any()) }
            }
        }
    }

    given("OrderService의 getOrder 메서드가 주어졌을 때") {
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

        `when`("존재하는 주문 ID로 조회하면") {
            every { orderRepository.findById(orderId) } returns order
            every { orderItemService.getOrderItems(orderId) } returns emptyList()

            then("주문 정보가 반환되어야 한다") {
                val response = orderService.getOrder(orderId)

                response.userId shouldBe "user123"
                response.status shouldBe OrderStatus.PENDING

                verify(exactly = 1) { orderRepository.findById(orderId) }
                verify(exactly = 1) { orderItemService.getOrderItems(orderId) }
            }
        }

        `when`("존재하지 않는 주문 ID로 조회하면") {
            val invalidId = 999L
            every { orderRepository.findById(invalidId) } returns null

            then("예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    orderService.getOrder(invalidId)
                }
                exception.message shouldBe "존재하지 않는 주문입니다: $invalidId"
            }
        }
    }

    given("OrderService의 getOrdersByUser 메서드가 주어졌을 때") {
        val orderId1 = 236372517419679744L
        val orderId2 = 236372517419679745L
        val orders = listOf(
            Order(
                id = orderId1,
                orderNumber = "ORD-20250128-000001",
                userId = "user123",
                status = OrderStatus.PENDING,
                totalAmount = BigDecimal("100000"),
                shippingAddress = "서울시 강남구",
                shippingName = "홍길동",
                shippingPhone = "010-1234-5678",
                orderedAt = LocalDateTime.now()
            ),
            Order(
                id = orderId2,
                orderNumber = "ORD-20250128-000002",
                userId = "user123",
                status = OrderStatus.CONFIRMED,
                totalAmount = BigDecimal("200000"),
                shippingAddress = "서울시 강남구",
                shippingName = "홍길동",
                shippingPhone = "010-1234-5678",
                orderedAt = LocalDateTime.now()
            )
        )

        `when`("사용자 ID로 주문 목록을 조회하면") {
            every { orderRepository.findByUserId("user123") } returns orders
            every { orderItemService.getOrderItems(any()) } returns emptyList()

            then("사용자의 모든 주문이 반환되어야 한다") {
                val response = orderService.getOrdersByUser("user123")

                response.size shouldBe 2
                response[0].userId shouldBe "user123"
                response[1].userId shouldBe "user123"

                verify(exactly = 1) { orderRepository.findByUserId("user123") }
            }
        }

        `when`("주문이 없는 사용자를 조회하면") {
            every { orderRepository.findByUserId("user999") } returns emptyList()

            then("빈 리스트가 반환되어야 한다") {
                val response = orderService.getOrdersByUser("user999")

                response.size shouldBe 0
            }
        }
    }

    given("OrderService의 cancelOrder 메서드가 주어졌을 때") {
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

        `when`("주문을 취소하면") {
            every { orderRepository.findById(orderId) } returns order
            every { orderRepository.save(any()) } returns order
            every { kafkaTemplate.send(any(), any(), any()) } returns mockk()

            then("주문 상태가 CANCELLED로 변경되어야 한다") {
                orderService.cancelOrder(orderId)

                order.status shouldBe OrderStatus.CANCELLED

                verify(exactly = 1) { orderRepository.findById(orderId) }
                verify(exactly = 1) { orderRepository.save(any()) }
                verify(exactly = 1) { kafkaTemplate.send("order-cancelled", any(), any()) }
            }
        }

        `when`("취소 사유와 함께 주문을 취소하면") {
            every { orderRepository.findById(orderId) } returns order
            every { orderRepository.save(any()) } returns order
            every { kafkaTemplate.send(any(), any(), any()) } returns mockk()

            then("취소 이벤트에 사유가 포함되어야 한다") {
                orderService.cancelOrder(orderId, "재고 부족")

                verify(exactly = 1) {
                    kafkaTemplate.send(
                        "order-cancelled",
                        any(),
                        match { event: Any ->
                            event.toString().contains("재고 부족")
                        }
                    )
                }
            }
        }
    }

    given("OrderService의 confirmOrder 메서드가 주어졌을 때") {
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

        `when`("주문을 확정하면") {
            every { orderRepository.findById(orderId) } returns order
            every { orderRepository.save(any()) } returns order
            every { kafkaTemplate.send(any(), any(), any()) } returns mockk()

            then("주문 상태가 CONFIRMED로 변경되어야 한다") {
                orderService.confirmOrder(orderId)

                order.status shouldBe OrderStatus.CONFIRMED

                verify(exactly = 1) { orderRepository.findById(orderId) }
                verify(exactly = 1) { orderRepository.save(any()) }
                verify(exactly = 1) { kafkaTemplate.send("order-confirmed", any(), any()) }
            }
        }
    }

    given("OrderService의 updateOrderStatus 메서드가 주어졌을 때") {
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

        `when`("유효한 상태로 변경하면") {
            every { orderRepository.findById(orderId) } returns order
            every { orderRepository.save(any()) } returns order

            then("주문 상태가 변경되어야 한다") {
                orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED)

                order.status shouldBe OrderStatus.CONFIRMED

                verify(exactly = 1) { orderRepository.findById(orderId) }
                verify(exactly = 1) { orderRepository.save(any()) }
            }
        }

        `when`("유효하지 않은 상태로 변경하면") {
            every { orderRepository.findById(orderId) } returns order

            then("예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED)
                }
                exception.message shouldBe "주문 상태를 PENDING 에서 SHIPPED 로 변경할 수 없습니다"
            }
        }
    }
})
