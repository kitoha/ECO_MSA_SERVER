package com.ecommerce.inventory

import com.ecommerce.inventory.entity.Inventory
import com.ecommerce.inventory.repository.Inventory.InventoryJpaRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@DataJpaTest
@Transactional
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(TestJpaConfig::class)
class InventoryRepositoryIntegrationTest(
    private val inventoryJpaRepository: InventoryJpaRepository
) : BehaviorSpec({
    extensions(SpringExtension)

    beforeEach {
        inventoryJpaRepository.deleteAllInBatch()
    }

    given("재고 저장") {
        `when`("새로운 재고를 저장하면") {
            then("ID가 생성되고 저장된 데이터를 조회할 수 있어야 한다") {
                val inventory = Inventory(
                    productId = "PROD-001",
                    availableQuantity = 100,
                    reservedQuantity = 0,
                    totalQuantity = 100
                )
                val saved = inventoryJpaRepository.save(inventory)

                saved.id shouldNotBe null

                val found = inventoryJpaRepository.findByProductId("PROD-001")
                found shouldNotBe null
                found!!.availableQuantity shouldBe 100
                found.totalQuantity shouldBe 100
            }
        }
    }

    given("productId로 재고 조회") {
        val productId = "PROD-002"
        `when`("존재하는 productId로 조회하면") {
            then("해당 재고가 조회되어야 한다") {
                val inventory = Inventory(
                    productId = productId,
                    availableQuantity = 50,
                    reservedQuantity = 10,
                    totalQuantity = 60
                )
                inventoryJpaRepository.save(inventory)

                val found = inventoryJpaRepository.findByProductId(productId)

                found shouldNotBe null
                found!!.productId shouldBe productId
                found.availableQuantity shouldBe 50
                found.reservedQuantity shouldBe 10
            }
        }

        `when`("존재하지 않는 productId로 조회하면") {
            then("null이 반환되어야 한다") {
                val found = inventoryJpaRepository.findByProductId("NON-EXISTENT")
                found shouldBe null
            }
        }
    }

    given("여러 productId로 재고 조회") {
        val productIds = listOf("PROD-A", "PROD-B", "PROD-C")
        `when`("여러 productId로 일괄 조회하면") {
            then("요청한 productId에 해당하는 재고들만 조회되어야 한다") {
                val inventories = listOf(
                    Inventory(productId = productIds[0], availableQuantity = 10, totalQuantity = 10),
                    Inventory(productId = productIds[1], availableQuantity = 20, totalQuantity = 20),
                    Inventory(productId = productIds[2], availableQuantity = 30, totalQuantity = 30)
                )
                inventoryJpaRepository.saveAll(inventories)

                val found = inventoryJpaRepository.findByProductIdIn(listOf(productIds[0], productIds[2]))

                found.size shouldBe 2
                found.map { it.productId }.toSet() shouldBe setOf(productIds[0], productIds[2])
            }
        }
    }

    given("Inventory 엔티티 비즈니스 로직 - increaseStock") {
        `when`("재고를 증가시키면") {
            then("가용 재고와 총 재고가 증가해야 한다") {
                val inventory = Inventory(
                    productId = "PROD-INC",
                    availableQuantity = 100,
                    totalQuantity = 100
                )
                val saved = inventoryJpaRepository.save(inventory)
                saved.increaseStock(50)
                inventoryJpaRepository.flush()

                val found = inventoryJpaRepository.findByProductId("PROD-INC")
                found shouldNotBe null
                found!!.availableQuantity shouldBe 150
                found.totalQuantity shouldBe 150
            }
        }
    }

    given("Inventory 엔티티 비즈니스 로직 - decreaseStock") {
        `when`("재고를 감소시키면") {
            then("가용 재고와 총 재고가 감소해야 한다") {
                val inventory = Inventory(
                    productId = "PROD-DEC",
                    availableQuantity = 100,
                    totalQuantity = 100
                )
                val saved = inventoryJpaRepository.save(inventory)
                saved.decreaseStock(30)
                inventoryJpaRepository.flush()

                val found = inventoryJpaRepository.findByProductId("PROD-DEC")
                found shouldNotBe null
                found!!.availableQuantity shouldBe 70
                found.totalQuantity shouldBe 70
            }
        }
    }

    given("Inventory 엔티티 비즈니스 로직 - reserveStock") {
        `when`("재고를 예약하면") {
            then("가용 재고는 감소하고 예약 재고는 증가해야 한다") {
                val inventory = Inventory(
                    productId = "PROD-RES",
                    availableQuantity = 100,
                    reservedQuantity = 0,
                    totalQuantity = 100
                )
                val saved = inventoryJpaRepository.save(inventory)
                saved.reserveStock(20)
                inventoryJpaRepository.flush()

                val found = inventoryJpaRepository.findByProductId("PROD-RES")
                found shouldNotBe null
                found!!.availableQuantity shouldBe 80
                found.reservedQuantity shouldBe 20
                found.totalQuantity shouldBe 100
            }
        }
    }

    given("Inventory 엔티티 비즈니스 로직 - releaseReservedStock") {
        `when`("예약된 재고를 해제하면") {
            then("예약 재고는 감소하고 가용 재고는 증가해야 한다") {
                val inventory = Inventory(
                    productId = "PROD-REL",
                    availableQuantity = 80,
                    reservedQuantity = 20,
                    totalQuantity = 100
                )
                val saved = inventoryJpaRepository.save(inventory)
                saved.releaseReservedStock(10)
                inventoryJpaRepository.flush()

                val found = inventoryJpaRepository.findByProductId("PROD-REL")
                found shouldNotBe null
                found!!.availableQuantity shouldBe 90
                found.reservedQuantity shouldBe 10
                found.totalQuantity shouldBe 100
            }
        }
    }

    given("Inventory 엔티티 비즈니스 로직 - confirmReservedStock") {
        `when`("예약된 재고를 확정하면") {
            then("예약 재고와 총 재고가 감소해야 한다") {
                val inventory = Inventory(
                    productId = "PROD-CONF",
                    availableQuantity = 80,
                    reservedQuantity = 20,
                    totalQuantity = 100
                )
                val saved = inventoryJpaRepository.save(inventory)
                saved.confirmReservedStock(15)
                inventoryJpaRepository.flush()

                val found = inventoryJpaRepository.findByProductId("PROD-CONF")
                found shouldNotBe null
                found!!.availableQuantity shouldBe 80
                found.reservedQuantity shouldBe 5
                found.totalQuantity shouldBe 85
            }
        }
    }
}) {
    companion object {
        @Container
        @ServiceConnection
        val postgreSQLContainer: PostgreSQLContainer<*> = PostgreSQLContainer("pgvector/pgvector:pg15")
            .withDatabaseName("inventory_test_db")
            .withUsername("test")
            .withPassword("test")
    }
}
