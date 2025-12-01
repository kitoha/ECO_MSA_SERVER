package com.ecommerce.dto.external

data class ProductImageResponse(
  val id: Long,
  val imageUrl: String,
  val displayOrder: Int,
  val isThumbnail: Boolean
)