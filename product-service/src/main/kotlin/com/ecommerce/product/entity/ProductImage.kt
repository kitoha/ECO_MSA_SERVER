package com.ecommerce.product.entity

import com.ecommerce.product.entity.audit.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "product_images")
class ProductImage (
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(nullable = false, name = "product_id")
  var productId: Long,

  @Column(nullable = false, length = 500)
  var imageUrl: String,

  @Column(nullable = false)
  var displayOrder: Int = 0
):  BaseEntity()