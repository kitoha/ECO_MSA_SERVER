package com.ecommerce.gateway.filter

import com.ecommerce.gateway.security.AuthConstants
import com.ecommerce.gateway.util.JwtUtils
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AuthorizationHeaderFilter(
    private val jwtUtils: JwtUtils
) : AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config>(Config::class.java) {

    private val logger = LoggerFactory.getLogger(AuthorizationHeaderFilter::class.java)

    class Config

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            var token: String? = null

            if (request.headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                val authorizationHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
                if (authorizationHeader != null && authorizationHeader.startsWith(AuthConstants.BEARER_PREFIX)) {
                    token = authorizationHeader.replace(AuthConstants.BEARER_PREFIX, "")
                }
            }

            if (token == null && request.cookies.containsKey(AuthConstants.ACCESS_TOKEN_COOKIE_NAME)) {
                token = request.cookies.getFirst(AuthConstants.ACCESS_TOKEN_COOKIE_NAME)?.value
            }

            if (token == null || !jwtUtils.validateToken(token)) {
                return@GatewayFilter onError(exchange, "No valid token found in Header or Cookie", HttpStatus.UNAUTHORIZED)
            }

            val userId = jwtUtils.getUserId(token)

            val modifiedRequest = exchange.request.mutate()
                .header(AuthConstants.USER_ID_HEADER, userId)
                .build()

            chain.filter(exchange.mutate().request(modifiedRequest).build())
        }
    }

    private fun onError(exchange: ServerWebExchange, err: String, httpStatus: HttpStatus): Mono<Void> {
        logger.error("Authorization error: $err")
        val response = exchange.response
        response.statusCode = httpStatus
        return response.setComplete()
    }
}
