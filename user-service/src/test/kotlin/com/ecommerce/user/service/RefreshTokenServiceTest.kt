package com.ecommerce.user.service

import com.ecommerce.user.config.RefreshTokenProperties
import com.ecommerce.user.domain.RefreshToken
import com.ecommerce.user.repository.RefreshTokenRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import java.time.Duration
import java.time.LocalDateTime

class RefreshTokenServiceTest : DescribeSpec({

    val refreshTokenRepository = mockk<RefreshTokenRepository>()
    val expirationDuration = Duration.ofDays(14)
    val properties = RefreshTokenProperties(expiration = expirationDuration, cookieName = "refresh_token")
    val refreshTokenService = RefreshTokenService(refreshTokenRepository, properties)

    beforeEach {
        clearMocks(refreshTokenRepository)
    }

    describe("RefreshToken 생성") {
        it("성공적으로 토큰을 생성하고 DB에 저장한다") {
            every { refreshTokenRepository.save(any()) } answers { firstArg() }

            val result = refreshTokenService.createRefreshToken(1L)

            result.userId shouldBe 1L
            result.tokenValue.shouldNotBeBlank()
            verify(exactly = 1) { refreshTokenRepository.save(any()) }
        }
    }

    describe("RefreshToken 회전 (RTR)") {
        it("유효한 토큰 사용 시 기존 토큰을 USED로 만들고 새 토큰을 발급한다") {
            val oldToken = RefreshToken(
                userId = 1L,
                tokenValue = "old-token",
                issuedAt = LocalDateTime.now().minusDays(1),
                expiresAt = LocalDateTime.now().plusDays(1),
                rotationGroup = "group-1",
                used = false
            )

            every { refreshTokenRepository.findByTokenValue("old-token") } returns oldToken
            every { refreshTokenRepository.save(any()) } answers { firstArg() }

            val newToken = refreshTokenService.rotateRefreshToken("old-token")

            oldToken.used shouldBe true
            newToken.rotationGroup shouldBe "group-1"
            verify(exactly = 2) { refreshTokenRepository.save(any()) }
        }

        it("이미 사용된(USED) 토큰으로 재발급 시도 시 해당 그룹의 모든 토큰을 무효화하고 예외를 던진다") {
            val usedToken = RefreshToken(
                userId = 1L,
                tokenValue = "already-used-token",
                issuedAt = LocalDateTime.now().minusDays(2),
                expiresAt = LocalDateTime.now().plusDays(1),
                rotationGroup = "group-1",
                used = true
            )

            every { refreshTokenRepository.findByTokenValue("already-used-token") } returns usedToken
            every { refreshTokenRepository.deleteAllByRotationGroup("group-1") } just Runs

            val exception = shouldThrow<IllegalStateException> {
                refreshTokenService.rotateRefreshToken("already-used-token")
            }

            exception.message shouldBe "Token reuse detected! Revoking all tokens in group."
            verify(exactly = 1) { refreshTokenRepository.deleteAllByRotationGroup("group-1") }
        }
    }
})

private fun String?.shouldNotBeBlank() {
    this shouldNotBe null
    this!!.isBlank() shouldBe false
}
