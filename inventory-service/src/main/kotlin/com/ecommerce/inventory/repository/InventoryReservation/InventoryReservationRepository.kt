package com.ecommerce.inventory.repository.InventoryReservation

import com.ecommerce.inventory.entity.InventoryReservation
import com.ecommerce.inventory.enums.ReservationStatus
import org.springframework.stereotype.Repository

@Repository
class InventoryReservationRepository(
  private val inventoryReservationJpaRepository: InventoryReservationJpaRepository,
  private val inventoryReservationQueryRepository: InventoryReservationQueryRepository
) {

  fun save(reservation: InventoryReservation) : InventoryReservation {
    return inventoryReservationJpaRepository.save(reservation)
  }

  fun findById(reservationId: Long): InventoryReservation {
    return inventoryReservationJpaRepository.findById(reservationId).orElseThrow {
      IllegalArgumentException("Reservation not found: $reservationId")
    }
  }

  fun findByOrderIdAndInventoryId(orderId: String, inventoryId: Long): InventoryReservation? {
    return inventoryReservationJpaRepository.findByOrderIdAndInventoryId(orderId, inventoryId)
  }

  fun findActiveReservationsByOrderId(orderId: String): List<InventoryReservation> {
    return inventoryReservationJpaRepository.findByOrderIdAndStatus(orderId, ReservationStatus.ACTIVE)
  }
}