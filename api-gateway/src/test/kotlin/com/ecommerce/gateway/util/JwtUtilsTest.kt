package com.ecommerce.gateway.util

import com.ecommerce.gateway.config.JwtProperties
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Base64
import java.util.Date

class JwtUtilsTest : BehaviorSpec({

    val secretKeyString = Base64.getEncoder().encodeToString("super-secret-key-must-be-very-long-to-be-secure-enough-for-hs256".toByteArray())
    val jwtProperties = JwtProperties(secret = secretKeyString, expiration = 3600000) // 1 hour
    val jwtUtils = JwtUtils(jwtProperties)

    Given("JwtUtils가 초기화되었을 때") {
        val userId = "user-1234"

        When("사용자 ID로 토큰을 생성하면") {
            val token = jwtUtils.generateToken(userId)

            Then("토큰은 비어있지 않아야 한다") {
                token.shouldNotBeBlank()
            }

            Then("생성된 토큰은 유효해야 한다") {
                jwtUtils.validateToken(token) shouldBe true
            }

            Then("토큰에서 사용자 ID를 추출하면 원래 ID와 같아야 한다") {
                val extractedUserId = jwtUtils.getUserId(token)
                extractedUserId shouldBe userId
            }
        }

        When("잘못된 토큰을 검증하면") {
            val invalidToken = "invalid.token.string"

            Then("유효하지 않다고 판단해야 한다") {
                jwtUtils.validateToken(invalidToken) shouldBe false
            }
        }
    }
})
