package com.ecommerce.product.entity

import com.ecommerce.product.entity.audit.BaseEntity
import com.ecommerce.product.enums.ProductStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  var category: Category,

  @Column(nullable = false, precision = 10, scale = 2)
  var originalPrice: BigDecimal,

  @Column(nullable = false, precision = 10, scale = 2)
  var salePrice: BigDecimal,

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  var status: ProductStatus = ProductStatus.DRAFT
) : BaseEntity(){

  @OneToMany(
    mappedBy = "product",
    cascade = [CascadeType.ALL],
    orphanRemoval = true
  )
  protected val _images: MutableList<ProductImage> = mutableListOf()

  val images: List<ProductImage>
    get() = _images.toList()

  val categoryId: Long
    get() = category.id ?: throw IllegalStateException("Category ID is null")

  fun addImage(image: ProductImage) {
    _images.add(image)
  }

  fun changeCategory(newCategory: Category) {
    this.category = newCategory
  }

  fun updateName(newName: String) {
    this.name = newName
  }

  fun updateDescription(newDescription: String) {
    this.description = newDescription
  }

  fun updatePrice(newOriginalPrice: BigDecimal, newSalePrice: BigDecimal) {
    require(newOriginalPrice >= BigDecimal.ZERO) { "원가는 0 이상이어야 합니다" }
    require(newSalePrice >= BigDecimal.ZERO) { "판매가는 0 이상이어야 합니다" }
    require(newSalePrice <= newOriginalPrice) { "판매가는 원가보다 클 수 없습니다" }

    this.originalPrice = newOriginalPrice
    this.salePrice = newSalePrice
  }

  fun changeStatus(newStatus: ProductStatus) {
    this.status = newStatus
  }

  fun delete() {
    softDelete()
  }

  fun getDiscountRate(): BigDecimal {
    if (originalPrice == BigDecimal.ZERO) return BigDecimal.ZERO
    return (originalPrice - salePrice)
      .multiply(BigDecimal(100))
      .divide(originalPrice, 2, RoundingMode.HALF_UP)
  }

  fun isOnSale(): Boolean = salePrice < originalPrice
}