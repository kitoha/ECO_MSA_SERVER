package com.ecommerce.inventory.repository

import com.ecommerce.inventory.entity.Inventory
import com.ecommerce.inventory.repository.Inventory.InventoryJpaRepository
import com.ecommerce.inventory.repository.Inventory.InventoryRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*

class InventoryRepositoryTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val jpaRepository: InventoryJpaRepository = mockk()
    val inventoryRepository = InventoryRepository(jpaRepository)

    given("InventoryRepository의 findByProductId 메서드") {
        `when`("상품 ID로 재고를 조회하면") {
            val inventory = Inventory(
                id = 1L,
                productId = "PRODUCT-001",
                availableQuantity = 100,
                reservedQuantity = 0,
                totalQuantity = 100
            )

            every { jpaRepository.findByProductId("PRODUCT-001") } returns inventory

            then("jpaRepository의 findByProductId 메서드가 호출되고 재고를 반환한다") {
                val result = inventoryRepository.findByProductId("PRODUCT-001")
                result.shouldNotBeNull()
                result.productId shouldBe "PRODUCT-001"
                result.availableQuantity shouldBe 100
                verify(exactly = 1) { jpaRepository.findByProductId("PRODUCT-001") }
            }
        }

        `when`("존재하지 않는 상품 ID로 조회하면") {
            every { jpaRepository.findByProductId("INVALID-PRODUCT") } returns null

            then("jpaRepository의 findByProductId 메서드가 호출되고 null을 반환한다") {
                val result = inventoryRepository.findByProductId("INVALID-PRODUCT")
                result.shouldBeNull()
                verify(exactly = 1) { jpaRepository.findByProductId("INVALID-PRODUCT") }
            }
        }
    }

    given("InventoryRepository의 findByProductIdIn 메서드") {
        `when`("여러 상품 ID로 재고를 조회하면") {
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

            every { jpaRepository.findByProductIdIn(listOf("PRODUCT-001", "PRODUCT-002")) } returns inventories

            then("jpaRepository의 findByProductIdIn 메서드가 호출되고 재고 목록을 반환한다") {
                val result = inventoryRepository.findByProductIdIn(listOf("PRODUCT-001", "PRODUCT-002"))
                result shouldHaveSize 2
                result[0].productId shouldBe "PRODUCT-001"
                result[1].productId shouldBe "PRODUCT-002"
                verify(exactly = 1) { jpaRepository.findByProductIdIn(listOf("PRODUCT-001", "PRODUCT-002")) }
            }
        }

        `when`("빈 목록으로 조회하면") {
            every { jpaRepository.findByProductIdIn(emptyList()) } returns emptyList()

            then("jpaRepository의 findByProductIdIn 메서드가 호출되고 빈 목록을 반환한다") {
                val result = inventoryRepository.findByProductIdIn(emptyList())
                result shouldHaveSize 0
                verify(exactly = 1) { jpaRepository.findByProductIdIn(emptyList()) }
            }
        }
    }

    given("InventoryRepository의 save 메서드") {
        `when`("재고를 저장하면") {
            val inventory = Inventory(
                productId = "PRODUCT-001",
                availableQuantity = 100,
                reservedQuantity = 0,
                totalQuantity = 100
            )

            val savedInventory = Inventory(
                id = 1L,
                productId = "PRODUCT-001",
                availableQuantity = 100,
                reservedQuantity = 0,
                totalQuantity = 100
            )

            every { jpaRepository.save(inventory) } returns savedInventory

            then("jpaRepository의 save 메서드가 호출된다") {
                inventoryRepository.save(inventory)
                verify(exactly = 1) { jpaRepository.save(inventory) }
            }
        }
    }

    given("InventoryRepository의 findById 메서드") {
        `when`("ID로 재고를 조회하면") {
            val inventory = Inventory(
                id = 1L,
                productId = "PRODUCT-001",
                availableQuantity = 100,
                reservedQuantity = 0,
                totalQuantity = 100
            )

            every { jpaRepository.findById(1L) } returns Optional.of(inventory)

            then("jpaRepository의 findById 메서드가 호출되고 재고를 반환한다") {
                val result = inventoryRepository.findById(1L)
                result.id shouldBe 1L
                result.productId shouldBe "PRODUCT-001"
                verify(exactly = 1) { jpaRepository.findById(1L) }
            }
        }

        `when`("존재하지 않는 ID로 조회하면") {
            every { jpaRepository.findById(999L) } returns Optional.empty()

            then("jpaRepository의 findById 메서드가 호출되고 예외를 던진다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    inventoryRepository.findById(999L)
                }
                exception.message shouldBe "Inventory not found: 999"
                verify(exactly = 1) { jpaRepository.findById(999L) }
            }
        }
    }
})
