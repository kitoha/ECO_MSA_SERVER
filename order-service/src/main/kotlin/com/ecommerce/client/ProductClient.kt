package com.ecommerce.client

import com.ecommerce.dto.external.ProductResponse
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
        const val MAX_BATCH_SIZE = 100
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

    fun getProductsByIds(productIds: List<String>): List<ProductResponse> {
        require(productIds.size <= MAX_BATCH_SIZE) {
            "한번에 최대 ${MAX_BATCH_SIZE}개까지 조회 가능합니다. 요청: ${productIds.size}개. " +
            "getProductsByIdsChunked()를 사용하세요."
        }

        return try {
            logger.debug("Fetching products in batch: {}", productIds)

            webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/api/v1/products/batch")
                        .queryParam("ids", productIds.joinToString(","))
                        .build()
                }
                .retrieve()
                .bodyToFlux(ProductResponse::class.java)
                .collectList()
                .doOnError { error ->
                    logger.error("Error fetching products in batch: {}", error.message)
                }
                .block() ?: emptyList()

        } catch (e: Exception) {
            logger.error("Failed to fetch products in batch", e)
            throw ProductClientException("상품 일괄 조회 실패: ${e.message}", e)
        }
    }

    fun getProductsByIdsChunked(productIds: List<String>): List<ProductResponse> {
        if (productIds.isEmpty()) return emptyList()

        return productIds.chunked(MAX_BATCH_SIZE).flatMap { chunk ->
            getProductsByIds(chunk)
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

    fun searchProducts(keyword: String, page: Int = 0, size: Int = 20): List<ProductResponse> {
        return try {
            logger.debug("Searching products with keyword: {}", keyword)

            webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/api/v1/products/search")
                        .queryParam("keyword", keyword)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build()
                }
                .retrieve()
                .bodyToFlux(ProductResponse::class.java)
                .collectList()
                .doOnError { error ->
                    logger.error("Error searching products: {}", error.message)
                }
                .block() ?: emptyList()

        } catch (e: Exception) {
            logger.error("Failed to search products with keyword: $keyword", e)
            throw ProductClientException("상품 검색 실패: ${e.message}", e)
        }
    }
}


class ProductClientException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
