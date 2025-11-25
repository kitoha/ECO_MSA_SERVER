package com.ecommerce.inventory.repository.InventoryReservation

import org.springframework.stereotype.Repository

@Repository
class InventoryReservationRepository(
  private val inventoryReservationJpaRepository: InventoryReservationJpaRepository,
  private val inventoryReservationQueryRepository: InventoryReservationQueryRepository
) {
}