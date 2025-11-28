package com.ecommerce.inventory.service

import com.ecommerce.inventory.entity.InventoryHistory
import com.ecommerce.inventory.enums.InventoryChangeType
import com.ecommerce.inventory.repository.InventoryHistory.InventoryHistoryRepository
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

class InventoryHistoryServiceTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val inventoryHistoryRepository = mockk<InventoryHistoryRepository>()
    val inventoryHistoryService = InventoryHistoryService(inventoryHistoryRepository)

    beforeEach {
        clearMocks(inventoryHistoryRepository, answers = false)
    }

    given("InventoryHistoryService의 recordChange 메서드가 주어졌을 때") {
        `when`("재고 변경 이력을 기록하면") {
            every { inventoryHistoryRepository.save(any()) } just runs

            then("이력이 정상적으로 저장되어야 한다") {
                inventoryHistoryService.recordChange(
                    inventoryId = 1L,
                    changeType = InventoryChangeType.INCREASE,
                    quantity = 50,
                    beforeQuantity = 100,
                    afterQuantity = 150,
                    reason = "재고 입고",
                    referenceId = "INBOUND-001"
                )

                verify(exactly = 1) {
                    inventoryHistoryRepository.save(
                        match { history ->
                            history.inventoryId == 1L &&
                            history.changeType == InventoryChangeType.INCREASE &&
                            history.quantity == 50 &&
                            history.beforeQuantity == 100 &&
                            history.afterQuantity == 150 &&
                            history.reason == "재고 입고" &&
                            history.referenceId == "INBOUND-001"
                        }
                    )
                }
            }
        }

        `when`("referenceId 없이 이력을 기록하면") {
            every { inventoryHistoryRepository.save(any()) } just runs

            then("referenceId가 null인 이력이 저장되어야 한다") {
                inventoryHistoryService.recordChange(
                    inventoryId = 1L,
                    changeType = InventoryChangeType.DECREASE,
                    quantity = 30,
                    beforeQuantity = 100,
                    afterQuantity = 70,
                    reason = "재고 출고",
                    referenceId = null
                )

                verify(exactly = 1) {
                    inventoryHistoryRepository.save(
                        match { history ->
                            history.inventoryId == 1L &&
                            history.changeType == InventoryChangeType.DECREASE &&
                            history.referenceId == null
                        }
                    )
                }
            }
        }

        `when`("재고 예약 이력을 기록하면") {
            every { inventoryHistoryRepository.save(any()) } just runs

            then("RESERVE 타입의 이력이 저장되어야 한다") {
                inventoryHistoryService.recordChange(
                    inventoryId = 1L,
                    changeType = InventoryChangeType.RESERVE,
                    quantity = 20,
                    beforeQuantity = 100,
                    afterQuantity = 80,
                    reason = "주문 예약",
                    referenceId = "ORDER-001"
                )

                verify(exactly = 1) {
                    inventoryHistoryRepository.save(
                        match { history ->
                            history.changeType == InventoryChangeType.RESERVE &&
                            history.quantity == 20 &&
                            history.referenceId == "ORDER-001"
                        }
                    )
                }
            }
        }
    }

    given("InventoryHistoryService의 getHistoryByInventoryId 메서드가 주어졌을 때") {
        val histories = listOf(
            InventoryHistory(
                id = 1L,
                inventoryId = 1L,
                changeType = InventoryChangeType.INCREASE,
                quantity = 50,
                beforeQuantity = 100,
                afterQuantity = 150,
                reason = "재고 입고",
                referenceId = "INBOUND-001"
            ),
            InventoryHistory(
                id = 2L,
                inventoryId = 1L,
                changeType = InventoryChangeType.RESERVE,
                quantity = 30,
                beforeQuantity = 150,
                afterQuantity = 120,
                reason = "주문 예약",
                referenceId = "ORDER-001"
            )
        )

        `when`("재고 ID로 이력을 조회하면") {
            every { inventoryHistoryRepository.findByInventoryId(1L) } returns histories

            then("해당 재고의 모든 이력이 반환되어야 한다") {
                val result = inventoryHistoryService.getHistoryByInventoryId(1L)

                result shouldHaveSize 2
                result[0].changeType shouldBe InventoryChangeType.INCREASE
                result[1].changeType shouldBe InventoryChangeType.RESERVE

                verify(exactly = 1) { inventoryHistoryRepository.findByInventoryId(1L) }
            }
        }

        `when`("이력이 없는 재고 ID로 조회하면") {
            every { inventoryHistoryRepository.findByInventoryId(999L) } returns emptyList()

            then("빈 리스트가 반환되어야 한다") {
                val result = inventoryHistoryService.getHistoryByInventoryId(999L)

                result shouldHaveSize 0

                verify(exactly = 1) { inventoryHistoryRepository.findByInventoryId(999L) }
            }
        }
    }

    given("InventoryHistoryService의 getHistoryByReferenceId 메서드가 주어졌을 때") {
        val histories = listOf(
            InventoryHistory(
                id = 1L,
                inventoryId = 1L,
                changeType = InventoryChangeType.RESERVE,
                quantity = 30,
                beforeQuantity = 100,
                afterQuantity = 70,
                reason = "주문 예약",
                referenceId = "ORDER-001"
            ),
            InventoryHistory(
                id = 2L,
                inventoryId = 2L,
                changeType = InventoryChangeType.RESERVE,
                quantity = 20,
                beforeQuantity = 50,
                afterQuantity = 30,
                reason = "주문 예약",
                referenceId = "ORDER-001"
            )
        )

        `when`("참조 ID로 이력을 조회하면") {
            every { inventoryHistoryRepository.findByReferenceId("ORDER-001") } returns histories

            then("해당 참조 ID의 모든 이력이 반환되어야 한다") {
                val result = inventoryHistoryService.getHistoryByReferenceId("ORDER-001")

                result shouldHaveSize 2
                result[0].referenceId shouldBe "ORDER-001"
                result[1].referenceId shouldBe "ORDER-001"

                verify(exactly = 1) { inventoryHistoryRepository.findByReferenceId("ORDER-001") }
            }
        }

        `when`("이력이 없는 참조 ID로 조회하면") {
            every { inventoryHistoryRepository.findByReferenceId("INVALID-REF") } returns emptyList()

            then("빈 리스트가 반환되어야 한다") {
                val result = inventoryHistoryService.getHistoryByReferenceId("INVALID-REF")

                result shouldHaveSize 0

                verify(exactly = 1) { inventoryHistoryRepository.findByReferenceId("INVALID-REF") }
            }
        }
    }
})
