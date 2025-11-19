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
   * 상품 단건 조회 (연관 엔티티 포함, FETCH JOIN)
   *
   * LazyInitializationException 방지를 위해 category와 images를 함께 로딩
   */
  fun findByIdAndNotDeleted(id: Long): Product? {
    return jpaRepository.findByIdWithDetails(id)
  }

  /**
   * 모든 상품 조회 (연관 엔티티 포함, FETCH JOIN)
   */
  fun findAllNotDeleted(): List<Product> {
    return jpaRepository.findAllWithDetails()
  }

  /**
   * 카테고리별 상품 조회 (연관 엔티티 포함, FETCH JOIN)
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
   * 상품 삭제 (실제로는 사용하지 않음 - Soft Delete 사용)
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