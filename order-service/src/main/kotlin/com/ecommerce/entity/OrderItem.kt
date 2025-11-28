package com.ecommerce.entity

import com.ecommerce.entity.audit.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

/*
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id VARCHAR(100) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    price DECIMAL(19, 2) NOT NULL,  -- 주문 시점의 가격 (스냅샷)
    quantity INT NOT NULL,
    subtotal DECIMAL(19, 2) NOT NULL,  -- price * quantity
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
 */
class OrderItem(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long,

  @Column(name = "order_id", nullable = false)
  val orderId: Long,

  @Column(name = "product_id", nullable = false)
  val productId: String,

  @Column(name = "product_name", nullable = false)
  val productName: String,

  @Column(name = "price", nullable = false)
  val price: Int,

  @Column(name = "quantity", nullable = false)
  val quantity: Int,
) : BaseEntity() {
  val subtotal: Int
    get() = price * quantity
}