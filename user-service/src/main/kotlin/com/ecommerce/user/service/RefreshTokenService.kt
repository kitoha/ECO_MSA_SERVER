package com.ecommerce.user.service

import com.ecommerce.user.config.RefreshTokenProperties
import com.ecommerce.user.domain.RefreshToken
import com.ecommerce.user.repository.RefreshTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val properties: RefreshTokenProperties
) {

    private val logger = LoggerFactory.getLogger(RefreshTokenService::class.java)

    fun createRefreshToken(userId: Long): RefreshToken {
        logger.info("Creating new refresh token for user: $userId")
        
        val now = LocalDateTime.now()
        val tokenValue = UUID.randomUUID().toString()
        val rotationGroup = UUID.randomUUID().toString()

        val refreshToken = RefreshToken(
            userId = userId,
            tokenValue = tokenValue,
            issuedAt = now,
            expiresAt = now.plus(properties.expiration),
            rotationGroup = rotationGroup
        )

        return refreshTokenRepository.save(refreshToken)
    }

    fun rotateRefreshToken(oldTokenValue: String): RefreshToken {
        val oldToken = refreshTokenRepository.findByTokenValue(oldTokenValue)
            ?: throw IllegalArgumentException("Invalid refresh token")

        if (oldToken.used) {
            logger.error("Refresh token reuse detected for group: ${oldToken.rotationGroup}. Revoking all tokens.")
            refreshTokenRepository.deleteAllByRotationGroup(oldToken.rotationGroup)
            throw IllegalStateException("Token reuse detected! Revoking all tokens in group.")
        }

        if (oldToken.isExpired()) {
            logger.warn("Expired refresh token used: $oldTokenValue")
            refreshTokenRepository.delete(oldToken)
            throw IllegalArgumentException("Refresh token is expired")
        }

        logger.info("Rotating refresh token for group: ${oldToken.rotationGroup}")

        oldToken.markAsUsed()
        refreshTokenRepository.save(oldToken)

        val now = LocalDateTime.now()
        val newToken = RefreshToken(
            userId = oldToken.userId,
            tokenValue = UUID.randomUUID().toString(),
            issuedAt = now,
            expiresAt = now.plus(properties.expiration),
            rotationGroup = oldToken.rotationGroup
        )

        return refreshTokenRepository.save(newToken)
    }

    fun logout(userId: Long) {
        logger.info("Logging out user: $userId, deleting all refresh tokens.")
        refreshTokenRepository.deleteAllByUserId(userId)
    }
}
