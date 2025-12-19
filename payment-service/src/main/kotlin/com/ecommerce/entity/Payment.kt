package com.ecommerce.entity

import com.ecommerce.entity.audit.BaseEntity
import com.ecommerce.enums.PaymentMethod
import com.ecommerce.enums.PaymentStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 결제 엔티티
 */
@Entity
@Table(
  name = "payments",
  indexes = [
    Index(name = "idx_payments_order_id", columnList = "order_id"),
    Index(name = "idx_payments_user_id", columnList = "user_id"),
    Index(name = "idx_payments_status", columnList = "status"),
    Index(name = "idx_payments_pg_payment_key", columnList = "pg_payment_key"),
    Index(name = "idx_payments_created_at", columnList = "created_at DESC")
  ]
)
class Payment(
  @Id
  @Column(name = "id")
  val id: Long,

  @Column(name = "order_id", nullable = false, unique = true, length = 100)
  val orderId: String,

  @Column(name = "user_id", nullable = false, length = 100)
  val userId: String,

  @Column(nullable = false, precision = 19, scale = 2)
  val amount: BigDecimal,

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  var status: PaymentStatus = PaymentStatus.PENDING,

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", length = 50)
  var paymentMethod: PaymentMethod? = null,

  @Column(name = "pg_provider", length = 50)
  var pgProvider: String? = null,

  @Column(name = "pg_payment_key", unique = true, length = 255)
  var pgPaymentKey: String? = null,

  @Column(name = "failure_reason", columnDefinition = "TEXT")
  var failureReason: String? = null,

  @Column(name = "approved_at")
  var approvedAt: LocalDateTime? = null,

  @Version
  val version: Int = 0

) : BaseEntity() {

  @OneToMany(
    mappedBy = "payment",
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
    fetch = FetchType.LAZY
  )
  protected val _transactions: MutableList<PaymentTransaction> = mutableListOf()

  val transactions: List<PaymentTransaction>
    get() = _transactions.toList()

  fun addTransaction(transaction: PaymentTransaction) {
    _transactions.add(transaction)
    transaction.payment = this
  }

  fun startProcessing(pgProvider: String, pgPaymentKey: String, paymentMethod: PaymentMethod) {
    require(status == PaymentStatus.PENDING) {
      "결제 처리를 시작할 수 있는 상태가 아닙니다: $status"
    }
    this.status = PaymentStatus.PROCESSING
    this.pgProvider = pgProvider
    this.pgPaymentKey = pgPaymentKey
    this.paymentMethod = paymentMethod
  }

  fun complete() {
    require(status == PaymentStatus.PROCESSING) {
      "결제를 완료할 수 있는 상태가 아닙니다: $status"
    }
    this.status = PaymentStatus.COMPLETED
    this.approvedAt = LocalDateTime.now()
  }

  fun fail(reason: String) {
    require(status == PaymentStatus.PROCESSING || status == PaymentStatus.PENDING) {
      "결제를 실패 처리할 수 있는 상태가 아닙니다: $status"
    }
    this.status = PaymentStatus.FAILED
    this.failureReason = reason
  }

  fun cancel(reason: String) {
    require(status == PaymentStatus.PENDING || status == PaymentStatus.PROCESSING) {
      "결제를 취소할 수 있는 상태가 아닙니다: $status"
    }
    this.status = PaymentStatus.CANCELLED
    this.failureReason = reason
  }

  fun refund() {
    require(status == PaymentStatus.COMPLETED) {
      "환불할 수 있는 상태가 아닙니다: $status"
    }
    this.status = PaymentStatus.REFUNDED
  }

  fun isPayable(): Boolean {
    return status == PaymentStatus.PENDING
  }

  fun isRefundable(): Boolean {
    return status == PaymentStatus.COMPLETED
  }

  fun isCompleted(): Boolean {
    return status == PaymentStatus.COMPLETED
  }

  fun isFailed(): Boolean {
    return status == PaymentStatus.FAILED
  }

  fun isCancelled(): Boolean {
    return status == PaymentStatus.CANCELLED
  }

  fun isRefunded(): Boolean {
    return status == PaymentStatus.REFUNDED
  }

  override fun toString(): String {
    return "Payment(id=$id, orderId='$orderId', userId='$userId', " +
      "amount=$amount, status=$status, paymentMethod=$paymentMethod, " +
      "pgProvider=$pgProvider, approvedAt=$approvedAt)"
  }
}
