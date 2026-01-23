package com.ecommerce.user.controller

import com.ecommerce.user.config.RefreshTokenProperties
import com.ecommerce.user.security.AuthConstants
import com.ecommerce.user.util.CookieUtils
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import com.ecommerce.user.repository.UserRepository
import com.ecommerce.user.security.JwtTokenProvider
import com.ecommerce.user.service.RefreshTokenService
import org.springframework.web.bind.annotation.CookieValue

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val cookieUtils: CookieUtils,
    private val refreshTokenProperties: RefreshTokenProperties,
    private val refreshTokenService: RefreshTokenService,
    private val tokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository,
    private val jwtProperties: com.ecommerce.user.config.JwtProperties
) {

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<Void> {
        // Access Token 쿠키 삭제
        cookieUtils.deleteCookie(response, AuthConstants.ACCESS_TOKEN_COOKIE_NAME)
        
        // Refresh Token 쿠키 삭제
        cookieUtils.deleteCookie(response, refreshTokenProperties.cookieName)
        
        return ResponseEntity.ok().build()
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(name = "refresh_token") oldRefreshToken: String,
        response: HttpServletResponse
    ): ResponseEntity<Void> {
        try {
            // 1. 리프레시 토큰 갱신 (Rotation)
            val rotatedToken = refreshTokenService.rotateRefreshToken(oldRefreshToken)
            
            // 2. 사용자 정보 조회
            val user = userRepository.findById(rotatedToken.userId)
                .orElseThrow { throw IllegalArgumentException("User not found") }

            // 3. 새 액세스 토큰 생성
            val newAccessToken = tokenProvider.createAccessToken(user.id!!, user.email, user.role.name)

            // 4. 쿠키 업데이트
            cookieUtils.addCookie(
                response, 
                AuthConstants.ACCESS_TOKEN_COOKIE_NAME, 
                newAccessToken, 
                jwtProperties.accessTokenExpiration
            )
            cookieUtils.addCookie(
                response, 
                refreshTokenProperties.cookieName, 
                rotatedToken.tokenValue, 
                refreshTokenProperties.expiration
            )

            return ResponseEntity.ok().build()
        } catch (e: Exception) {
            logout(response)
            return ResponseEntity.status(401).build()
        }
    }
}
