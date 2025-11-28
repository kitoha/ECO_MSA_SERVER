package com.ecommerce.entity

import com.ecommerce.entity.audit.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version

/*
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,  -- 주문번호 (예: ORD-20250101-000001)
    user_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,  -- PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    total_amount DECIMAL(19, 2) NOT NULL,

    -- 배송 정보
    shipping_address TEXT NOT NULL,
    shipping_name VARCHAR(100) NOT NULL,
    shipping_phone VARCHAR(20) NOT NULL,

    ordered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INT NOT NULL DEFAULT 0
 */
@Entity
@Table(name = "orders")
class Oder(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long,

  @Column(name = "order_number", nullable = false, unique = true)
  val orderNumber: String,

  @Column(name = "user_id", nullable = false)
  val userId: String,

  @Column(name = "status", nullable = false)
  val status: String,

  @Column(name = "total_amount", nullable = false)
  val totalAmount: Int,

  @Column(name = "shipping_address", nullable = false)
  val shippingAddress: String,

  @Column(name = "shipping_name", nullable = false)
  val shippingName: String,

  @Column(name = "shipping_phone", nullable = false)
  val shippingPhone: String,

  @Version
  val version: Int = 0

) : BaseEntity() {

}