package com.ecommerce.inventory.repository.Inventory

import com.ecommerce.inventory.entity.Inventory
import org.springframework.stereotype.Repository

@Repository
class InventoryRepository (
  private val inventoryJpaRepository: InventoryJpaRepository,
  private val inventoryQueryRepository: InventoryQueryRepository
){

  fun findByProductId(productId: String) : Inventory? {
    return inventoryJpaRepository.findByProductId(productId)
  }

  fun save(inventory: Inventory) {
    inventoryJpaRepository.save(inventory)
  }

  fun findById(inventoryId: Long) : Inventory {
    return inventoryJpaRepository.findById(inventoryId).orElseThrow {
      IllegalArgumentException("Inventory not found: $inventoryId")
    }
  }

}