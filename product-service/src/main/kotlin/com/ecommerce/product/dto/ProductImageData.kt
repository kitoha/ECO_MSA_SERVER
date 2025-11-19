package com.ecommerce.product.dto

data class ProductImageData(
  val imageUrl: String,
  val displayOrder: Int,
  val isThumbnail: Boolean = false
)
