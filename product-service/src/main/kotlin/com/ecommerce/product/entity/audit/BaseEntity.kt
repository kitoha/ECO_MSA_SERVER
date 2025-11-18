package com.ecommerce.product.entity.audit

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity (
  @CreatedDate
  @Column(nullable = false, updatable = false)
  var createdAt: LocalDateTime? = null,

  @LastModifiedDate
  @Column(nullable = false)
  var updatedAt: LocalDateTime? = null,

  @Column(nullable = false)
  var deleted: Boolean = false,

  @Column
  var deletedAt: LocalDateTime? = null
){
  fun softDelete() {
    this.deleted = true
    this.deletedAt = LocalDateTime.now()
  }

  fun restore() {
    this.deleted = false
    this.deletedAt = null
  }
}