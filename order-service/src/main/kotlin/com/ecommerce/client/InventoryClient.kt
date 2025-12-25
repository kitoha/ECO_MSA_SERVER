package com.ecommerce.client

import com.ecommerce.dto.external.InventoryResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

/**
 * 재고 조회 및 검증 관련 기능 제공
 */
@Component
class InventoryClient(
    private val webClientBuilder: WebClient.Builder
) {
    private val logger = LoggerFactory.getLogger(InventoryClient::class.java)

    private val webClient: WebClient by lazy {
        webClientBuilder
            .baseUrl("http://inventory-service")
            .build()
    }

    /**
     * 특정 상품의 재고 조회
     *
     * @param productId 상품 ID
     * @return InventoryResponse 또는 null
     */
    fun getInventoryByProductId(productId: String): InventoryResponse? {
        return try {
            logger.debug("Fetching inventory for product: {}", productId)

            webClient.get()
                .uri("/api/v1/inventory/product/{productId}", productId)
                .retrieve()
                .bodyToMono(InventoryResponse::class.java)
                .onErrorResume(WebClientResponseException.NotFound::class.java) {
                    logger.warn("Inventory not found for product: {}", productId)
                    Mono.empty()
                }
                .doOnError { error ->
                    logger.error("Error fetching inventory for product {}: {}", productId, error.message)
                }
                .block()

        } catch (e: Exception) {
            logger.error("Failed to fetch inventory for product: $productId", e)
            throw InventoryClientException("재고 조회 실패: ${e.message}", e)
        }
    }

    /**
     * 여러 상품의 재고를 한번에 조회
     *
     * @param productIds 상품 ID 리스트
     * @return List<InventoryResponse>
     */
    fun getInventoriesByProductIds(productIds: List<String>): List<InventoryResponse> {
        return try {
            logger.debug("Fetching inventories in batch: {}", productIds)

            webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/api/v1/inventory/batch")
                        .queryParam("productIds", productIds.joinToString(","))
                        .build()
                }
                .retrieve()
                .bodyToFlux(InventoryResponse::class.java)
                .collectList()
                .doOnError { error ->
                    logger.error("Error fetching inventories in batch: {}", error.message)
                }
                .block() ?: emptyList()

        } catch (e: Exception) {
            logger.error("Failed to fetch inventories in batch", e)
            throw InventoryClientException("재고 일괄 조회 실패: ${e.message}", e)
        }
    }

    /**
     * 재고 충분 여부 확인
     *
     * @param productId 상품 ID
     * @param requiredQuantity 필요한 수량
     * @return true if sufficient, false otherwise
     */
    fun hasEnoughStock(productId: String, requiredQuantity: Int): Boolean {
        return try {
            val inventory = getInventoryByProductId(productId)
            inventory?.let { it.availableQuantity >= requiredQuantity } ?: false
        } catch (e: Exception) {
            logger.error("Failed to check stock for product: $productId", e)
            false
        }
    }

    /**
     * 재고 가용 수량 조회
     *
     * @param productId 상품 ID
     * @return 가용 수량 (조회 실패시 0)
     */
    fun getAvailableQuantity(productId: String): Int {
        return try {
            val inventory = getInventoryByProductId(productId)
            inventory?.availableQuantity ?: 0
        } catch (e: Exception) {
            logger.error("Failed to get available quantity for product: $productId", e)
            0
        }
    }

    /**
     * 여러 상품의 재고 충분 여부를 한번에 검증
     *
     * @param items Map<ProductId, RequiredQuantity>
     * @return true if all items have sufficient stock
     */
    fun validateStockForItems(items: Map<String, Int>): Boolean {
        return try {
            val productIds = items.keys.toList()
            val inventories = getInventoriesByProductIds(productIds)

            val inventoryMap = inventories.associateBy { it.productId }

            items.all { (productId, requiredQuantity) ->
                val inventory = inventoryMap[productId]
                inventory != null && inventory.availableQuantity >= requiredQuantity
            }
        } catch (e: Exception) {
            logger.error("Failed to validate stock for items", e)
            false
        }
    }
}

/**
 * InventoryClient 전용 예외
 */
class InventoryClientException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
