package com.ecommerce.product.dto

import com.ecommerce.product.entity.Product
import com.ecommerce.product.enums.ProductStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class ProductResponse(
  val id: Long,
  val name: String,
  val description: String?,
  val categoryId: Long,
  val categoryName: String,
  val originalPrice: BigDecimal,
  val salePrice: BigDecimal,
  val discountRate: BigDecimal,
  val status: ProductStatus,
  val images: List<ProductImageResponse>,
  val createdAt: LocalDateTime?,
  val updatedAt: LocalDateTime?
) {
  companion object {
    fun from(product: Product): ProductResponse {
      return ProductResponse(
        id = product.id!!,
        name = product.name,
        description = product.description,
        categoryId = product.category.id!!,
        categoryName = product.category.name,
        originalPrice = product.originalPrice,
        salePrice = product.salePrice,
        discountRate = product.getDiscountRate(),
        status = product.status,
        images = product.images.map { ProductImageResponse.from(it) },
        createdAt = product.createdAt,
        updatedAt = product.updatedAt
      )
    }
  }
}
