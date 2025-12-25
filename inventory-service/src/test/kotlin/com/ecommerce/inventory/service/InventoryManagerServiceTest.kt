package com.ecommerce.inventory.service

import com.ecommerce.inventory.entity.Inventory
import com.ecommerce.inventory.enums.InventoryChangeType
import com.ecommerce.inventory.repository.Inventory.InventoryRepository
import io.kotest.assertions.throwables.shouldThrow
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

class InventoryManagerServiceTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val inventoryRepository = mockk<InventoryRepository>()
    val inventoryHistoryService = mockk<InventoryHistoryService>()
    val inventoryManagerService = InventoryManagerService(inventoryRepository, inventoryHistoryService)

    beforeEach {
        clearMocks(inventoryRepository, inventoryHistoryService, answers = false)
    }

    given("InventoryManagerService의 getInventoryByProductId 메서드가 주어졌을 때") {
        val inventory = Inventory(
            id = 1L,
            productId = "PRODUCT-001",
            availableQuantity = 100,
            reservedQuantity = 0,
            totalQuantity = 100
        )

        `when`("존재하는 상품 ID로 조회하면") {
            every { inventoryRepository.findByProductId("PRODUCT-001") } returns inventory

            then("재고 정보가 반환되어야 한다") {
                val result = inventoryManagerService.getInventoryByProductId("PRODUCT-001")

                result.productId shouldBe "PRODUCT-001"
                result.availableQuantity shouldBe 100
                result.totalQuantity shouldBe 100

                verify(exactly = 1) { inventoryRepository.findByProductId("PRODUCT-001") }
            }
        }

        `when`("존재하지 않는 상품 ID로 조회하면") {
            every { inventoryRepository.findByProductId("INVALID-PRODUCT") } returns null

            then("예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    inventoryManagerService.getInventoryByProductId("INVALID-PRODUCT")
                }
                exception.message shouldBe "Product not found: INVALID-PRODUCT"
            }
        }
    }

    given("InventoryManagerService의 getInventoriesByProductIds 메서드가 주어졌을 때") {
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
                reservedQuantity = 0,
                totalQuantity = 50
            )
        )

        `when`("여러 상품 ID로 조회하면") {
            every { inventoryRepository.findByProductIdIn(listOf("PRODUCT-001", "PRODUCT-002")) } returns inventories

            then("모든 재고 정보가 반환되어야 한다") {
                val result = inventoryManagerService.getInventoriesByProductIds(listOf("PRODUCT-001", "PRODUCT-002"))

                result shouldHaveSize 2
                result[0].productId shouldBe "PRODUCT-001"
                result[1].productId shouldBe "PRODUCT-002"

                verify(exactly = 1) { inventoryRepository.findByProductIdIn(listOf("PRODUCT-001", "PRODUCT-002")) }
            }
        }
    }

    given("InventoryManagerService의 adjustStock 메서드가 주어졌을 때") {
        val inventory = Inventory(
            id = 1L,
            productId = "PRODUCT-001",
            availableQuantity = 100,
            reservedQuantity = 0,
            totalQuantity = 100
        )

        `when`("재고를 증가시키면") {
            every { inventoryRepository.findByProductId("PRODUCT-001") } returns inventory
            every { inventoryRepository.save(any()) } just runs
            every {
                inventoryHistoryService.recordChange(
                    inventoryId = 1L,
                    changeType = InventoryChangeType.INCREASE,
                    quantity = 50,
                    beforeQuantity = 100,
                    afterQuantity = 150,
                    reason = "재고 입고",
                    referenceId = null
                )
            } just runs

            then("재고가 정상적으로 증가되어야 한다") {
                inventoryManagerService.adjustStock(
                    productId = "PRODUCT-001",
                    quantity = 50,
                    inventoryChangeType = InventoryChangeType.INCREASE,
                    reason = "재고 입고",
                    referenceId = null
                )

                inventory.availableQuantity shouldBe 150
                inventory.totalQuantity shouldBe 150

                verify(exactly = 1) { inventoryRepository.findByProductId("PRODUCT-001") }
                verify(exactly = 1) { inventoryRepository.save(any()) }
                verify(exactly = 1) {
                    inventoryHistoryService.recordChange(
                        inventoryId = 1L,
                        changeType = InventoryChangeType.INCREASE,
                        quantity = 50,
                        beforeQuantity = 100,
                        afterQuantity = 150,
                        reason = "재고 입고",
                        referenceId = null
                    )
                }
            }
        }

        `when`("재고를 감소시키면") {
            every { inventoryRepository.findByProductId("PRODUCT-001") } returns inventory
            every { inventoryRepository.save(any()) } just runs
            every {
                inventoryHistoryService.recordChange(
                    inventoryId = 1L,
                    changeType = InventoryChangeType.DECREASE,
                    quantity = 30,
                    beforeQuantity = 100,
                    afterQuantity = 70,
                    reason = "재고 출고",
                    referenceId = null
                )
            } just runs

            then("재고가 정상적으로 감소되어야 한다") {
                inventoryManagerService.adjustStock(
                    productId = "PRODUCT-001",
                    quantity = 30,
                    inventoryChangeType = InventoryChangeType.DECREASE,
                    reason = "재고 출고",
                    referenceId = null
                )

                inventory.availableQuantity shouldBe 70
                inventory.totalQuantity shouldBe 70

                verify(exactly = 1) { inventoryRepository.findByProductId("PRODUCT-001") }
                verify(exactly = 1) { inventoryRepository.save(any()) }
                verify(exactly = 1) {
                    inventoryHistoryService.recordChange(
                        inventoryId = 1L,
                        changeType = InventoryChangeType.DECREASE,
                        quantity = 30,
                        beforeQuantity = 100,
                        afterQuantity = 70,
                        reason = "재고 출고",
                        referenceId = null
                    )
                }
            }
        }

        `when`("잘못된 재고 변경 타입으로 조정하면") {
            every { inventoryRepository.findByProductId("PRODUCT-001") } returns inventory

            then("예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    inventoryManagerService.adjustStock(
                        productId = "PRODUCT-001",
                        quantity = 10,
                        inventoryChangeType = InventoryChangeType.RESERVE,
                        reason = "잘못된 타입",
                        referenceId = null
                    )
                }
                exception.message shouldBe "Invalid inventory change type: RESERVE"
            }
        }

        `when`("존재하지 않는 상품의 재고를 조정하면") {
            every { inventoryRepository.findByProductId("INVALID-PRODUCT") } returns null

            then("예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    inventoryManagerService.adjustStock(
                        productId = "INVALID-PRODUCT",
                        quantity = 10,
                        inventoryChangeType = InventoryChangeType.INCREASE,
                        reason = "재고 입고",
                        referenceId = null
                    )
                }
                exception.message shouldBe "Product not found: INVALID-PRODUCT"
            }
        }
    }

    given("InventoryManagerService의 reserveStock 메서드가 주어졌을 때") {
        val inventory = Inventory(
            id = 1L,
            productId = "PRODUCT-001",
            availableQuantity = 100,
            reservedQuantity = 0,
            totalQuantity = 100
        )

        `when`("재고를 예약하면") {
            every { inventoryRepository.findByProductId("PRODUCT-001") } returns inventory
            every { inventoryRepository.save(any()) } just runs
            every {
                inventoryHistoryService.recordChange(
                    inventoryId = 1L,
                    changeType = InventoryChangeType.RESERVE,
                    quantity = 30,
                    beforeQuantity = 100,
                    afterQuantity = 70,
                    reason = "주문 예약",
                    referenceId = "ORDER-001"
                )
            } just runs

            then("재고가 정상적으로 예약되어야 한다") {
                inventoryManagerService.reserveStock(
                    productId = "PRODUCT-001",
                    quantity = 30,
                    reason = "주문 예약",
                    referenceId = "ORDER-001"
                )

                inventory.availableQuantity shouldBe 70
                inventory.reservedQuantity shouldBe 30
                inventory.totalQuantity shouldBe 100

                verify(exactly = 1) { inventoryRepository.findByProductId("PRODUCT-001") }
                verify(exactly = 1) { inventoryRepository.save(any()) }
                verify(exactly = 1) {
                    inventoryHistoryService.recordChange(
                        inventoryId = 1L,
                        changeType = InventoryChangeType.RESERVE,
                        quantity = 30,
                        beforeQuantity = 100,
                        afterQuantity = 70,
                        reason = "주문 예약",
                        referenceId = "ORDER-001"
                    )
                }
            }
        }
    }

    given("InventoryManagerService의 releaseReservedStock 메서드가 주어졌을 때") {
        val inventory = Inventory(
            id = 1L,
            productId = "PRODUCT-001",
            availableQuantity = 70,
            reservedQuantity = 30,
            totalQuantity = 100
        )

        `when`("예약된 재고를 해제하면") {
            every { inventoryRepository.findByProductId("PRODUCT-001") } returns inventory
            every { inventoryRepository.save(any()) } just runs
            every {
                inventoryHistoryService.recordChange(
                    inventoryId = 1L,
                    changeType = InventoryChangeType.RELEASE,
                    quantity = 20,
                    beforeQuantity = 70,
                    afterQuantity = 90,
                    reason = "주문 취소",
                    referenceId = "ORDER-001"
                )
            } just runs

            then("예약이 정상적으로 해제되어야 한다") {
                inventoryManagerService.releaseReservedStock(
                    productId = "PRODUCT-001",
                    quantity = 20,
                    reason = "주문 취소",
                    referenceId = "ORDER-001"
                )

                inventory.availableQuantity shouldBe 90
                inventory.reservedQuantity shouldBe 10
                inventory.totalQuantity shouldBe 100

                verify(exactly = 1) { inventoryRepository.findByProductId("PRODUCT-001") }
                verify(exactly = 1) { inventoryRepository.save(any()) }
                verify(exactly = 1) {
                    inventoryHistoryService.recordChange(
                        inventoryId = 1L,
                        changeType = InventoryChangeType.RELEASE,
                        quantity = 20,
                        beforeQuantity = 70,
                        afterQuantity = 90,
                        reason = "주문 취소",
                        referenceId = "ORDER-001"
                    )
                }
            }
        }
    }
})
