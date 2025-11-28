package com.ecommerce.repository

import org.springframework.stereotype.Repository

@Repository
class OrderItemRepository(
  private val orderItemJpaRepository: OrderItemJpaRepository
) {
}