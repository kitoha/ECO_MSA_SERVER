package com.ecommerce.inventory.entity

import com.ecommerce.inventory.entity.audit.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.Version

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
@Table(
  name = "inventories",
  indexes = [
    Index(name = "idx_inventories_product_id", columnList = "product_id")
  ]
)
class Inventory (
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(name = "product_id", unique = true, nullable = false, length = 100)
  val productId: String,

  @Column(name = "available_quantity", nullable = false)
  var availableQuantity: Int = 0,

  @Column(name = "reserved_quantity", nullable = false)
  var reservedQuantity: Int = 0,

  @Column(name = "total_quantity", nullable = false)
  var totalQuantity: Int = 0,

  @Version
  val version: Int = 0
) : BaseEntity() {

  fun increaseStock(amount: Int) {
    if (amount <= 0) throw IllegalArgumentException("증가 수량은 양수여야 합니다.")
    this.availableQuantity += amount
    this.totalQuantity += amount
  }

  fun decreaseStock(amount: Int) {
    if (amount <= 0) throw IllegalArgumentException("감소 수량은 양수여야 합니다.")
    if (this.availableQuantity < amount) throw IllegalArgumentException("사용 가능한 재고가 부족합니다.")
    this.availableQuantity -= amount
    this.totalQuantity -= amount
  }

  fun reserveStock(amount: Int) {
    if (amount <= 0) throw IllegalArgumentException("예약 수량은 양수여야 합니다.")
    if (this.availableQuantity < amount) throw IllegalArgumentException("사용 가능한 재고가 부족합니다.")
    this.availableQuantity -= amount
    this.reservedQuantity += amount
  }

  fun releaseReservedStock(amount: Int) {
    if (amount <= 0) throw IllegalArgumentException("해제 수량은 양수여야 합니다.")
    if (this.reservedQuantity < amount) throw IllegalArgumentException("예약된 재고가 부족합니다.")
    this.reservedQuantity -= amount
    this.availableQuantity += amount
  }

  fun confirmReservedStock(amount: Int) {
    if (amount <= 0) throw IllegalArgumentException("확정 수량은 양수여야 합니다.")
    if (this.reservedQuantity < amount) throw IllegalArgumentException("예약된 재고가 부족합니다.")
    this.reservedQuantity -= amount
    this.totalQuantity -= amount
  }
}