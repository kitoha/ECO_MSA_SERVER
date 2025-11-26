package com.ecommerce.inventory.repository.Inventory

import com.ecommerce.inventory.entity.Inventory
import org.springframework.data.jpa.repository.JpaRepository

interface InventoryJpaRepository : JpaRepository<Inventory, Long> {
  fun findByProductId(productId: String) : Inventory?
  fun findByProductIdIn(productIds: List<String>) : List<Inventory>
}