package com.ecommerce.product.entity

import com.ecommerce.product.entity.audit.BaseEntity
import com.ecommerce.product.enums.CategoryStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "categories")
class Category (
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(nullable = false, length = 100)
  var name: String,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_category_id")
  var parent: Category? = null,

  @Column(nullable = false)
  var depth: Int = 0,

  @Column(nullable = false, unique = true, length = 200)
  var slug: String,

  @Column(nullable = false)
  var displayOrder: Int = 0,

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  var status: CategoryStatus = CategoryStatus.ACTIVE,
) : BaseEntity() {

  @OneToMany(
    mappedBy = "parent",
    cascade = [CascadeType.ALL],
    orphanRemoval = false
  )
  private val _children: MutableList<Category> = mutableListOf()

  val children: List<Category>
    get() = _children.toList()

  @OneToMany(
    mappedBy = "category",
    cascade = [CascadeType.PERSIST, CascadeType.MERGE]
  )
  private val _products: MutableList<Product> = mutableListOf()

  @Column(name = "parent_category_id", insertable = false, updatable = false)
  var parentCategoryId: Long? = null
    private set

}