package com.ecommerce.user.repository

import com.ecommerce.user.domain.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByTokenValue(tokenValue: String): RefreshToken?
    fun deleteAllByRotationGroup(rotationGroup: String)
    fun deleteAllByUserId(userId: Long)
}