package com.ecommerce.user.controller

import com.ecommerce.user.config.RefreshTokenProperties
import com.ecommerce.user.security.AuthConstants
import com.ecommerce.user.util.CookieUtils
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val cookieUtils: CookieUtils,
    private val refreshTokenProperties: RefreshTokenProperties
) {

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<Void> {
        cookieUtils.deleteCookie(response, AuthConstants.ACCESS_TOKEN_COOKIE_NAME)

        cookieUtils.deleteCookie(response, refreshTokenProperties.cookieName)
        
        return ResponseEntity.ok().build()
    }
}
