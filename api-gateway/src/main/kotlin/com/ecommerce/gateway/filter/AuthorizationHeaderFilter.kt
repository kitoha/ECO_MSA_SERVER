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

            if (!request.headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                return@GatewayFilter onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED)
            }

            val authorizationHeader = request.headers.getOrEmpty(HttpHeaders.AUTHORIZATION)[0]
            val token = authorizationHeader.replace(AuthConstants.BEARER_PREFIX, "")

            if (!jwtUtils.validateToken(token)) {
                return@GatewayFilter onError(exchange, "JWT token is not valid", HttpStatus.UNAUTHORIZED)
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
