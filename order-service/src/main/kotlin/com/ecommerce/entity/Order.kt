package com.ecommerce.entity

import com.ecommerce.entity.audit.BaseEntity
import com.ecommerce.enums.OrderStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 주문 엔티티
 */
@Entity
@Table(name = "orders")
class Order(
  @Column(name = "order_number", nullable = false, unique = true, length = 50)
  var orderNumber: String,

  @Column(name = "user_id", nullable = false, length = 100)
  val userId: String,

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  var status: OrderStatus,

  @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
  var totalAmount: BigDecimal,

  @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
  var shippingAddress: String,

  @Column(name = "shipping_name", nullable = false, length = 100)
  var shippingName: String,

  @Column(name = "shipping_phone", nullable = false, length = 20)
  var shippingPhone: String,

  @Column(name = "ordered_at", nullable = false)
  val orderedAt: LocalDateTime = LocalDateTime.now(),

  @Version
  val version: Int = 0

) : BaseEntity() {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long = 0L

  @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  val items: MutableList<OrderItem> = mutableListOf()

  fun addItem(item: OrderItem) {
    items.add(item)
    item.order = this
  }

  fun changeStatus(newStatus: OrderStatus) {
    require(isValidStatusTransition(status, newStatus)) {
      "주문 상태를 $status 에서 $newStatus 로 변경할 수 없습니다"
    }
    this.status = newStatus
  }

  fun cancel() {
    require(status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED) {
      "취소 가능한 상태가 아닙니다: $status"
    }
    this.status = OrderStatus.CANCELLED
  }

  fun confirm() {
    require(status == OrderStatus.PENDING) {
      "확정 가능한 상태가 아닙니다: $status"
    }
    this.status = OrderStatus.CONFIRMED
  }

  fun ship() {
    require(status == OrderStatus.CONFIRMED) {
      "배송 시작 가능한 상태가 아닙니다: $status"
    }
    this.status = OrderStatus.SHIPPED
  }

  fun deliver() {
    require(status == OrderStatus.SHIPPED) {
      "배송 완료 가능한 상태가 아닙니다: $status"
    }
    this.status = OrderStatus.DELIVERED
  }

  fun recalculateTotalAmount() {
    this.totalAmount = items.sumOf { it.subtotal }
  }

  private fun isValidStatusTransition(from: OrderStatus, to: OrderStatus): Boolean {
    return when (from) {
      OrderStatus.PENDING -> to in setOf(OrderStatus.CONFIRMED, OrderStatus.CANCELLED)
      OrderStatus.CONFIRMED -> to in setOf(OrderStatus.SHIPPED, OrderStatus.CANCELLED)
      OrderStatus.SHIPPED -> to in setOf(OrderStatus.DELIVERED)
      OrderStatus.DELIVERED -> false
      OrderStatus.CANCELLED -> false
    }
  }

  fun isCancellable(): Boolean {
    return status in setOf(OrderStatus.PENDING, OrderStatus.CONFIRMED)
  }
}