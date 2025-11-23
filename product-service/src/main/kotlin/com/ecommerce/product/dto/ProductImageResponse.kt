package com.ecommerce.product.dto

import com.ecommerce.product.entity.ProductImage

data class ProductImageResponse(
  val id: Long,
  val imageUrl: String,
  val displayOrder: Int,
  val isThumbnail: Boolean
) {
  companion object {
    fun from(image: ProductImage): ProductImageResponse {
      return ProductImageResponse(
        id = image.id!!,
        imageUrl = image.imageUrl,
        displayOrder = image.displayOrder,
        isThumbnail = image.isThumbnail
      )
    }
  }
}
