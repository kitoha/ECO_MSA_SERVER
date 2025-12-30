package com.ecommerce.cart.client

import com.ecommerce.cart.dto.external.ProductResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Component
class ProductClient(
    private val webClientBuilder: WebClient.Builder
) {
    private val logger = LoggerFactory.getLogger(ProductClient::class.java)

    companion object {
        const val BASE_URL = "http://product-service"
    }

    private val webClient: WebClient by lazy {
        webClientBuilder
            .baseUrl(BASE_URL)
            .build()
    }

    fun getProductById(productId: String): ProductResponse? {
        return try {
            logger.debug("Fetching product: {}", productId)

            webClient.get()
                .uri("/api/v1/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductResponse::class.java)
                .onErrorResume(WebClientResponseException.NotFound::class.java) {
                    logger.warn("Product not found: {}", productId)
                    Mono.empty()
                }
                .doOnError { error ->
                    logger.error("Error fetching product {}: {}", productId, error.message)
                }
                .block()

        } catch (e: Exception) {
            logger.error("Failed to fetch product: $productId", e)
            throw ProductClientException("상품 조회 실패: ${e.message}", e)
        }
    }

    fun isProductAvailable(productId: String): Boolean {
        return try {
            val product = getProductById(productId)
            product?.status == "ACTIVE"
        } catch (e: Exception) {
            logger.error("Failed to check product availability: $productId", e)
            false
        }
    }
}