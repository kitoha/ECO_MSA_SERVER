package com.ecommerce.inventory.repository.InventoryReservation

import com.ecommerce.inventory.entity.InventoryReservation
import org.springframework.data.jpa.repository.JpaRepository

interface InventoryReservationJpaRepository : JpaRepository<InventoryReservation, Long> {
}