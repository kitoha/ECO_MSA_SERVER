package com.ecommerce.product.repository

import com.ecommerce.product.entity.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CategoryRepository : JpaRepository<Category, Long> {

  @Query("""
        SELECT c FROM Category c
        LEFT JOIN FETCH c.parent p
        WHERE c.id = :id AND c.deletedAt IS NULL
    """)
  fun findByIdAndNotDeleted(@Param("id") id: Long): Category?

  @Query("""
        SELECT c FROM Category c
        WHERE c.parent.id = :parentId AND c.deletedAt IS NULL
        ORDER BY c.displayOrder ASC, c.name ASC
    """)
  fun findByParentId(@Param("parentId") parentId: Long): List<Category>

  @Query("""
        SELECT c FROM Category c
        WHERE c.parent IS NULL AND c.deletedAt IS NULL
        ORDER BY c.displayOrder ASC, c.name ASC
    """)
  fun findRootCategories(): List<Category>

  @Query("""
        SELECT c FROM Category c
        LEFT JOIN FETCH c.parent p
        WHERE c.slug = :slug AND c.deletedAt IS NULL
    """)
  fun findBySlug(@Param("slug") slug: String): Category?

  @Query("""
        SELECT c FROM Category c
        LEFT JOIN FETCH c.parent p
        WHERE c.deletedAt IS NULL
        ORDER BY c.parent.id ASC NULLS FIRST, c.displayOrder ASC, c.name ASC
    """)
  fun findAllNotDeleted(): List<Category>

  @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.id = :id AND c.deletedAt IS NULL")
  fun existsByIdAndNotDeleted(@Param("id") id: Long): Boolean
}