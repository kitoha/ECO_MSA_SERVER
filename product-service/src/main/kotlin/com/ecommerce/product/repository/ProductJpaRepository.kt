package com.ecommerce.product.repository

import com.ecommerce.product.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductJpaRepository : JpaRepository<Product, Long> {

  @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.category c
        LEFT JOIN FETCH p.images i
        WHERE p.id = :id AND p.deletedAt IS NULL
    """)
  fun findByIdWithDetails(@Param("id") id: Long): Product?

  @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.category c
        LEFT JOIN FETCH p.images i
        WHERE p.deletedAt IS NULL
        ORDER BY p.createdAt DESC
    """)
  fun findAllWithDetails(): List<Product>

  @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.category c
        LEFT JOIN FETCH p.images i
        WHERE c.id = :categoryId AND p.deletedAt IS NULL
        ORDER BY p.createdAt DESC
    """)
  fun findByCategoryIdWithDetails(@Param("categoryId") categoryId: Long): List<Product>

  @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.id = :id AND p.deletedAt IS NULL")
  fun existsByIdAndNotDeleted(@Param("id") id: Long): Boolean
}