package com.ecommerce.inventory.repository.InventoryHistory

import com.ecommerce.inventory.entity.InventoryHistory
import org.springframework.stereotype.Repository

@Repository
class InventoryHistoryRepository(
  private val inventoryHistoryJpaRepository: InventoryHistoryJpaRepository,
  private val inventoryHistoryQueryRepository: InventoryHistoryQueryRepository
) {

  fun save(inventoryHistory: InventoryHistory) {
    inventoryHistoryJpaRepository.save(inventoryHistory)
  }
}