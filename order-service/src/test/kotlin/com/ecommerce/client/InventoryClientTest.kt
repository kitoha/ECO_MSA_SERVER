package com.ecommerce.client

import com.ecommerce.dto.external.InventoryResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.util.function.Function

class InventoryClientTest : BehaviorSpec({

    val webClientBuilder = mockk<WebClient.Builder>()
    val webClient = mockk<WebClient>()
    val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
    val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
    val responseSpec = mockk<WebClient.ResponseSpec>()

    val inventoryClient = InventoryClient(webClientBuilder)

    beforeEach {
        every { webClientBuilder.baseUrl(any()) } returns webClientBuilder
        every { webClientBuilder.build() } returns webClient
    }

    given("InventoryClient의 getInventoryByProductId 메서드가 주어졌을 때") {
        val productId = "0C6JNH3N3B8G0"
        val mockInventory = InventoryResponse(
            id = 1L,
            productId = productId,
            availableQuantity = 100,
            reservedQuantity = 10,
            totalQuantity = 110
        )

        `when`("존재하는 상품의 재고를 조회하면") {
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri("/api/v1/inventory/product/{productId}", productId) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(InventoryResponse::class.java) } returns Mono.just(mockInventory)

            then("재고 정보를 반환해야 한다") {
                val result = inventoryClient.getInventoryByProductId(productId)

                result shouldBe mockInventory
                result?.availableQuantity shouldBe 100
                result?.reservedQuantity shouldBe 10
                result?.totalQuantity shouldBe 110
            }
        }

        `when`("존재하지 않는 상품의 재고를 조회하면") {
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri("/api/v1/inventory/product/{productId}", "INVALID") } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(InventoryResponse::class.java) } returns Mono.error(
                WebClientResponseException.NotFound.create(404, "Not Found", null, null, null)
            )

            then("null을 반환해야 한다") {
                val result = inventoryClient.getInventoryByProductId("INVALID")

                result shouldBe null
            }
        }

        `when`("조회 중 예외가 발생하면") {
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri("/api/v1/inventory/product/{productId}", productId) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(InventoryResponse::class.java) } returns Mono.error(
                RuntimeException("Connection error")
            )

            then("InventoryClientException이 발생해야 한다") {
                shouldThrow<InventoryClientException> {
                    inventoryClient.getInventoryByProductId(productId)
                }
            }
        }
    }

    given("InventoryClient의 getInventoriesByProductIds 메서드가 주어졌을 때") {
        val productIds = listOf("0C6JNH3N3B8G0", "0C6JNH3N3B8G1")
        val mockInventories = listOf(
            InventoryResponse(
                id = 1L,
                productId = "0C6JNH3N3B8G0",
                availableQuantity = 100,
                reservedQuantity = 10,
                totalQuantity = 110
            ),
            InventoryResponse(
                id = 2L,
                productId = "0C6JNH3N3B8G1",
                availableQuantity = 50,
                reservedQuantity = 5,
                totalQuantity = 55
            )
        )

        `when`("여러 상품의 재고를 조회하면") {
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri(any<Function<UriBuilder, URI>>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToFlux(InventoryResponse::class.java) } returns Flux.fromIterable(mockInventories)

            then("재고 목록을 반환해야 한다") {
                val result = inventoryClient.getInventoriesByProductIds(productIds)

                result shouldHaveSize 2
                result[0].productId shouldBe "0C6JNH3N3B8G0"
                result[0].availableQuantity shouldBe 100
                result[1].productId shouldBe "0C6JNH3N3B8G1"
                result[1].availableQuantity shouldBe 50
            }
        }

        `when`("조회 중 예외가 발생하면") {
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri(any<Function<UriBuilder, URI>>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToFlux(InventoryResponse::class.java) } returns Flux.error(
                RuntimeException("Connection error")
            )

            then("InventoryClientException이 발생해야 한다") {
                shouldThrow<InventoryClientException> {
                    inventoryClient.getInventoriesByProductIds(productIds)
                }
            }
        }
    }

    given("InventoryClient의 hasEnoughStock 메서드가 주어졌을 때") {
        val productId = "0C6JNH3N3B8G0"

        `when`("재고가 충분하면") {
            val inventory = InventoryResponse(
                id = 1L,
                productId = productId,
                availableQuantity = 100,
                reservedQuantity = 10,
                totalQuantity = 110
            )

            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri("/api/v1/inventory/product/{productId}", productId) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(InventoryResponse::class.java) } returns Mono.just(inventory)

            then("true를 반환해야 한다") {
                val result = inventoryClient.hasEnoughStock(productId, 50)

                result shouldBe true
            }
        }

        `when`("재고가 부족하면") {
            val inventory = InventoryResponse(
                id = 1L,
                productId = productId,
                availableQuantity = 10,
                reservedQuantity = 10,
                totalQuantity = 20
            )

            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri("/api/v1/inventory/product/{productId}", productId) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(InventoryResponse::class.java) } returns Mono.just(inventory)

            then("false를 반환해야 한다") {
                val result = inventoryClient.hasEnoughStock(productId, 50)

                result shouldBe false
            }
        }

        `when`("재고 정보가 없으면") {
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri("/api/v1/inventory/product/{productId}", productId) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(InventoryResponse::class.java) } returns Mono.error(
                WebClientResponseException.NotFound.create(404, "Not Found", null, null, null)
            )

            then("false를 반환해야 한다") {
                val result = inventoryClient.hasEnoughStock(productId, 50)

                result shouldBe false
            }
        }
    }

    given("InventoryClient의 getAvailableQuantity 메서드가 주어졌을 때") {
        val productId = "0C6JNH3N3B8G0"

        `when`("재고 정보가 있으면") {
            val inventory = InventoryResponse(
                id = 1L,
                productId = productId,
                availableQuantity = 100,
                reservedQuantity = 10,
                totalQuantity = 110
            )

            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri("/api/v1/inventory/product/{productId}", productId) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(InventoryResponse::class.java) } returns Mono.just(inventory)

            then("가용 수량을 반환해야 한다") {
                val result = inventoryClient.getAvailableQuantity(productId)

                result shouldBe 100
            }
        }

        `when`("재고 정보가 없으면") {
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri("/api/v1/inventory/product/{productId}", productId) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(InventoryResponse::class.java) } returns Mono.error(
                WebClientResponseException.NotFound.create(404, "Not Found", null, null, null)
            )

            then("0을 반환해야 한다") {
                val result = inventoryClient.getAvailableQuantity(productId)

                result shouldBe 0
            }
        }
    }

    given("InventoryClient의 validateStockForItems 메서드가 주어졌을 때") {
        val items = mapOf(
            "0C6JNH3N3B8G0" to 50,
            "0C6JNH3N3B8G1" to 30
        )

        `when`("모든 상품의 재고가 충분하면") {
            val inventories = listOf(
                InventoryResponse(
                    id = 1L,
                    productId = "0C6JNH3N3B8G0",
                    availableQuantity = 100,
                    reservedQuantity = 10,
                    totalQuantity = 110
                ),
                InventoryResponse(
                    id = 2L,
                    productId = "0C6JNH3N3B8G1",
                    availableQuantity = 50,
                    reservedQuantity = 5,
                    totalQuantity = 55
                )
            )

            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri(any<Function<UriBuilder, URI>>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToFlux(InventoryResponse::class.java) } returns Flux.fromIterable(inventories)

            then("true를 반환해야 한다") {
                val result = inventoryClient.validateStockForItems(items)

                result shouldBe true
            }
        }

        `when`("일부 상품의 재고가 부족하면") {
            val inventories = listOf(
                InventoryResponse(
                    id = 1L,
                    productId = "0C6JNH3N3B8G0",
                    availableQuantity = 100,
                    reservedQuantity = 10,
                    totalQuantity = 110
                ),
                InventoryResponse(
                    id = 2L,
                    productId = "0C6JNH3N3B8G1",
                    availableQuantity = 20,
                    reservedQuantity = 5,
                    totalQuantity = 25
                )
            )

            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri(any<Function<UriBuilder, URI>>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToFlux(InventoryResponse::class.java) } returns Flux.fromIterable(inventories)

            then("false를 반환해야 한다") {
                val result = inventoryClient.validateStockForItems(items)

                result shouldBe false
            }
        }

        `when`("조회 중 예외가 발생하면") {
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri(any<Function<UriBuilder, URI>>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToFlux(InventoryResponse::class.java) } returns Flux.error(
                RuntimeException("Connection error")
            )

            then("false를 반환해야 한다") {
                val result = inventoryClient.validateStockForItems(items)

                result shouldBe false
            }
        }
    }
})
