package com.ecommerce.repository

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository

interface OrderJpaRepository : JpaRepository<Sort.Order, Long> {
}