package com.ecommerce.inventory.service

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.kafka.core.KafkaTemplate

class InventorySchedulerServiceTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val redisTemplate = mockk<RedisTemplate<String, String>>()
    val kafkaTemplate = mockk<KafkaTemplate<String, Any>>()
    val zSetOperations = mockk<ZSetOperations<String, String>>()

    val inventorySchedulerService = InventorySchedulerService(redisTemplate, kafkaTemplate)

    beforeEach {
        clearMocks(redisTemplate, kafkaTemplate, zSetOperations, answers = false)
        every { redisTemplate.opsForZSet() } returns zSetOperations
    }

    given("InventorySchedulerService의 checkExpiredReservations 메서드가 주어졌을 때") {
        `when`("만료된 예약이 있으면") {
            val expiredReservationIds = setOf("1", "2", "3")

            every {
                zSetOperations.rangeByScore(
                    "reservation:expiry",
                    0.0,
                    any(),
                    0,
                    100
                )
            } returns expiredReservationIds

            every { kafkaTemplate.send(any(), any(), any()) } returns mockk()
            every { zSetOperations.remove(any(), any<String>()) } returns 1L

            then("각 예약에 대해 취소 이벤트를 발행하고 Redis에서 제거해야 한다") {
                inventorySchedulerService.checkExpiredReservations()

                verify(exactly = 3) {
                    kafkaTemplate.send(
                        "reservation-cancel",
                        any(),
                        match<Map<String, Any>> { payload ->
                            payload["reason"] as? String == "EXPIRED" &&
                            (payload["reservationId"] as? Long == 1L ||
                             payload["reservationId"] as? Long == 2L ||
                             payload["reservationId"] as? Long == 3L)
                        }
                    )
                }

                verify(exactly = 1) { zSetOperations.remove("reservation:expiry", "1") }
                verify(exactly = 1) { zSetOperations.remove("reservation:expiry", "2") }
                verify(exactly = 1) { zSetOperations.remove("reservation:expiry", "3") }
            }
        }

        `when`("만료된 예약이 없으면") {
            every {
                zSetOperations.rangeByScore(
                    "reservation:expiry",
                    0.0,
                    any(),
                    0,
                    100
                )
            } returns emptySet()

            then("아무 작업도 수행하지 않아야 한다") {
                inventorySchedulerService.checkExpiredReservations()

                verify(exactly = 0) { kafkaTemplate.send(any(), any(), any()) }
                verify(exactly = 0) { zSetOperations.remove(any(), any<String>()) }
            }
        }

        `when`("만료된 예약이 null이면") {
            every {
                zSetOperations.rangeByScore(
                    "reservation:expiry",
                    0.0,
                    any(),
                    0,
                    100
                )
            } returns null

            then("아무 작업도 수행하지 않아야 한다") {
                inventorySchedulerService.checkExpiredReservations()

                verify(exactly = 0) { kafkaTemplate.send(any(), any(), any()) }
                verify(exactly = 0) { zSetOperations.remove(any(), any<String>()) }
            }
        }

        `when`("특정 예약 처리 중 예외가 발생하면") {
            val expiredReservationIds = setOf("1", "invalid", "3")

            every {
                zSetOperations.rangeByScore(
                    "reservation:expiry",
                    0.0,
                    any(),
                    0,
                    100
                )
            } returns expiredReservationIds

            every { kafkaTemplate.send(any(), any(), any()) } returns mockk()
            every { zSetOperations.remove(any(), any<String>()) } returns 1L

            then("다른 예약들은 정상적으로 처리되어야 한다") {
                inventorySchedulerService.checkExpiredReservations()

                verify(exactly = 1) {
                    kafkaTemplate.send(
                        "reservation-cancel",
                        "1",
                        match<Map<String, Any>> { payload ->
                            payload["reservationId"] as? Long == 1L
                        }
                    )
                }

                verify(exactly = 1) {
                    kafkaTemplate.send(
                        "reservation-cancel",
                        "3",
                        match<Map<String, Any>> { payload ->
                            payload["reservationId"] as? Long == 3L
                        }
                    )
                }

                verify(exactly = 0) {
                    kafkaTemplate.send(
                        "reservation-cancel",
                        "invalid",
                        any()
                    )
                }

                verify(exactly = 1) { zSetOperations.remove("reservation:expiry", "1") }
                verify(exactly = 1) { zSetOperations.remove("reservation:expiry", "3") }
            }
        }

        `when`("단일 예약을 처리하면") {
            val expiredReservationIds = setOf("10")

            every {
                zSetOperations.rangeByScore(
                    "reservation:expiry",
                    0.0,
                    any(),
                    0,
                    100
                )
            } returns expiredReservationIds

            every { kafkaTemplate.send(any(), any(), any()) } returns mockk()
            every { zSetOperations.remove(any(), any<String>()) } returns 1L

            then("정확한 reservationId로 이벤트를 발행해야 한다") {
                inventorySchedulerService.checkExpiredReservations()

                verify(exactly = 1) {
                    kafkaTemplate.send(
                        "reservation-cancel",
                        "10",
                        match<Map<String, Any>> { payload ->
                            payload["reservationId"] as? Long == 10L &&
                            payload["reason"] as? String == "EXPIRED"
                        }
                    )
                }

                verify(exactly = 1) { zSetOperations.remove("reservation:expiry", "10") }
            }
        }
    }
})
