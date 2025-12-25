package com.ecommerce.dto.external

import java.math.BigDecimal
import java.time.LocalDateTime

data class ProductResponse(
    val id: String,
    val name: String,
    val description: String?,
    val categoryId: Long,
    val categoryName: String,
    val originalPrice: BigDecimal,
    val salePrice: BigDecimal,
    val discountRate: BigDecimal,
    val status: String,
    val images: List<ProductImageResponse>,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)