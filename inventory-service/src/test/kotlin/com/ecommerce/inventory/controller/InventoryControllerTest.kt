package com.ecommerce.inventory.controller

import com.ecommerce.inventory.entity.Inventory
import com.ecommerce.inventory.entity.InventoryHistory
import com.ecommerce.inventory.entity.InventoryReservation
import com.ecommerce.inventory.enums.InventoryChangeType
import com.ecommerce.inventory.enums.ReservationStatus
import com.ecommerce.inventory.request.AdjustStockRequest
import com.ecommerce.inventory.request.ReleaseInventoryRequest
import com.ecommerce.inventory.request.ReserveInventoryRequest
import com.ecommerce.inventory.service.InventoryHistoryService
import com.ecommerce.inventory.service.InventoryManagerService
import com.ecommerce.inventory.service.InventoryReservationService
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class InventoryControllerTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val inventoryManagerService = mockk<InventoryManagerService>()
    val inventoryReservationService = mockk<InventoryReservationService>()
    val inventoryHistoryService = mockk<InventoryHistoryService>()
    val inventoryController = InventoryController(
        inventoryManagerService,
        inventoryReservationService,
        inventoryHistoryService
    )

    beforeEach {
        clearMocks(inventoryManagerService, inventoryReservationService, inventoryHistoryService, answers = false)
    }

    given("InventoryController의 getProductInventoryById 엔드포인트가 주어졌을 때") {
        val inventory = Inventory(
            id = 1L,
            productId = "PRODUCT-001",
            availableQuantity = 100,
            reservedQuantity = 0,
            totalQuantity = 100
        )

        `when`("상품 ID로 재고를 조회하면") {
            every { inventoryManagerService.getInventoryByProductId("PRODUCT-001") } returns inventory

            then("재고 정보를 반환하고 200 OK 응답을 반환해야 한다") {
                val response = inventoryController.getProductInventoryById("PRODUCT-001")

                response.statusCode shouldBe HttpStatus.OK
                response.body?.productId shouldBe "PRODUCT-001"
                response.body?.availableQuantity shouldBe 100
                response.body?.reservedQuantity shouldBe 0
                response.body?.totalQuantity shouldBe 100

                verify(exactly = 1) { inventoryManagerService.getInventoryByProductId("PRODUCT-001") }
            }
        }
    }

    given("InventoryController의 getAllProductInventories 엔드포인트가 주어졌을 때") {
        val inventories = listOf(
            Inventory(
                id = 1L,
                productId = "PRODUCT-001",
                availableQuantity = 100,
                reservedQuantity = 0,
                totalQuantity = 100
            ),
            Inventory(
                id = 2L,
                productId = "PRODUCT-002",
                availableQuantity = 50,
                reservedQuantity = 10,
                totalQuantity = 60
            )
        )

        `when`("여러 상품 ID로 재고를 조회하면") {
            every {
                inventoryManagerService.getInventoriesByProductIds(listOf("PRODUCT-001", "PRODUCT-002"))
            } returns inventories

            then("재고 목록을 반환하고 200 OK 응답을 반환해야 한다") {
                val response = inventoryController.getAllProductInventories(listOf("PRODUCT-001", "PRODUCT-002"))

                response.statusCode shouldBe HttpStatus.OK
                response.body shouldHaveSize 2
                response.body?.get(0)?.productId shouldBe "PRODUCT-001"
                response.body?.get(1)?.productId shouldBe "PRODUCT-002"

                verify(exactly = 1) {
                    inventoryManagerService.getInventoriesByProductIds(listOf("PRODUCT-001", "PRODUCT-002"))
                }
            }
        }
    }

    given("InventoryController의 reserveInventory 엔드포인트가 주어졌을 때") {
        val request = ReserveInventoryRequest(
            orderId = "ORDER-001",
            productId = "PRODUCT-001",
            quantity = 30
        )

        val reservation = InventoryReservation(
            id = 1L,
            inventoryId = 1L,
            orderId = "ORDER-001",
            quantity = 30,
            status = ReservationStatus.ACTIVE,
            expiresAt = LocalDateTime.now().plusMinutes(15)
        )

        `when`("재고 예약을 요청하면") {
            every {
                inventoryReservationService.createReservation(
                    orderId = "ORDER-001",
                    productId = "PRODUCT-001",
                    quantity = 30
                )
            } returns reservation

            then("예약이 생성되고 201 CREATED 응답을 반환해야 한다") {
                val response = inventoryController.reserveInventory(request)

                response.statusCode shouldBe HttpStatus.CREATED
                response.body?.orderId shouldBe "ORDER-001"
                response.body?.quantity shouldBe 30
                response.body?.status shouldBe ReservationStatus.ACTIVE

                verify(exactly = 1) {
                    inventoryReservationService.createReservation(
                        orderId = "ORDER-001",
                        productId = "PRODUCT-001",
                        quantity = 30
                    )
                }
            }
        }
    }

    given("InventoryController의 releaseInventory 엔드포인트가 주어졌을 때") {
        val request = ReleaseInventoryRequest(
            reservationId = 1L
        )

        `when`("재고 예약 해제를 요청하면") {
            every { inventoryReservationService.cancelReservation(1L) } just runs

            then("204 NO CONTENT 응답을 반환해야 한다") {
                val response = inventoryController.releaseInventory(request)

                response.statusCode shouldBe HttpStatus.NO_CONTENT
                response.body shouldBe null

                verify(exactly = 1) { inventoryReservationService.cancelReservation(1L) }
            }
        }
    }

    given("InventoryController의 adjustInventory 엔드포인트가 주어졌을 때") {
        val request = AdjustStockRequest(
            quantity = 50,
            changeType = InventoryChangeType.INCREASE,
            reason = "재고 입고",
            referenceId = "INBOUND-001"
        )

        `when`("재고 조정을 요청하면") {
            every {
                inventoryManagerService.adjustStock(
                    productId = "PRODUCT-001",
                    quantity = 50,
                    inventoryChangeType = InventoryChangeType.INCREASE,
                    reason = "재고 입고",
                    referenceId = "INBOUND-001"
                )
            } just runs

            then("200 OK 응답을 반환해야 한다") {
                val response = inventoryController.adjustInventory("PRODUCT-001", request)

                response.statusCode shouldBe HttpStatus.OK

                verify(exactly = 1) {
                    inventoryManagerService.adjustStock(
                        productId = "PRODUCT-001",
                        quantity = 50,
                        inventoryChangeType = InventoryChangeType.INCREASE,
                        reason = "재고 입고",
                        referenceId = "INBOUND-001"
                    )
                }
            }
        }
    }

    given("InventoryController의 getInventoryHistory 엔드포인트가 주어졌을 때") {
        val inventory = Inventory(
            id = 1L,
            productId = "PRODUCT-001",
            availableQuantity = 100,
            reservedQuantity = 0,
            totalQuantity = 100
        )

        val histories = listOf(
            InventoryHistory(
                id = 1L,
                inventoryId = 1L,
                changeType = InventoryChangeType.INCREASE,
                quantity = 50,
                beforeQuantity = 50,
                afterQuantity = 100,
                reason = "재고 입고",
                referenceId = "INBOUND-001"
            ),
            InventoryHistory(
                id = 2L,
                inventoryId = 1L,
                changeType = InventoryChangeType.RESERVE,
                quantity = 30,
                beforeQuantity = 100,
                afterQuantity = 70,
                reason = "주문 예약",
                referenceId = "ORDER-001"
            )
        )

        `when`("재고 변동 이력을 조회하면") {
            every { inventoryManagerService.getInventoryByProductId("PRODUCT-001") } returns inventory
            every { inventoryHistoryService.getHistoryByInventoryId(1L) } returns histories

            then("이력 목록을 반환하고 200 OK 응답을 반환해야 한다") {
                val response = inventoryController.getInventoryHistory("PRODUCT-001")

                response.statusCode shouldBe HttpStatus.OK
                response.body shouldHaveSize 2
                response.body?.get(0)?.changeType shouldBe InventoryChangeType.INCREASE
                response.body?.get(1)?.changeType shouldBe InventoryChangeType.RESERVE

                verify(exactly = 1) { inventoryManagerService.getInventoryByProductId("PRODUCT-001") }
                verify(exactly = 1) { inventoryHistoryService.getHistoryByInventoryId(1L) }
            }
        }
    }
})
