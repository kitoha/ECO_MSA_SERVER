package com.ecommerce.gateway.filter

import com.ecommerce.gateway.util.JwtUtils
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class AuthorizationHeaderFilterTest : BehaviorSpec({

    val jwtUtils = mockk<JwtUtils>()
    val filter = AuthorizationHeaderFilter(jwtUtils)
    val chain = mockk<GatewayFilterChain>()

    val config = AuthorizationHeaderFilter.Config()

    Given("AuthorizationHeaderFilter가 있을 때") {

        every { chain.filter(any()) } returns Mono.empty()

        When("헤더에 토큰이 없으면") {
            val request = MockServerHttpRequest.get("/api/v1/orders").build()
            val exchange = MockServerWebExchange.from(request)

            Then("401 Unauthorized 에러를 반환해야 한다") {
                val result = filter.apply(config).filter(exchange, chain)
                
                StepVerifier.create(result)
                    .verifyComplete()
                
                exchange.response.statusCode shouldBe HttpStatus.UNAUTHORIZED
            }
        }

        When("유효하지 않은 토큰이면") {
            val token = "invalid-token"
            val request = MockServerHttpRequest.get("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtUtils.validateToken(token) } returns false

            Then("401 Unauthorized 에러를 반환해야 한다") {
                val result = filter.apply(config).filter(exchange, chain)
                
                StepVerifier.create(result)
                    .verifyComplete()
                
                exchange.response.statusCode shouldBe HttpStatus.UNAUTHORIZED
            }
        }

        When("유효한 토큰이면") {
            val token = "valid-token"
            val userId = "user-123"
            val request = MockServerHttpRequest.get("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()
            val exchange = MockServerWebExchange.from(request)

            every { jwtUtils.validateToken(token) } returns true
            every { jwtUtils.getUserId(token) } returns userId

            Then("요청이 통과되어야 한다") {
                val result = filter.apply(config).filter(exchange, chain)

                StepVerifier.create(result)
                    .verifyComplete()

                exchange.response.statusCode shouldBe null 
            }
        }
    }
})
