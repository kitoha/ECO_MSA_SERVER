package com.ecommerce.inventory.repository.Inventory

import com.ecommerce.inventory.entity.Inventory
import org.springframework.stereotype.Repository

@Repository
class InventoryRepository (
  private val inventoryJpaRepository: InventoryJpaRepository
){

  fun findByProductId(productId: String) : Inventory? {
    return inventoryJpaRepository.findByProductId(productId)
  }

  fun findByProductIdIn(productIds: List<String>) : List<Inventory> {
    return inventoryJpaRepository.findByProductIdIn(productIds)
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