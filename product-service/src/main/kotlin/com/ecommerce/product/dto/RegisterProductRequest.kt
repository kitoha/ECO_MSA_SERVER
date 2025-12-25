package com.ecommerce.product.dto

import com.ecommerce.product.enums.ProductStatus
import java.math.BigDecimal

data class RegisterProductRequest(
  val name: String,
  val description: String?,
  val categoryId: Long,
  val originalPrice: BigDecimal,
  val salePrice: BigDecimal,
  val status: ProductStatus = ProductStatus.DRAFT,
  val images: List<ProductImageData> = emptyList()
)