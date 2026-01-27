package com.ecommerce.repository

import com.ecommerce.entity.OutboxEvent
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface OutboxEventJpaRepository : JpaRepository<OutboxEvent, Long> {
    fun countByCreatedAtAfter(after: LocalDateTime): Long
}
