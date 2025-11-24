package com.ecommerce.inventory.entity

import com.ecommerce.inventory.entity.audit.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.Columns

/*
추후 삭제 예정 (참조용)
CREATE TABLE inventories (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(100) NOT NULL UNIQUE,  -- Product Service의 product_id (외래키 아님)
    available_quantity INT NOT NULL DEFAULT 0,  -- 사용 가능한 재고
    reserved_quantity INT NOT NULL DEFAULT 0,   -- 예약된 재고
    total_quantity INT NOT NULL DEFAULT 0,      -- 총 재고 (available + reserved)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INT NOT NULL DEFAULT 0  -- 낙관적 락
);
 */
@Entity
@Table(name = "inventories")
class Inventory (
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column
  val productId: String,

  @Column
  val availableQuantity: Int,

  @Column
  val reservedQuantity: Int,

  @Column
  val totalQuantity: Int,

  @Version
  val version: Int = 0
) : BaseEntity() {
}