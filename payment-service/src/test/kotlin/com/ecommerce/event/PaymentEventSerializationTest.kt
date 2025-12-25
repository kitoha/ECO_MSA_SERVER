package com.ecommerce.event

import com.ecommerce.enums.PaymentMethod
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import java.math.BigDecimal
import java.time.LocalDateTime

class PaymentEventSerializationTest : BehaviorSpec({
  isolationMode = IsolationMode.InstancePerLeaf

  val objectMapper = ObjectMapper().apply {
    registerKotlinModule()
    registerModule(JavaTimeModule())
  }

  given("PaymentCreatedEvent가 주어졌을 때") {

    `when`("이벤트를 직렬화/역직렬화하면") {
      val event = PaymentCreatedEvent(
        paymentId = 1L,
        orderId = "ORDER-001",
        userId = "USER-001",
        amount = BigDecimal("10000.00"),
        paymentMethod = PaymentMethod.CARD,
        timestamp = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
      )

      then("JSON으로 올바르게 직렬화되어야 한다") {
        val json = objectMapper.writeValueAsString(event)
        json shouldContain "paymentCreated"
        json shouldContain "ORDER-001"
        json shouldContain "CARD"
      }

      then("JSON에서 올바르게 역직렬화되어야 한다") {
        val json = objectMapper.writeValueAsString(event)
        val deserialized = objectMapper.readValue(json, PaymentCreatedEvent::class.java)
        deserialized.paymentId shouldBe 1L
        deserialized.orderId shouldBe "ORDER-001"
        deserialized.userId shouldBe "USER-001"
        deserialized.amount shouldBe BigDecimal("10000.00")
        deserialized.paymentMethod shouldBe PaymentMethod.CARD
      }
    }

    `when`("paymentMethod가 null인 이벤트를 직렬화/역직렬화하면") {
      val event = PaymentCreatedEvent(
        paymentId = 1L,
        orderId = "ORDER-001",
        userId = "USER-001",
        amount = BigDecimal("10000.00"),
        paymentMethod = null
      )

      then("null을 올바르게 처리해야 한다") {
        val json = objectMapper.writeValueAsString(event)
        val deserialized = objectMapper.readValue(json, PaymentCreatedEvent::class.java)
        deserialized.paymentMethod shouldBe null
      }
    }
  }

  given("PaymentCompletedEvent가 주어졌을 때") {

    `when`("이벤트를 직렬화/역직렬화하면") {
      val event = PaymentCompletedEvent(
        paymentId = 1L,
        orderId = "ORDER-001",
        userId = "USER-001",
        amount = BigDecimal("10000.00"),
        pgProvider = "TOSS",
        pgPaymentKey = "PG-KEY-001",
        timestamp = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
      )

      then("JSON으로 올바르게 직렬화되어야 한다") {
        val json = objectMapper.writeValueAsString(event)
        json shouldContain "paymentCompleted"
        json shouldContain "TOSS"
        json shouldContain "PG-KEY-001"
      }

      then("JSON에서 올바르게 역직렬화되어야 한다") {
        val json = objectMapper.writeValueAsString(event)
        val deserialized = objectMapper.readValue(json, PaymentCompletedEvent::class.java)
        deserialized.paymentId shouldBe 1L
        deserialized.orderId shouldBe "ORDER-001"
        deserialized.pgProvider shouldBe "TOSS"
        deserialized.pgPaymentKey shouldBe "PG-KEY-001"
      }
    }
  }

  given("PaymentFailedEvent가 주어졌을 때") {

    `when`("이벤트를 직렬화/역직렬화하면") {
      val event = PaymentFailedEvent(
        paymentId = 1L,
        orderId = "ORDER-001",
        userId = "USER-001",
        amount = BigDecimal("10000.00"),
        failureReason = "카드 한도 초과",
        timestamp = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
      )

      then("JSON으로 올바르게 직렬화되어야 한다") {
        val json = objectMapper.writeValueAsString(event)
        json shouldContain "paymentFailed"
        json shouldContain "카드 한도 초과"
      }

      then("JSON에서 올바르게 역직렬화되어야 한다") {
        val json = objectMapper.writeValueAsString(event)
        val deserialized = objectMapper.readValue(json, PaymentFailedEvent::class.java)
        deserialized.paymentId shouldBe 1L
        deserialized.failureReason shouldBe "카드 한도 초과"
      }
    }
  }

  given("PaymentCancelledEvent가 주어졌을 때") {

    `when`("이벤트를 직렬화/역직렬화하면") {
      val event = PaymentCancelledEvent(
        paymentId = 1L,
        orderId = "ORDER-001",
        userId = "USER-001",
        amount = BigDecimal("10000.00"),
        reason = "사용자 취소",
        timestamp = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
      )

      then("JSON으로 올바르게 직렬화되어야 한다") {
        val json = objectMapper.writeValueAsString(event)
        json shouldContain "paymentCancelled"
        json shouldContain "사용자 취소"
      }

      then("JSON에서 올바르게 역직렬화되어야 한다") {
        val json = objectMapper.writeValueAsString(event)
        val deserialized = objectMapper.readValue(json, PaymentCancelledEvent::class.java)
        deserialized.paymentId shouldBe 1L
        deserialized.reason shouldBe "사용자 취소"
      }
    }
  }

  given("PaymentRefundedEvent가 주어졌을 때") {

    `when`("이벤트를 직렬화/역직렬화하면") {
      val event = PaymentRefundedEvent(
        paymentId = 1L,
        orderId = "ORDER-001",
        userId = "USER-001",
        amount = BigDecimal("10000.00"),
        reason = "상품 환불",
        timestamp = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
      )

      then("JSON으로 올바르게 직렬화되어야 한다") {
        val json = objectMapper.writeValueAsString(event)
        json shouldContain "paymentRefunded"
        json shouldContain "상품 환불"
      }

      then("JSON에서 올바르게 역직렬화되어야 한다") {
        val json = objectMapper.writeValueAsString(event)
        val deserialized = objectMapper.readValue(json, PaymentRefundedEvent::class.java)
        deserialized.paymentId shouldBe 1L
        deserialized.reason shouldBe "상품 환불"
      }
    }
  }

  given("OrderCreatedEvent가 주어졌을 때") {

    `when`("이벤트를 직렬화/역직렬화하면") {
      val items = listOf(
        OrderItemData(
          productId = "1",
          productName = "상품1",
          price = BigDecimal("5000.00"),
          quantity = 2
        ),
        OrderItemData(
          productId = "2",
          productName = "상품2",
          price = BigDecimal("3000.00"),
          quantity = 1
        )
      )

      val event = OrderCreatedEvent(
        orderId = 1L,
        orderNumber = "ORDER-001",
        userId = "USER-001",
        items = items,
        totalAmount = BigDecimal("13000.00"),
        shippingAddress = "서울시 강남구",
        shippingName = "홍길동",
        shippingPhone = "010-1234-5678",
        timestamp = LocalDateTime.of(2025, 1, 1, 10, 0, 0)
      )

      then("JSON으로 올바르게 직렬화되어야 한다") {
        val json = objectMapper.writeValueAsString(event)
        json shouldContain "orderCreated"
        json shouldContain "ORDER-001"
        json shouldContain "상품1"
        json shouldContain "서울시 강남구"
      }

      then("JSON에서 올바르게 역직렬화되어야 한다") {
        val json = objectMapper.writeValueAsString(event)
        val deserialized = objectMapper.readValue(json, OrderCreatedEvent::class.java)
        deserialized.orderId shouldBe 1L
        deserialized.orderNumber shouldBe "ORDER-001"
        deserialized.items.size shouldBe 2
        deserialized.items[0].productId shouldBe "1"
        deserialized.items[0].productName shouldBe "상품1"
        deserialized.shippingAddress shouldBe "서울시 강남구"
      }
    }
  }

  given("OrderItemData가 주어졌을 때") {

    `when`("데이터를 직렬화/역직렬화하면") {
      val item = OrderItemData(
        productId = "1",
        productName = "테스트 상품",
        price = BigDecimal("15000.00"),
        quantity = 3
      )

      then("JSON으로 올바르게 직렬화되어야 한다") {
        val json = objectMapper.writeValueAsString(item)
        json shouldContain "테스트 상품"
      }

      then("JSON에서 올바르게 역직렬화되어야 한다") {
        val json = objectMapper.writeValueAsString(item)
        val deserialized = objectMapper.readValue(json, OrderItemData::class.java)
        deserialized.productId shouldBe "1"
        deserialized.productName shouldBe "테스트 상품"
        deserialized.quantity shouldBe 3
        deserialized.price shouldBe BigDecimal("15000.00")
      }
    }

    `when`("여러 인스턴스를 생성하면") {
      val item1 = OrderItemData("1", "상품A", BigDecimal("1000"), 1)
      val item2 = OrderItemData("2", "상품B", BigDecimal("2000"), 2)

      then("각각 다른 값을 가져야 한다") {
        item1.productId shouldNotBe item2.productId
        item1.productName shouldNotBe item2.productName
      }
    }
  }

  given("이벤트 동등성 및 복사가 주어졌을 때") {

    `when`("PaymentCreatedEvent를 복사하면") {
      val event1 = PaymentCreatedEvent(
        paymentId = 1L,
        orderId = "ORDER-001",
        userId = "USER-001",
        amount = BigDecimal("10000"),
        paymentMethod = PaymentMethod.CARD
      )

      then("동등성을 지원해야 한다") {
        val event2 = event1.copy()
        event1 shouldBe event2
      }
    }

    `when`("PaymentCreatedEvent를 수정하며 복사하면") {
      val event1 = PaymentCreatedEvent(
        paymentId = 1L,
        orderId = "ORDER-001",
        userId = "USER-001",
        amount = BigDecimal("10000"),
        paymentMethod = PaymentMethod.CARD
      )

      then("수정된 값만 변경되어야 한다") {
        val event2 = event1.copy(paymentId = 2L)
        event2.paymentId shouldBe 2L
        event2.orderId shouldBe "ORDER-001"
      }
    }
  }
})
