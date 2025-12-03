package com.ecommerce.gateway

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class GatewayRoutesConfigTest(
    private val routeLocator: RouteLocator
) : BehaviorSpec({
    extensions(SpringExtension)

    given("API Gateway 라우팅 설정이 주어졌을 때") {

        `when`("라우트를 조회하면") {
            val routes = routeLocator.routes.collectList().block()

            then("모든 서비스에 대한 라우트가 정의되어 있어야 한다") {
                routes shouldNotBe null
                routes!!.size shouldBe 5
            }

            then("Product Service 라우트가 올바르게 설정되어 있어야 한다") {
                val productRoute = routes!!.find { it.id == "product-service" }

                productRoute shouldNotBe null
                productRoute!!.uri.toString() shouldBe "lb://product-service"
                productRoute.predicate.toString() shouldContain "Paths: [/api/v1/products/**]"
            }

            then("Inventory Service 라우트가 올바르게 설정되어 있어야 한다") {
                val inventoryRoute = routes!!.find { it.id == "inventory-service" }

                inventoryRoute shouldNotBe null
                inventoryRoute!!.uri.toString() shouldBe "lb://inventory-service"
                inventoryRoute.predicate.toString() shouldContain "Paths: [/api/v1/inventory/**]"
            }

            then("Order Service 라우트가 올바르게 설정되어 있어야 한다") {
                val orderRoute = routes!!.find { it.id == "order-service" }

                orderRoute shouldNotBe null
                orderRoute!!.uri.toString() shouldBe "lb://order-service"
                orderRoute.predicate.toString() shouldContain "Paths: [/api/v1/orders/**]"
            }

            then("Cart Service 라우트가 올바르게 설정되어 있어야 한다") {
                val cartRoute = routes!!.find { it.id == "cart-service" }

                cartRoute shouldNotBe null
                cartRoute!!.uri.toString() shouldBe "lb://cart-service"
                cartRoute.predicate.toString() shouldContain "Paths: [/api/v1/carts/**]"
            }

            then("Payment Service 라우트가 올바르게 설정되어 있어야 한다") {
                val paymentRoute = routes!!.find { it.id == "payment-service" }

                paymentRoute shouldNotBe null
                paymentRoute!!.uri.toString() shouldBe "lb://payment-service"
                paymentRoute.predicate.toString() shouldContain "Paths: [/api/v1/payments/**]"
            }
        }

        `when`("Order Service 라우트의 필터를 확인하면") {
            val routes = routeLocator.routes.collectList().block()
            val orderRoute = routes?.find { it.id == "order-service" }

            then("RewritePath 필터가 적용되어 있어야 한다") {
                orderRoute shouldNotBe null
                orderRoute!!.filters.any {
                    it.toString().contains("RewritePath")
                } shouldBe true
            }
        }
    }
})
