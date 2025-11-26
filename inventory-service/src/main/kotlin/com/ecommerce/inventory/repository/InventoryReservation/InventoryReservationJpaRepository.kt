package com.ecommerce.inventory.repository.InventoryReservation

import com.ecommerce.inventory.entity.InventoryReservation
import com.ecommerce.inventory.enums.ReservationStatus
import org.springframework.data.jpa.repository.JpaRepository

interface InventoryReservationJpaRepository : JpaRepository<InventoryReservation, Long> {
  fun findByOrderIdAndInventoryId(orderId: String, inventoryId: Long): InventoryReservation?
  fun findByOrderIdAndStatus(orderId: String, status: ReservationStatus): List<InventoryReservation>
}