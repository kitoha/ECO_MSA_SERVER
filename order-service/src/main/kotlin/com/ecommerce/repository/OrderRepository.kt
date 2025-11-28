package com.ecommerce.repository

import org.springframework.stereotype.Repository

@Repository
class OrderRepository(
  private val orderJpaRepository: OrderJpaRepository
) {
}