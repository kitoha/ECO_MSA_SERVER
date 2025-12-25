package com.ecommerce.inventory.entity

import com.ecommerce.inventory.enums.ReservationStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class InventoryReservationTest : BehaviorSpec({

    lateinit var reservation: InventoryReservation

    given("InventoryReservation 엔티티가 주어졌을 때") {

        `when`("만료 시간 전의 예약을 생성하면") {
            beforeEach {
                reservation = InventoryReservation(
                    id = 1L,
                    inventoryId = 1L,
                    orderId = "ORDER-001",
                    quantity = 10,
                    status = ReservationStatus.ACTIVE,
                    expiresAt = LocalDateTime.now().plusMinutes(15)
                )
            }

            then("예약이 만료되지 않았어야 한다") {
                reservation.isExpired().shouldBeFalse()
            }

            then("예약이 활성 상태여야 한다") {
                reservation.isActive().shouldBeTrue()
            }
        }

        `when`("만료 시간이 지난 예약을 생성하면") {
            beforeEach {
                reservation = InventoryReservation(
                    id = 1L,
                    inventoryId = 1L,
                    orderId = "ORDER-001",
                    quantity = 10,
                    status = ReservationStatus.ACTIVE,
                    expiresAt = LocalDateTime.now().minusMinutes(1)
                )
            }

            then("예약이 만료되어야 한다") {
                reservation.isExpired().shouldBeTrue()
            }

            then("예약이 활성 상태가 아니어야 한다") {
                reservation.isActive().shouldBeFalse()
            }
        }

        `when`("활성 상태의 예약을 완료 처리할 때") {
            beforeEach {
                reservation = InventoryReservation(
                    id = 1L,
                    inventoryId = 1L,
                    orderId = "ORDER-001",
                    quantity = 10,
                    status = ReservationStatus.ACTIVE,
                    expiresAt = LocalDateTime.now().plusMinutes(15)
                )
            }

            then("정상적으로 완료 처리되어야 한다") {
                reservation.markCompleted()
                reservation.status shouldBe ReservationStatus.COMPLETED
            }
        }

        `when`("이미 완료된 예약을 완료 처리하려 하면") {
            beforeEach {
                reservation = InventoryReservation(
                    id = 1L,
                    inventoryId = 1L,
                    orderId = "ORDER-001",
                    quantity = 10,
                    status = ReservationStatus.COMPLETED,
                    expiresAt = LocalDateTime.now().plusMinutes(15)
                )
            }

            then("예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalStateException> {
                    reservation.markCompleted()
                }
                exception.message shouldBe "Only ACTIVE reservations can be completed. Current status: COMPLETED"
            }
        }

        `when`("이미 취소된 예약을 완료 처리하려 하면") {
            beforeEach {
                reservation = InventoryReservation(
                    id = 1L,
                    inventoryId = 1L,
                    orderId = "ORDER-001",
                    quantity = 10,
                    status = ReservationStatus.CANCELLED,
                    expiresAt = LocalDateTime.now().plusMinutes(15)
                )
            }

            then("예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalStateException> {
                    reservation.markCompleted()
                }
                exception.message shouldBe "Only ACTIVE reservations can be completed. Current status: CANCELLED"
            }
        }

        `when`("만료된 예약을 완료 처리하려 하면") {
            beforeEach {
                reservation = InventoryReservation(
                    id = 1L,
                    inventoryId = 1L,
                    orderId = "ORDER-001",
                    quantity = 10,
                    status = ReservationStatus.ACTIVE,
                    expiresAt = LocalDateTime.now().minusMinutes(1)
                )
            }

            then("예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalStateException> {
                    reservation.markCompleted()
                }
                exception.message shouldBe "Cannot complete expired reservation"
            }
        }

        `when`("활성 상태의 예약을 취소할 때") {
            beforeEach {
                reservation = InventoryReservation(
                    id = 1L,
                    inventoryId = 1L,
                    orderId = "ORDER-001",
                    quantity = 10,
                    status = ReservationStatus.ACTIVE,
                    expiresAt = LocalDateTime.now().plusMinutes(15)
                )
            }

            then("정상적으로 취소 처리되어야 한다") {
                reservation.markCancelled()
                reservation.status shouldBe ReservationStatus.CANCELLED
            }
        }

        `when`("만료된 활성 예약을 취소할 때") {
            beforeEach {
                reservation = InventoryReservation(
                    id = 1L,
                    inventoryId = 1L,
                    orderId = "ORDER-001",
                    quantity = 10,
                    status = ReservationStatus.ACTIVE,
                    expiresAt = LocalDateTime.now().minusMinutes(1)
                )
            }

            then("정상적으로 취소 처리되어야 한다") {
                reservation.markCancelled()
                reservation.status shouldBe ReservationStatus.CANCELLED
            }
        }

        `when`("이미 완료된 예약을 취소하려 하면") {
            beforeEach {
                reservation = InventoryReservation(
                    id = 1L,
                    inventoryId = 1L,
                    orderId = "ORDER-001",
                    quantity = 10,
                    status = ReservationStatus.COMPLETED,
                    expiresAt = LocalDateTime.now().plusMinutes(15)
                )
            }

            then("예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalStateException> {
                    reservation.markCancelled()
                }
                exception.message shouldBe "Only ACTIVE reservations can be cancelled. Current status: COMPLETED"
            }
        }

        `when`("이미 취소된 예약을 취소하려 하면") {
            beforeEach {
                reservation = InventoryReservation(
                    id = 1L,
                    inventoryId = 1L,
                    orderId = "ORDER-001",
                    quantity = 10,
                    status = ReservationStatus.CANCELLED,
                    expiresAt = LocalDateTime.now().plusMinutes(15)
                )
            }

            then("예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalStateException> {
                    reservation.markCancelled()
                }
                exception.message shouldBe "Only ACTIVE reservations can be cancelled. Current status: CANCELLED"
            }
        }

        `when`("상태별로 예약을 확인할 때") {
            then("ACTIVE 상태이고 만료되지 않은 예약은 활성 상태여야 한다") {
                val activeReservation = InventoryReservation(
                    inventoryId = 1L,
                    orderId = "ORDER-001",
                    quantity = 10,
                    status = ReservationStatus.ACTIVE,
                    expiresAt = LocalDateTime.now().plusMinutes(15)
                )
                activeReservation.isActive().shouldBeTrue()
            }

            then("COMPLETED 상태의 예약은 활성 상태가 아니어야 한다") {
                val completedReservation = InventoryReservation(
                    inventoryId = 1L,
                    orderId = "ORDER-001",
                    quantity = 10,
                    status = ReservationStatus.COMPLETED,
                    expiresAt = LocalDateTime.now().plusMinutes(15)
                )
                completedReservation.isActive().shouldBeFalse()
            }

            then("CANCELLED 상태의 예약은 활성 상태가 아니어야 한다") {
                val cancelledReservation = InventoryReservation(
                    inventoryId = 1L,
                    orderId = "ORDER-001",
                    quantity = 10,
                    status = ReservationStatus.CANCELLED,
                    expiresAt = LocalDateTime.now().plusMinutes(15)
                )
                cancelledReservation.isActive().shouldBeFalse()
            }
        }
    }
})
