package com.ecommerce.client

import com.ecommerce.dto.external.ProductResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

/**
 * Product Service와 통신하는 Client
 *
 * ProductId는 String으로 처리하여 마이크로서비스 간 결합도 감소
 * (Product Service 내부에서는 Long이지만, 다른 서비스에서는 String으로 추상화)
 *
 * **Batch API 제한사항:**
 * - 최대 100개까지 한번에 조회 가능
 * - 100개 이상 필요시 chunking 메서드 사용 권장
 */
@Component
class ProductClient(
    private val webClientBuilder: WebClient.Builder
) {
    private val logger = LoggerFactory.getLogger(ProductClient::class.java)

    companion object {
        /**
         * Product Service의 Batch API 최대 허용 개수
         */
        const val MAX_BATCH_SIZE = 100
    }

    private val webClient: WebClient by lazy {
        webClientBuilder
            .baseUrl("http://product-service")
            .build()
    }

    /**
     * 상품 ID로 단일 상품 조회
     *
     * @param productId 상품 ID (String 또는 Long을 String으로 변환)
     * @return ProductResponse 또는 null (상품이 없는 경우)
     */
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

    /**
     * 여러 상품을 한번에 조회 (Batch)
     *
     * **제한사항:** 최대 100개까지 조회 가능
     * 100개 이상 필요시 getProductsByIdsChunked() 사용 권장
     *
     * @param productIds 상품 ID 리스트 (최대 100개)
     * @return List<ProductResponse>
     * @throws ProductClientException productIds가 100개를 초과하거나 조회 실패시
     */
    fun getProductsByIds(productIds: List<String>): List<ProductResponse> {
        return try {
            // 최대 개수 검증
            require(productIds.size <= MAX_BATCH_SIZE) {
                "한번에 최대 ${MAX_BATCH_SIZE}개까지 조회 가능합니다. 요청: ${productIds.size}개. " +
                "getProductsByIdsChunked()를 사용하세요."
            }

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

    /**
     * 대량의 상품을 조회 (자동 Chunking)
     *
     * 100개씩 나눠서 여러 번 요청하므로 개수 제한 없음
     * 단, 너무 많은 요청은 성능 저하 가능
     *
     * @param productIds 상품 ID 리스트 (개수 제한 없음)
     * @return List<ProductResponse>
     */
    fun getProductsByIdsChunked(productIds: List<String>): List<ProductResponse> {
        if (productIds.isEmpty()) return emptyList()

        return productIds.chunked(MAX_BATCH_SIZE).flatMap { chunk ->
            getProductsByIds(chunk)
        }
    }

    /**
     * 상품 판매 가능 여부 확인
     *
     * @param productId 상품 ID
     * @return true if available, false otherwise
     */
    fun isProductAvailable(productId: String): Boolean {
        return try {
            val product = getProductById(productId)
            product?.status == "ACTIVE"
        } catch (e: Exception) {
            logger.error("Failed to check product availability: $productId", e)
            false
        }
    }

    /**
     * 상품 검색
     *
     * @param keyword 검색 키워드
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return List<ProductResponse>
     */
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

/**
 * ProductClient 전용 예외
 */
class ProductClientException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
