package com.ecommerce.product.repository

import com.ecommerce.product.dto.CategoryProductStats
import com.ecommerce.product.entity.Product
import com.ecommerce.product.entity.QCategory
import com.ecommerce.product.entity.QProduct
import com.ecommerce.product.enums.ProductStatus
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
class ProductQueryRepository(
  private val queryFactory: JPAQueryFactory
)  {

  private val product = QProduct.product
  private val category = QCategory.category

  fun searchProducts(
    categoryId: Long?,
    keyword: String?,
    minPrice: BigDecimal?,
    maxPrice: BigDecimal?,
    status: ProductStatus?
  ): List<Product> {
    return queryFactory
      .selectFrom(product)
      .leftJoin(product.category, category).fetchJoin()
      .where(
        notDeleted(),
        eqCategoryId(categoryId),
        containsKeyword(keyword),
        goeMinPrice(minPrice),
        loeMaxPrice(maxPrice),
        eqStatus(status)
      )
      .orderBy(product.createdAt.desc())
      .fetch()
  }

  fun getProductStatsByCategory(): List<CategoryProductStats> {
    return queryFactory
      .select(
        Projections.constructor(
          CategoryProductStats::class.java,
          category.id,
          category.name,
          product.count(),
          product.salePrice.avg()
        )
      )
      .from(product)
      .join(product.category, category)
      .where(notDeleted())
      .groupBy(category.id, category.name)
      .fetch()
  }

  private fun notDeleted(): BooleanExpression {
    return product.deletedAt.isNull
  }

  private fun eqCategoryId(categoryId: Long?): BooleanExpression? {
    return categoryId?.let { product.category.id.eq(it) }
  }

  private fun containsKeyword(keyword: String?): BooleanExpression? {
    return keyword?.let { product.name.containsIgnoreCase(it) }
  }

  private fun goeMinPrice(minPrice: BigDecimal?): BooleanExpression? {
    return minPrice?.let { product.salePrice.goe(it) }
  }

  private fun loeMaxPrice(maxPrice: BigDecimal?): BooleanExpression? {
    return maxPrice?.let { product.salePrice.loe(it) }
  }


  private fun eqStatus(status: ProductStatus?): BooleanExpression? {
    return status?.let {
      product.status.eq(it)
    }
  }
}