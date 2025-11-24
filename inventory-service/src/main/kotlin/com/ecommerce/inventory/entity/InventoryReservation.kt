package com.ecommerce.inventory.entity

import com.ecommerce.inventory.entity.audit.BaseEntity
import com.ecommerce.inventory.enums.InventoryReservationStatus
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

/*
추후 삭제 예정 (참조용)
CREATE TABLE inventory_reservations (
    id BIGSERIAL PRIMARY KEY,
    inventory_id BIGINT NOT NULL,
    order_id VARCHAR(100) NOT NULL,  -- Order Service의 order_id (외래키 아님)
    quantity INT NOT NULL,
    status VARCHAR(20) NOT NULL,  -- ACTIVE, COMPLETED, CANCELLED
    expires_at TIMESTAMP NOT NULL,  -- 예약 만료 시간 (15분)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (inventory_id) REFERENCES inventories(id) ON DELETE CASCADE
);
 */
@Entity
@Table(name = "inventory_reservations")
class InventoryReservation(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long = 0,

  @ManyToOne
  @JoinColumn(name = "inventory_id", nullable = false)
  val inventory: Inventory,

  val orderId: String,

  val quantity: Int,

  val status: InventoryReservationStatus,

  val expiresAt: LocalDateTime
) : BaseEntity(){
}