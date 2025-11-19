package com.ecommerce.product.dto

import java.math.BigDecimal

data class CategoryProductStats(
  val categoryId: Long,
  val categoryName: String,
  val productCount: Long,
  val averagePrice: BigDecimal
)
