package com.ecommerce.user.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class UserEntityTest : StringSpec({

    "User 엔티티 생성 및 정보 확인" {
        val user = User(
            email = "test@example.com",
            name = "Test User",
            provider = "google",
            providerId = "1234567890",
            role = UserRole.USER
        )

        user.email shouldBe "test@example.com"
        user.name shouldBe "Test User"
        user.provider shouldBe "google"
        user.role shouldBe UserRole.USER
    }

    "RefreshToken 엔티티 생성 및 만료 확인" {
        val now = LocalDateTime.now()
        val userId = 1L
        val tokenValue = "random-uuid-token-value"
        
        val refreshToken = RefreshToken(
            userId = userId,
            tokenValue = tokenValue,
            issuedAt = now,
            expiresAt = now.plusDays(14),
            rotationGroup = "group-1"
        )

        refreshToken.userId shouldBe userId
        refreshToken.tokenValue shouldBe tokenValue
        refreshToken.isExpired(now.plusDays(1)) shouldBe false
        refreshToken.isExpired(now.plusDays(15)) shouldBe true
    }
})
