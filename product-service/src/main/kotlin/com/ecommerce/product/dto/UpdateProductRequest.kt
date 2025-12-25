package com.ecommerce.product.dto

import com.ecommerce.product.enums.ProductStatus
import java.math.BigDecimal

data class UpdateProductRequest(
  val categoryId: Long? = null,
  val name: String? = null,
  val description: String? = null,
  val originalPrice: BigDecimal? = null,
  val salePrice: BigDecimal? = null,
  val status: ProductStatus? = null
)
