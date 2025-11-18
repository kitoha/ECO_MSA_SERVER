package com.ecommerce.product.entity

import com.ecommerce.product.enums.ProductStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.math.RoundingMode

@Entity
@Table(name = "products")
class Product (
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(nullable = false, length = 200)
  var name: String,

  @Column(columnDefinition = "TEXT")
  var description: String? = null,

  @Column(nullable = false, precision = 10, scale = 2)
  var originalPrice: BigDecimal,

  @Column(nullable = false, precision = 10, scale = 2)
  var salePrice: BigDecimal,

  @Column(nullable = false, name = "category_id")
  var categoryId: Long,

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  var status: ProductStatus = ProductStatus.DRAFT
){
  fun getDiscountRate(): BigDecimal {
    if (originalPrice == BigDecimal.ZERO) return BigDecimal.ZERO
    return ((originalPrice - salePrice) / originalPrice * BigDecimal(100))
      .setScale(2, RoundingMode.HALF_UP)
  }

  fun isOnSale(): Boolean = salePrice < originalPrice
}