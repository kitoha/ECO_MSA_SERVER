package com.ecommerce.product.dto

import com.ecommerce.product.enums.ProductStatus
import java.math.BigDecimal

data class ProductSearchRequest(
  val categoryId: Long? = null,
  val keyword: String? = null,
  val minPrice: BigDecimal? = null,
  val maxPrice: BigDecimal? = null,
  val status: ProductStatus? = null
)
