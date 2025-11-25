package com.ecommerce.inventory.repository.Inventory

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class InventoryQueryRepository(
  private val queryFactory: JPAQueryFactory
) {
}