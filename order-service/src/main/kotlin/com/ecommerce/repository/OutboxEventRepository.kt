package com.ecommerce.repository

import com.ecommerce.entity.OutboxEvent
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class OutboxEventRepository(
    private val outboxEventJpaRepository: OutboxEventJpaRepository
) {

    fun save(outboxEvent: OutboxEvent): OutboxEvent {
        return outboxEventJpaRepository.save(outboxEvent)
    }

    fun countByCreatedAtAfter(after: LocalDateTime): Long {
        return outboxEventJpaRepository.countByCreatedAtAfter(after)
    }

    fun count(): Long {
        return outboxEventJpaRepository.count()
    }
}
