package com.ecommerce.user.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "auth.refresh-token")
data class RefreshTokenProperties(
    val expiration: Long,
    val cookieName: String
)
