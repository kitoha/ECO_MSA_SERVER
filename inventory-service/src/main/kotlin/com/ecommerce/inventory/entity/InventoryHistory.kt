package com.ecommerce.inventory.entity

import com.ecommerce.inventory.entity.audit.BaseEntity
import com.ecommerce.inventory.enums.InventoryChangeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

/*
추후 삭제 예정 (참조용)
CREATE TABLE inventory_history (
    id BIGSERIAL PRIMARY KEY,
    inventory_id BIGINT NOT NULL,
    change_type VARCHAR(20) NOT NULL,  -- INCREASE, DECREASE, RESERVE, RELEASE
    quantity INT NOT NULL,              -- 변동 수량
    before_quantity INT NOT NULL,       -- 변경 전 사용가능 수량
    after_quantity INT NOT NULL,        -- 변경 후 사용가능 수량
    reason VARCHAR(100),                -- 변동 사유
    reference_id VARCHAR(100),          -- 참조 ID (주문번호, 입고번호 등)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (inventory_id) REFERENCES inventories(id) ON DELETE CASCADE
);
 */
@Entity
@Table(name = "inventory_history")
class InventoryHistory(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @ManyToOne
  @JoinColumn(name = "inventory_id", nullable = false)
  val inventory: Inventory,

  @Column(name = "change_type", nullable = false)
  val changeType: InventoryChangeType,

  @Column(name = "quantity", nullable = false)
  val quantity: Int,

  @Column(name = "before_quantity", nullable = false)
  val beforeQuantity: Int,

  @Column(name = "after_quantity", nullable = false)
  val afterQuantity: Int,

  @Column(name = "reason")
  val reason: String? = null,

  @Column(name = "reference_id")
  val referenceId: String? = null
) : BaseEntity(){
}