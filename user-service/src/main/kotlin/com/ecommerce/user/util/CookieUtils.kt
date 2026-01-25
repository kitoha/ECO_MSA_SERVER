package com.ecommerce.user.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class CookieUtils {

    fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Duration) {
        val cookie = Cookie(name, value)
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.secure = false
        cookie.maxAge = maxAge.toSeconds().toInt()
        cookie.setAttribute("SameSite", "Lax")

        response.addCookie(cookie)
    }

    fun deleteCookie(response: HttpServletResponse, name: String) {
        val cookie = Cookie(name, "")
        cookie.path = "/"
        cookie.maxAge = 0
        response.addCookie(cookie)
    }
}
