package com.ecommerce.user.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "auth.refresh-token")
data class RefreshTokenProperties(
    val expiration: Duration,
    val cookieName: String
)