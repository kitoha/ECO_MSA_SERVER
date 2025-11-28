package com.ecommerce.inventory.service

import com.ecommerce.inventory.entity.Inventory
import com.ecommerce.inventory.entity.InventoryReservation
import com.ecommerce.inventory.enums.InventoryChangeType
import com.ecommerce.inventory.enums.ReservationStatus
import com.ecommerce.inventory.repository.Inventory.InventoryRepository
import com.ecommerce.inventory.repository.InventoryReservation.InventoryReservationRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.kafka.core.KafkaTemplate
import java.time.LocalDateTime

class InventoryReservationServiceTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val inventoryRepository = mockk<InventoryRepository>()
    val inventoryReservationRepository = mockk<InventoryReservationRepository>()
    val inventoryHistoryService = mockk<InventoryHistoryService>()
    val redisTemplate = mockk<RedisTemplate<String, String>>()
    val kafkaTemplate = mockk<KafkaTemplate<String, Any>>()
    val zSetOperations = mockk<ZSetOperations<String, String>>()

    val inventoryReservationService = InventoryReservationService(
        inventoryRepository,
        inventoryReservationRepository,
        inventoryHistoryService,
        redisTemplate,
        kafkaTemplate
    )

    beforeEach {
        clearMocks(
            inventoryRepository,
            inventoryReservationRepository,
            inventoryHistoryService,
            redisTemplate,
            kafkaTemplate,
            zSetOperations,
            answers = false
        )
        every { redisTemplate.opsForZSet() } returns zSetOperations
    }

    given("InventoryReservationService의 createReservation 메서드가 주어졌을 때") {
        val inventory = Inventory(
            id = 1L,
            productId = "PRODUCT-001",
            availableQuantity = 100,
            reservedQuantity = 0,
            totalQuantity = 100
        )

        val reservation = InventoryReservation(
            id = 1L,
            inventoryId = 1L,
            orderId = "ORDER-001",
            quantity = 30,
            status = ReservationStatus.ACTIVE,
            expiresAt = LocalDateTime.now().plusMinutes(15)
        )

        `when`("새로운 예약을 생성하면") {
            every { inventoryRepository.findByProductId("PRODUCT-001") } returns inventory
            every { inventoryReservationRepository.findByOrderIdAndInventoryId("ORDER-001", 1L) } returns null
            every { inventoryRepository.save(any()) } just runs
            every { inventoryReservationRepository.save(any()) } returns reservation
            every {
                inventoryHistoryService.recordChange(
                    inventoryId = 1L,
                    changeType = InventoryChangeType.RESERVE,
                    quantity = 30,
                    beforeQuantity = 100,
                    afterQuantity = 70,
                    reason = "Stock reserved for order ORDER-001",
                    referenceId = "1"
                )
            } just runs
            every { zSetOperations.add(any(), any(), any()) } returns true
            every { kafkaTemplate.send(any(), any(), any()) } returns mockk()

            then("예약이 정상적으로 생성되어야 한다") {
                val result = inventoryReservationService.createReservation(
                    orderId = "ORDER-001",
                    productId = "PRODUCT-001",
                    quantity = 30
                )

                result.orderId shouldBe "ORDER-001"
                result.quantity shouldBe 30
                result.status shouldBe ReservationStatus.ACTIVE

                verify(exactly = 1) { inventoryRepository.findByProductId("PRODUCT-001") }
                verify(exactly = 1) { inventoryReservationRepository.findByOrderIdAndInventoryId("ORDER-001", 1L) }
                verify(exactly = 1) { inventoryRepository.save(any()) }
                verify(exactly = 1) { inventoryReservationRepository.save(any()) }
                verify(exactly = 1) {
                    inventoryHistoryService.recordChange(
                        inventoryId = 1L,
                        changeType = InventoryChangeType.RESERVE,
                        quantity = 30,
                        beforeQuantity = 100,
                        afterQuantity = 70,
                        reason = "Stock reserved for order ORDER-001",
                        referenceId = "1"
                    )
                }
                verify(exactly = 1) { zSetOperations.add(any(), any(), any()) }
                verify(exactly = 1) { kafkaTemplate.send("reservation-created", "ORDER-001", any()) }
            }
        }

        `when`("이미 활성화된 예약이 있으면") {
            val existingReservation = InventoryReservation(
                id = 2L,
                inventoryId = 1L,
                orderId = "ORDER-001",
                quantity = 30,
                status = ReservationStatus.ACTIVE,
                expiresAt = LocalDateTime.now().plusMinutes(10)
            )

            every { inventoryRepository.findByProductId("PRODUCT-001") } returns inventory
            every { inventoryReservationRepository.findByOrderIdAndInventoryId("ORDER-001", 1L) } returns existingReservation

            then("기존 예약을 반환해야 한다") {
                val result = inventoryReservationService.createReservation(
                    orderId = "ORDER-001",
                    productId = "PRODUCT-001",
                    quantity = 30
                )

                result.id shouldBe 2L
                result.orderId shouldBe "ORDER-001"

                verify(exactly = 1) { inventoryRepository.findByProductId("PRODUCT-001") }
                verify(exactly = 1) { inventoryReservationRepository.findByOrderIdAndInventoryId("ORDER-001", 1L) }
                verify(exactly = 0) { inventoryRepository.save(any()) }
                verify(exactly = 0) { inventoryReservationRepository.save(any()) }
            }
        }

        `when`("존재하지 않는 상품으로 예약을 생성하면") {
            every { inventoryRepository.findByProductId("INVALID-PRODUCT") } returns null

            then("예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    inventoryReservationService.createReservation(
                        orderId = "ORDER-001",
                        productId = "INVALID-PRODUCT",
                        quantity = 30
                    )
                }
                exception.message shouldBe "Product not found: INVALID-PRODUCT"
            }
        }
    }

    given("InventoryReservationService의 confirmReservation 메서드가 주어졌을 때") {
        val inventory = Inventory(
            id = 1L,
            productId = "PRODUCT-001",
            availableQuantity = 70,
            reservedQuantity = 30,
            totalQuantity = 100
        )

        val reservation = InventoryReservation(
            id = 1L,
            inventoryId = 1L,
            orderId = "ORDER-001",
            quantity = 30,
            status = ReservationStatus.ACTIVE,
            expiresAt = LocalDateTime.now().plusMinutes(10)
        )

        `when`("예약을 확정하면") {
            every { inventoryReservationRepository.findById(1L) } returns reservation
            every { inventoryRepository.findById(1L) } returns inventory
            every { inventoryReservationRepository.save(any()) } returns reservation
            every { inventoryRepository.save(any()) } just runs
            every {
                inventoryHistoryService.recordChange(
                    inventoryId = 1L,
                    changeType = InventoryChangeType.DECREASE,
                    quantity = 30,
                    beforeQuantity = 100,
                    afterQuantity = 70,
                    reason = "Stock reservation confirmed for reservation 1",
                    referenceId = "1"
                )
            } just runs
            every { zSetOperations.remove(any(), any()) } returns 1L
            every { kafkaTemplate.send(any(), any(), any()) } returns mockk()

            then("예약이 정상적으로 확정되어야 한다") {
                inventoryReservationService.confirmReservation(1L)

                reservation.status shouldBe ReservationStatus.COMPLETED
                inventory.reservedQuantity shouldBe 0
                inventory.totalQuantity shouldBe 70

                verify(exactly = 1) { inventoryReservationRepository.findById(1L) }
                verify(exactly = 1) { inventoryRepository.findById(1L) }
                verify(exactly = 1) { inventoryReservationRepository.save(any()) }
                verify(exactly = 1) { inventoryRepository.save(any()) }
                verify(exactly = 1) {
                    inventoryHistoryService.recordChange(
                        inventoryId = 1L,
                        changeType = InventoryChangeType.DECREASE,
                        quantity = 30,
                        beforeQuantity = 100,
                        afterQuantity = 70,
                        reason = "Stock reservation confirmed for reservation 1",
                        referenceId = "1"
                    )
                }
                verify(exactly = 1) { zSetOperations.remove(any(), "1") }
                verify(exactly = 1) { kafkaTemplate.send("reservation-confirmed", "ORDER-001", any()) }
            }
        }
    }

    given("InventoryReservationService의 cancelReservation 메서드가 주어졌을 때") {
        val inventory = Inventory(
            id = 1L,
            productId = "PRODUCT-001",
            availableQuantity = 70,
            reservedQuantity = 30,
            totalQuantity = 100
        )

        val reservation = InventoryReservation(
            id = 1L,
            inventoryId = 1L,
            orderId = "ORDER-001",
            quantity = 30,
            status = ReservationStatus.ACTIVE,
            expiresAt = LocalDateTime.now().plusMinutes(10)
        )

        `when`("예약을 취소하면") {
            every { inventoryReservationRepository.findById(1L) } returns reservation
            every { inventoryRepository.findById(1L) } returns inventory
            every { inventoryReservationRepository.save(any()) } returns reservation
            every { inventoryRepository.save(any()) } just runs
            every {
                inventoryHistoryService.recordChange(
                    inventoryId = 1L,
                    changeType = InventoryChangeType.RELEASE,
                    quantity = 30,
                    beforeQuantity = 70,
                    afterQuantity = 100,
                    reason = "Stock reservation cancelled for reservation 1",
                    referenceId = "1"
                )
            } just runs
            every { zSetOperations.remove(any(), any()) } returns 1L
            every { kafkaTemplate.send(any(), any(), any()) } returns mockk()

            then("예약이 정상적으로 취소되어야 한다") {
                inventoryReservationService.cancelReservation(1L)

                reservation.status shouldBe ReservationStatus.CANCELLED
                inventory.availableQuantity shouldBe 100
                inventory.reservedQuantity shouldBe 0

                verify(exactly = 1) { inventoryReservationRepository.findById(1L) }
                verify(exactly = 1) { inventoryRepository.findById(1L) }
                verify(exactly = 1) { inventoryReservationRepository.save(any()) }
                verify(exactly = 1) { inventoryRepository.save(any()) }
                verify(exactly = 1) {
                    inventoryHistoryService.recordChange(
                        inventoryId = 1L,
                        changeType = InventoryChangeType.RELEASE,
                        quantity = 30,
                        beforeQuantity = 70,
                        afterQuantity = 100,
                        reason = "Stock reservation cancelled for reservation 1",
                        referenceId = "1"
                    )
                }
                verify(exactly = 1) { zSetOperations.remove(any(), "1") }
                verify(exactly = 1) { kafkaTemplate.send("reservation-cancelled", "ORDER-001", any()) }
            }
        }
    }
})
