package com.ecommerce.entity

import com.ecommerce.entity.audit.BaseEntity
import com.ecommerce.enums.TransactionStatus
import com.ecommerce.enums.TransactionType
import jakarta.persistence.*
import java.math.BigDecimal

/**
 * 결제 트랜잭션 엔티티 (결제 승인, 취소, 환불 등의 모든 트랜잭션을 기록하여 추적성을 확보)
 */
@Entity
@Table(
  name = "payment_transactions",
  indexes = [
    Index(name = "idx_payment_transactions_payment_id", columnList = "payment_id"),
    Index(name = "idx_payment_transactions_type", columnList = "transaction_type"),
    Index(name = "idx_payment_transactions_created_at", columnList = "created_at DESC")
  ]
)
class PaymentTransaction(
  @Column(name = "transaction_type", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  val transactionType: TransactionType,

  @Column(nullable = false, precision = 19, scale = 2)
  val amount: BigDecimal,

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  val status: TransactionStatus,

  @Column(name = "pg_transaction_id", length = 255)
  val pgTransactionId: String? = null,

  @Column(name = "pg_response_code", length = 20)
  val pgResponseCode: String? = null,

  @Column(name = "pg_response_message", columnDefinition = "TEXT")
  val pgResponseMessage: String? = null

) : BaseEntity() {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long = 0L

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", nullable = false)
  lateinit var payment: Payment

  fun isSuccess(): Boolean {
    return status == TransactionStatus.SUCCESS
  }

  fun isFailed(): Boolean {
    return status == TransactionStatus.FAILED
  }

  override fun toString(): String {
    return "PaymentTransaction(id=$id, transactionType=$transactionType, " +
      "amount=$amount, status=$status, pgTransactionId=$pgTransactionId, " +
      "pgResponseCode=$pgResponseCode)"
  }

  companion object {
    fun success(
      transactionType: TransactionType,
      amount: BigDecimal,
      pgTransactionId: String? = null,
      pgResponseCode: String? = null,
      pgResponseMessage: String? = null
    ): PaymentTransaction {
      return PaymentTransaction(
        transactionType = transactionType,
        amount = amount,
        status = TransactionStatus.SUCCESS,
        pgTransactionId = pgTransactionId,
        pgResponseCode = pgResponseCode,
        pgResponseMessage = pgResponseMessage
      )
    }

    fun failure(
      transactionType: TransactionType,
      amount: BigDecimal,
      pgTransactionId: String? = null,
      pgResponseCode: String? = null,
      pgResponseMessage: String? = null
    ): PaymentTransaction {
      return PaymentTransaction(
        transactionType = transactionType,
        amount = amount,
        status = TransactionStatus.FAILED,
        pgTransactionId = pgTransactionId,
        pgResponseCode = pgResponseCode,
        pgResponseMessage = pgResponseMessage
      )
    }
  }
}
