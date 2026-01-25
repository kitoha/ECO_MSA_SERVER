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
import org.springframework.beans.factory.annotation.Value

import com.ecommerce.user.config.JwtProperties
import com.ecommerce.user.util.CookieUtils

@Component
class OAuth2SuccessHandler(
    private val tokenProvider: JwtTokenProvider,
    private val refreshTokenService: RefreshTokenService,
    private val userRepository: UserRepository,
    private val refreshTokenProperties: RefreshTokenProperties,
    private val jwtProperties: JwtProperties,
    private val cookieUtils: CookieUtils,
    @Value("\${app.frontend-url}") private val frontendUrl: String
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

        cookieUtils.addCookie(
            response,
            AuthConstants.ACCESS_TOKEN_COOKIE_NAME,
            accessToken,
            jwtProperties.accessTokenExpiration
        )

        cookieUtils.addCookie(
            response,
            refreshTokenProperties.cookieName,
            refreshToken.tokenValue,
            refreshTokenProperties.expiration
        )

        val targetUrl = UriComponentsBuilder.fromUriString("$frontendUrl/oauth2/redirect")
            .build().toUriString()

        logger.info("Social login success for user: ${user.email}, redirecting to targetUrl (Tokens in Cookies)")
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
