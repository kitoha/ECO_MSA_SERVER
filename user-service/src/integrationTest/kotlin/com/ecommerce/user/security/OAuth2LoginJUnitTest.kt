package com.ecommerce.user.security

import com.ecommerce.user.config.JwtProperties
import com.ecommerce.user.config.RefreshTokenProperties
import com.ecommerce.user.domain.User
import com.ecommerce.user.domain.UserRole
import com.ecommerce.user.repository.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties(JwtProperties::class, RefreshTokenProperties::class)
class OAuth2LoginTest(
    private val userRepository: UserRepository,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val refreshTokenProperties: RefreshTokenProperties
) : BehaviorSpec({

    Given("구글 OAuth2 로그인 사용자 정보가 주어졌을 때") {
        val email = "kotest-user@gmail.com"
        val name = "Kotest User"
        val providerId = "google-kotest-123"

        userRepository.save(
            User(
                email = email,
                name = name,
                provider = "google",
                providerId = providerId,
                role = UserRole.USER
            )
        )

        val attributes = mapOf(
            "email" to email,
            "name" to name,
            "sub" to providerId
        )
        val oAuth2User: OAuth2User = DefaultOAuth2User(
            emptyList(),
            attributes,
            "email"
        )

        val authentication = mock(Authentication::class.java)
        `when`(authentication.principal).thenReturn(oAuth2User)

        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        When("OAuth2 로그인이 성공하면") {
            oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication)

            Then("리다이렉트 URL이 올바르게 설정되어야 한다") {
                response.redirectedUrl.shouldNotBeNull()
                response.redirectedUrl!! shouldContain "/oauth2/redirect"
            }

            Then("accessToken 쿠키가 생성되어야 한다") {
                val accessTokenCookie = response.cookies.find { it.name == "accessToken" }
                accessTokenCookie.shouldNotBeNull()
                accessTokenCookie.isHttpOnly shouldBe true
            }

            Then("refresh_token 쿠키가 생성되어야 한다") {
                val refreshTokenCookie = response.cookies.find { it.name == refreshTokenProperties.cookieName }
                refreshTokenCookie.shouldNotBeNull()
                refreshTokenCookie.isHttpOnly shouldBe true
            }

            Then("사용자가 DB에 저장되어 있어야 한다") {
                val foundUser = userRepository.findByEmail(email)
                foundUser.shouldNotBeNull()
                foundUser.name shouldBe name
                foundUser.provider shouldBe "google"
            }
        }
    }
})
