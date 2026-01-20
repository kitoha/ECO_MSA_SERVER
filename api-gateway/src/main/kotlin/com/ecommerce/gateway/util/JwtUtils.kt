package com.ecommerce.gateway.util

import com.ecommerce.gateway.config.JwtProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtils(
    private val jwtProperties: JwtProperties
) {

    private val logger = LoggerFactory.getLogger(JwtUtils::class.java)

    private val key: SecretKey by lazy {
        val keyBytes = Base64.getDecoder().decode(jwtProperties.secret)
        Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateToken(userId: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.expiration)

        return Jwts.builder()
            .subject(userId)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun getUserId(token: String): String {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        return claims.subject
    }

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            return true
        } catch (e: Exception) {
            logger.error("Invalid JWT token: ${e.message}")
            return false
        }
    }
}
