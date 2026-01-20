package com.ecommerce.user.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@ConfigurationProperties(prefix = "auth.refresh-token")
data class RefreshTokenProperties(
    val expiration: Duration,
    val cookieName: String
)