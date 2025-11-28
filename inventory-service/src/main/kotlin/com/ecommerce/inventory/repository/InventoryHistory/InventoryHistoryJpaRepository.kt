package com.ecommerce.inventory.repository.InventoryHistory

import com.ecommerce.inventory.entity.InventoryHistory
import org.springframework.data.jpa.repository.JpaRepository

interface InventoryHistoryJpaRepository : JpaRepository<InventoryHistory, Long> {
    fun findByInventoryIdOrderByCreatedAtDesc(inventoryId: Long): List<InventoryHistory>
    fun findByReferenceIdOrderByCreatedAtDesc(referenceId: String): List<InventoryHistory>
}