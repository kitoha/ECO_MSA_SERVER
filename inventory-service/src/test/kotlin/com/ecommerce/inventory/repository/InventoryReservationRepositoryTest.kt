package com.ecommerce.inventory.repository

import com.ecommerce.inventory.entity.InventoryReservation
import com.ecommerce.inventory.enums.ReservationStatus
import com.ecommerce.inventory.repository.InventoryReservation.InventoryReservationJpaRepository
import com.ecommerce.inventory.repository.InventoryReservation.InventoryReservationRepository
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
import java.time.LocalDateTime
import java.util.*

class InventoryReservationRepositoryTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val jpaRepository: InventoryReservationJpaRepository = mockk()
    val reservationRepository = InventoryReservationRepository(jpaRepository)

    given("InventoryReservationRepository의 save 메서드") {
        `when`("예약을 저장하면") {
            val reservation = InventoryReservation(
                inventoryId = 1L,
                orderId = "ORDER-001",
                quantity = 30,
                status = ReservationStatus.ACTIVE,
                expiresAt = LocalDateTime.now().plusMinutes(15)
            )

            val savedReservation = InventoryReservation(
                id = 1L,
                inventoryId = 1L,
                orderId = "ORDER-001",
                quantity = 30,
                status = ReservationStatus.ACTIVE,
                expiresAt = LocalDateTime.now().plusMinutes(15)
            )

            every { jpaRepository.save(reservation) } returns savedReservation

            then("jpaRepository의 save 메서드가 호출되고 저장된 예약을 반환한다") {
                val result = reservationRepository.save(reservation)
                result.id shouldBe 1L
                result.orderId shouldBe "ORDER-001"
                verify(exactly = 1) { jpaRepository.save(reservation) }
            }
        }
    }

    given("InventoryReservationRepository의 findById 메서드") {
        `when`("ID로 예약을 조회하면") {
            val reservation = InventoryReservation(
                id = 1L,
                inventoryId = 1L,
                orderId = "ORDER-001",
                quantity = 30,
                status = ReservationStatus.ACTIVE,
                expiresAt = LocalDateTime.now().plusMinutes(15)
            )

            every { jpaRepository.findById(1L) } returns Optional.of(reservation)

            then("jpaRepository의 findById 메서드가 호출되고 예약을 반환한다") {
                val result = reservationRepository.findById(1L)
                result.id shouldBe 1L
                result.orderId shouldBe "ORDER-001"
                verify(exactly = 1) { jpaRepository.findById(1L) }
            }
        }

        `when`("존재하지 않는 ID로 조회하면") {
            every { jpaRepository.findById(999L) } returns Optional.empty()

            then("jpaRepository의 findById 메서드가 호출되고 예외를 던진다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    reservationRepository.findById(999L)
                }
                exception.message shouldBe "Reservation not found: 999"
                verify(exactly = 1) { jpaRepository.findById(999L) }
            }
        }
    }

    given("InventoryReservationRepository의 findByOrderIdAndInventoryId 메서드") {
        `when`("주문 ID와 재고 ID로 예약을 조회하면") {
            val reservation = InventoryReservation(
                id = 1L,
                inventoryId = 1L,
                orderId = "ORDER-001",
                quantity = 30,
                status = ReservationStatus.ACTIVE,
                expiresAt = LocalDateTime.now().plusMinutes(15)
            )

            every { jpaRepository.findByOrderIdAndInventoryId("ORDER-001", 1L) } returns reservation

            then("jpaRepository의 findByOrderIdAndInventoryId 메서드가 호출되고 예약을 반환한다") {
                val result = reservationRepository.findByOrderIdAndInventoryId("ORDER-001", 1L)
                result.shouldNotBeNull()
                result.orderId shouldBe "ORDER-001"
                result.inventoryId shouldBe 1L
                verify(exactly = 1) { jpaRepository.findByOrderIdAndInventoryId("ORDER-001", 1L) }
            }
        }

        `when`("존재하지 않는 조합으로 조회하면") {
            every { jpaRepository.findByOrderIdAndInventoryId("INVALID-ORDER", 999L) } returns null

            then("jpaRepository의 findByOrderIdAndInventoryId 메서드가 호출되고 null을 반환한다") {
                val result = reservationRepository.findByOrderIdAndInventoryId("INVALID-ORDER", 999L)
                result.shouldBeNull()
                verify(exactly = 1) { jpaRepository.findByOrderIdAndInventoryId("INVALID-ORDER", 999L) }
            }
        }
    }

    given("InventoryReservationRepository의 findActiveReservationsByOrderId 메서드") {
        `when`("주문 ID로 활성 예약을 조회하면") {
            val reservations = listOf(
                InventoryReservation(
                    id = 1L,
                    inventoryId = 1L,
                    orderId = "ORDER-001",
                    quantity = 30,
                    status = ReservationStatus.ACTIVE,
                    expiresAt = LocalDateTime.now().plusMinutes(15)
                ),
                InventoryReservation(
                    id = 2L,
                    inventoryId = 2L,
                    orderId = "ORDER-001",
                    quantity = 20,
                    status = ReservationStatus.ACTIVE,
                    expiresAt = LocalDateTime.now().plusMinutes(15)
                )
            )

            every { jpaRepository.findByOrderIdAndStatus("ORDER-001", ReservationStatus.ACTIVE) } returns reservations

            then("jpaRepository의 findByOrderIdAndStatus 메서드가 호출되고 예약 목록을 반환한다") {
                val result = reservationRepository.findActiveReservationsByOrderId("ORDER-001")
                result shouldHaveSize 2
                result[0].orderId shouldBe "ORDER-001"
                result[1].orderId shouldBe "ORDER-001"
                verify(exactly = 1) { jpaRepository.findByOrderIdAndStatus("ORDER-001", ReservationStatus.ACTIVE) }
            }
        }

        `when`("활성 예약이 없는 주문 ID로 조회하면") {
            every { jpaRepository.findByOrderIdAndStatus("ORDER-NO-ACTIVE", ReservationStatus.ACTIVE) } returns emptyList()

            then("jpaRepository의 findByOrderIdAndStatus 메서드가 호출되고 빈 목록을 반환한다") {
                val result = reservationRepository.findActiveReservationsByOrderId("ORDER-NO-ACTIVE")
                result shouldHaveSize 0
                verify(exactly = 1) { jpaRepository.findByOrderIdAndStatus("ORDER-NO-ACTIVE", ReservationStatus.ACTIVE) }
            }
        }
    }
})
