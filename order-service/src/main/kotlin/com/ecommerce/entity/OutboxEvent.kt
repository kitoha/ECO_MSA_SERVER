package com.ecommerce.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "outbox_events")
class OutboxEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "aggregate_type", nullable = false, length = 255)
    val aggregateType: String,

    @Column(name = "aggregate_id", nullable = false, length = 255)
    val aggregateId: String,

    @Column(name = "event_type", nullable = false, length = 255)
    val eventType: String,

    @Column(nullable = false, length = 255)
    val topic: String,

    @Column(name = "kafka_key", length = 255)
    val kafkaKey: String? = null,

    @Lob
    @Column(nullable = false)
    val payload: ByteArray,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        const val AGGREGATE_TYPE_ORDER = "ORDER"
    }
}
