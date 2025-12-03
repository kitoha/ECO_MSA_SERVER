package com.ecommerce.product.repository

import com.ecommerce.product.dto.CategoryProductStats
import com.ecommerce.product.entity.Product
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
class ProductRepository(
  private val jpaRepository: ProductJpaRepository,
  private val queryRepository: ProductQueryRepository
) {

  /**
   * 상품 저장 또는 수정
   */
  fun save(product: Product): Product {
    return jpaRepository.save(product)
  }

  /**
   * 상품 단건 조회
   */
  fun findByIdAndNotDeleted(id: Long): Product? {
    return jpaRepository.findByIdWithDetails(id)
  }

  /**
   * 여러 상품을 ID로 한번에 조회 (Batch)
   *
   */
  fun findByIdsAndNotDeleted(ids: List<Long>): List<Product> {
    return queryRepository.findByIdsWithDetails(ids)
  }

  /**
   * 모든 상품 조회
   */
  fun findAllNotDeleted(): List<Product> {
    return jpaRepository.findAllWithDetails()
  }

  /**
   * 카테고리별 상품 조회
   */
  fun findByCategoryId(categoryId: Long): List<Product> {
    return jpaRepository.findByCategoryIdWithDetails(categoryId)
  }

  /**
   * 상품 존재 여부 확인
   */
  fun existsByIdAndNotDeleted(id: Long): Boolean {
    return jpaRepository.existsByIdAndNotDeleted(id)
  }

  /**
   * 모든 상품 개수 조회
   */
  fun count(): Long {
    return jpaRepository.count()
  }

  /**
   * 상품 삭제
   */
  fun delete(product: Product) {
    jpaRepository.delete(product)
  }

  fun searchProducts(
    categoryId: Long? = null,
    keyword: String? = null,
    minPrice: BigDecimal? = null,
    maxPrice: BigDecimal? = null,
    status: com.ecommerce.product.enums.ProductStatus? = null
  ): List<Product> {
    return queryRepository.searchProducts(
      categoryId = categoryId,
      keyword = keyword,
      minPrice = minPrice,
      maxPrice = maxPrice,
      status = status
    )
  }

  fun getProductStatsByCategory(): List<CategoryProductStats> {
    return queryRepository.getProductStatsByCategory()
  }
}