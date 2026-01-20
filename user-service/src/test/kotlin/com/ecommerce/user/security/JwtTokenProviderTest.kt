package com.ecommerce.user.security

import com.ecommerce.user.config.JwtProperties
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import java.time.Duration
import java.util.Base64

class JwtTokenProviderTest : BehaviorSpec({

    val secretKey = "super-secret-key-must-be-very-long-to-be-secure-enough-for-hs256"
    val encodedSecret = Base64.getEncoder().encodeToString(secretKey.toByteArray())
    val jwtProperties = JwtProperties(secret = encodedSecret, expiration = Duration.ofHours(1))
    
    val tokenProvider = JwtTokenProvider(jwtProperties)

    Given("JwtTokenProvider가 초기화되었을 때") {
        val userId = 100L
        val email = "test@example.com"
        val role = "USER"

        When("사용자 정보로 토큰을 생성하면") {
            val token = tokenProvider.createAccessToken(userId, email, role)

            Then("토큰은 비어있지 않아야 한다") {
                token.shouldNotBeBlank()
            }

            Then("토큰에서 사용자 ID를 추출할 수 있어야 한다") {
                tokenProvider.validateToken(token) shouldBe true
            }
        }
    }
})
