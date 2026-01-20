package com.ecommerce.user.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "refresh_tokens",)
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false, unique = true)
    val tokenValue: String,

    @Column(nullable = false)
    val issuedAt: LocalDateTime,

    @Column(nullable = false)
    val expiresAt: LocalDateTime,

    @Column(nullable = false)
    val rotationGroup: String,

    @Column(nullable = false)
    var used: Boolean = false

) : BaseEntity() {

    fun isExpired(now: LocalDateTime = LocalDateTime.now()): Boolean {
        return now.isAfter(expiresAt)
    }

    fun markAsUsed() {
        this.used = true
    }
}