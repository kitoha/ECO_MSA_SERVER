package com.ecommerce.gateway.util

import com.ecommerce.gateway.config.JwtProperties
import io.kotest.core.spec.style.StringSpec
import java.util.Base64

class JwtTokenGeneratorTest : StringSpec({
    /*로컬 테스트 때 사용할 수 있는 토큰 생성*/
    val secretKey = "super-secret-key-must-be-very-long-to-be-secure-enough-for-hs256"
    val encodedSecret = Base64.getEncoder().encodeToString(secretKey.toByteArray())
    
    val jwtProperties = JwtProperties(
        secret = encodedSecret,
        accessTokenExpiration = java.time.Duration.ofHours(24),
        refreshTokenExpiration = java.time.Duration.ofHours(24)
    )
    val jwtUtils = JwtUtils(jwtProperties)

    "Generate Test Token" {
        val userId = "test-user-1"
        val token = jwtUtils.generateToken(userId)

        println("\n===========================================")
        println("Generated JWT Token for userId: $userId")
        println("-------------------------------------------")
        println(token)
        println("===========================================\n")
    }
})
