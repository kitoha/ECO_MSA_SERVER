package com.ecommerce.user.security

import com.ecommerce.user.config.RefreshTokenProperties
import com.ecommerce.user.repository.UserRepository
import com.ecommerce.user.service.RefreshTokenService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2SuccessHandler(
    private val tokenProvider: JwtTokenProvider,
    private val refreshTokenService: RefreshTokenService,
    private val userRepository: UserRepository,
    private val refreshTokenProperties: RefreshTokenProperties
) : SimpleUrlAuthenticationSuccessHandler() {

    private val logger = LoggerFactory.getLogger(OAuth2SuccessHandler::class.java)

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as OAuth2User
        val email = oAuth2User.attributes["email"] as String

        val user = userRepository.findByEmail(email)
            ?: throw IllegalStateException("User not found after social login")

        val accessToken = tokenProvider.createAccessToken(user.id!!, user.email, user.role.name)

        val refreshToken = refreshTokenService.createRefreshToken(user.id)

        val cookie = Cookie(refreshTokenProperties.cookieName, refreshToken.tokenValue).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = refreshTokenProperties.expiration.toSeconds().toInt()
        }
        response.addCookie(cookie)

        val targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/redirect")
            .queryParam("accessToken", accessToken)
            .build().toUriString()

        logger.info("Social login success for user: ${user.email}, redirecting to targetUrl")
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
