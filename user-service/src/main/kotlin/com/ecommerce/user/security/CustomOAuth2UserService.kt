package com.ecommerce.user.security

import com.ecommerce.user.domain.User
import com.ecommerce.user.domain.UserRole
import com.ecommerce.user.repository.UserRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository
) : DefaultOAuth2UserService() {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val registrationId = userRequest.clientRegistration.registrationId
        
        val attributes = oAuth2User.attributes
        val email = attributes["email"] as String
        val name = attributes["name"] as String
        val providerId = attributes["sub"] as String

        val user = userRepository.findByEmail(email)
            ?.apply { updateProfile(name) }
            ?: userRepository.save(
                User(
                    email = email,
                    name = name,
                    provider = registrationId,
                    providerId = providerId,
                    role = UserRole.USER
                )
            )

        return oAuth2User
    }
}
