package com.ecommerce.entity

import com.ecommerce.entity.audit.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal

/**
 * 주문 항목 엔티티
 */
@Entity
@Table(name = "order_items")
class OrderItem(
  @Column(name = "product_id", nullable = false, length = 100)
  val productId: String,

  @Column(name = "product_name", nullable = false)
  val productName: String,

  @Column(name = "price", nullable = false, precision = 19, scale = 2)
  val price: BigDecimal,

  @Column(name = "quantity", nullable = false)
  val quantity: Int,
) : BaseEntity() {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long = 0L

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  lateinit var order: Order

  val subtotal: BigDecimal
    get() = price.multiply(BigDecimal(quantity))

  override fun toString(): String {
    return "OrderItem(id=$id, productId='$productId', productName='$productName', " +
      "price=$price, quantity=$quantity, subtotal=$subtotal)"
  }
}