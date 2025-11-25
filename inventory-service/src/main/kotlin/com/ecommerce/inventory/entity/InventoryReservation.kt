package com.ecommerce.inventory.entity

import com.ecommerce.inventory.entity.audit.BaseEntity
import com.ecommerce.inventory.enums.InventoryReservationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
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
@Table(
  name = "inventory_reservations",
  indexes = [
    Index(name = "idx_inventory_reservations_inventory_id", columnList = "inventory_id"),
    Index(name = "idx_inventory_reservations_order_id", columnList = "order_id"),
    Index(name = "idx_inventory_reservations_status", columnList = "status"),
    Index(name = "idx_inventory_reservations_expires_at", columnList = "expires_at")
  ]
)
class InventoryReservation(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(name = "inventory_id", nullable = false)
  val inventoryId: Long,

  @Column(name = "order_id", nullable = false, length = 100)
  val orderId: String,

  @Column(nullable = false)
  val quantity: Int,

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  var status: InventoryReservationStatus,

  @Column(name = "expires_at", nullable = false)
  val expiresAt: LocalDateTime
) : BaseEntity(){
}