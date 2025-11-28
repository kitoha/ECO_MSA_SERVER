package com.ecommerce.inventory.repository.InventoryHistory

import com.ecommerce.inventory.entity.InventoryHistory
import org.springframework.stereotype.Repository

@Repository
class InventoryHistoryRepository(
  private val inventoryHistoryJpaRepository: InventoryHistoryJpaRepository
) {

  fun save(inventoryHistory: InventoryHistory) {
    inventoryHistoryJpaRepository.save(inventoryHistory)
  }

  fun findByInventoryId(inventoryId: Long): List<InventoryHistory> {
    return inventoryHistoryJpaRepository.findByInventoryIdOrderByCreatedAtDesc(inventoryId)
  }

  fun findByReferenceId(referenceId: String): List<InventoryHistory> {
    return inventoryHistoryJpaRepository.findByReferenceIdOrderByCreatedAtDesc(referenceId)
  }
}