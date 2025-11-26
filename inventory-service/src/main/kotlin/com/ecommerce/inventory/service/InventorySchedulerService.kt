package com.ecommerce.inventory.service

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class InventorySchedulerService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    companion object {
        private val logger = LoggerFactory.getLogger(InventorySchedulerService::class.java)
        private const val RESERVATION_EXPIRY_KEY = "reservation:expiry"
    }

    @Scheduled(fixedDelay = 10_000)
    @SchedulerLock(  // ShedLock 설정
        name = "checkExpiredReservations",
        lockAtMostFor = "5m",
        lockAtLeastFor = "5s"
    )
    fun checkExpiredReservations() {
        val now = Instant.now().epochSecond.toDouble()

        try {
            // Redis Sorted Set에서 현재 시간보다 이전(만료된) 예약 ID 조회
            val expiredReservationIds = redisTemplate.opsForZSet()
                .rangeByScore(RESERVATION_EXPIRY_KEY, 0.0, now, 0, 100) // 한 번에 최대 100개

            if (expiredReservationIds.isNullOrEmpty()) {
                logger.debug("No expired reservations found")
                return
            }

            logger.info("Found ${expiredReservationIds.size} expired reservations to process")

            expiredReservationIds.forEach { reservationIdStr ->
                try {
                    val reservationId = reservationIdStr.toLong()

                    kafkaTemplate.send(
                        "reservation-cancel",
                        reservationId.toString(),
                        mapOf(
                            "reservationId" to reservationId,
                            "reason" to "EXPIRED"
                        )
                    )

                    redisTemplate.opsForZSet().remove(RESERVATION_EXPIRY_KEY, reservationIdStr)

                    logger.info("Published cancel event for expired reservation: $reservationId")

                } catch (e: Exception) {
                    logger.error("Failed to process expired reservation: $reservationIdStr", e)
                }
            }

        } catch (e: Exception) {
            logger.error("Error checking expired reservations", e)
        }
    }
}
