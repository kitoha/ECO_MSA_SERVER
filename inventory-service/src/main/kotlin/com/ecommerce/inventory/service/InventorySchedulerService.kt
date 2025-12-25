package com.ecommerce.inventory.service

import com.ecommerce.inventory.event.ReservationCancelledEvent
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * 만료된 재고 예약을 주기적으로 확인하고 취소 이벤트를 발행하는 스케줄러 서비스
 */
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
            /**
             * 예약 생성 시 Redis Sorted Set에 저장된 만료된 예약 ID 조회
             */
            val expiredReservationIds = redisTemplate.opsForZSet()
                .rangeByScore(RESERVATION_EXPIRY_KEY, 0.0, now, 0, 100)

            if (expiredReservationIds.isNullOrEmpty()) {
                logger.debug("No expired reservations found")
                return
            }

            logger.info("Found ${expiredReservationIds.size} expired reservations to process")

            /**
             * 만료된 예약 ID에 대해 취소 이벤트 발행 및 Redis에서 제거 (100개씩 처리)
             * 주문을 했지만 결제가 이루어지지 않은 예약 건에 대해 재고를 해제하기 위함
             */
            expiredReservationIds.forEach { reservationIdStr ->
                try {
                    val reservationId = reservationIdStr.toLong()

                    val cancelEvent = ReservationCancelledEvent(
                        reservationId = reservationId,
                        reason = "EXPIRED"
                    )

                    kafkaTemplate.send(
                        "reservation-cancel",
                        reservationId.toString(),
                        cancelEvent
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
