package com.ecommerce.inventory.service

import com.ecommerce.inventory.entity.InventoryReservation
import com.ecommerce.inventory.enums.InventoryChangeType
import com.ecommerce.inventory.enums.ReservationStatus

import com.ecommerce.inventory.repository.Inventory.InventoryRepository
import com.ecommerce.inventory.repository.InventoryReservation.InventoryReservationRepository
import com.ecommerce.proto.inventory.ReservationCancelledEvent
import com.ecommerce.proto.inventory.ReservationConfirmedEvent
import com.ecommerce.proto.inventory.ReservationCreatedEvent
import com.google.protobuf.Message
import com.google.protobuf.Timestamp
import org.slf4j.LoggerFactory
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 재고 예약 서비스(재고 예약 생성, 확정, 취소 처리) - 결제 전 재고 확보 목적
 */
@Service
class InventoryReservationService(
  private val inventoryRepository: InventoryRepository,
  private val inventoryReservationRepository: InventoryReservationRepository,
  private val inventoryHistoryService: InventoryHistoryService,
  private val redisTemplate: RedisTemplate<String, String>,
  private val kafkaTemplate: KafkaTemplate<String, Message>
) {

  companion object {
    private val logger = LoggerFactory.getLogger(InventoryReservationService::class.java)
    private const val RESERVATION_EXPIRY_KEY = "reservation:expiry"
  }

  @Transactional
  @Retryable(
    value = [OptimisticLockingFailureException::class],
    maxAttempts = 5,
    backoff = Backoff(delay = 50)
  )
  fun createReservation(orderId: String, productId: String, quantity: Int): InventoryReservation {
    val inventory = inventoryRepository.findByProductId(productId)
      ?: throw IllegalArgumentException("Product not found: $productId")

    val inventoryId = inventory.id
      ?: throw IllegalStateException("Inventory must be persisted before creating reservation")

    val existingReservation = inventoryReservationRepository.findByOrderIdAndInventoryId(orderId, inventoryId)
    if (existingReservation != null && existingReservation.isActive()) {
      logger.info("Returning existing active reservation for order: $orderId")
      return existingReservation
    }

    val beforeAvailable = inventory.availableQuantity
    inventory.reserveStock(quantity)
    inventoryRepository.save(inventory)
    val afterAvailable = inventory.availableQuantity

    val expiresAt = LocalDateTime.now().plusMinutes(15)
    val reservation = InventoryReservation(
      inventoryId = inventoryId,
      orderId = orderId,
      quantity = quantity,
      status = ReservationStatus.ACTIVE,
      expiresAt = expiresAt
    )
    val savedReservation = inventoryReservationRepository.save(reservation)

    inventoryHistoryService.recordChange(
      inventoryId = inventoryId,
      changeType = InventoryChangeType.RESERVE,
      quantity = quantity,
      beforeQuantity = beforeAvailable,
      afterQuantity = afterAvailable,
      reason = "Stock reserved for order $orderId",
      referenceId = savedReservation.id?.toString()
    )

    val expiryScore = expiresAt.atZone(ZoneId.systemDefault()).toInstant().epochSecond.toDouble()
    redisTemplate.opsForZSet().add(RESERVATION_EXPIRY_KEY, savedReservation.id.toString(), expiryScore)

    val now = java.time.Instant.now()
    val event = ReservationCreatedEvent.newBuilder()
      .setReservationId(savedReservation.id!!)
      .setOrderId(orderId)
      .setProductId(productId)
      .setQuantity(quantity)
      .setExpiresAt(Timestamp.newBuilder()
        .setSeconds(expiresAt.atZone(ZoneId.systemDefault()).toInstant().epochSecond)
        .build())
      .setTimestamp(Timestamp.newBuilder()
        .setSeconds(now.epochSecond)
        .setNanos(now.nano)
        .build())
      .build()
    kafkaTemplate.send("reservation-created", orderId, event)

    logger.info("Created reservation: id=${savedReservation.id}, orderId=$orderId, quantity=$quantity, expiresAt=$expiresAt")

    return savedReservation
  }

  @Transactional
  @Retryable(
    value = [OptimisticLockingFailureException::class],
    maxAttempts = 5,
    backoff = Backoff(delay = 50)
  )
  fun confirmReservation(reservationId: Long) {
    val reservation = inventoryReservationRepository.findById(reservationId)
    val inventory = inventoryRepository.findById(reservation.inventoryId)

    val inventoryId = inventory.id
      ?: throw IllegalStateException("Inventory must be persisted")

    reservation.markCompleted()
    inventoryReservationRepository.save(reservation)

    val beforeTotal = inventory.totalQuantity
    inventory.confirmReservedStock(reservation.quantity)
    inventoryRepository.save(inventory)
    val afterTotal = inventory.totalQuantity

    inventoryHistoryService.recordChange(
      inventoryId = inventoryId,
      changeType = InventoryChangeType.DECREASE,
      quantity = reservation.quantity,
      beforeQuantity = beforeTotal,
      afterQuantity = afterTotal,
      reason = "Stock reservation confirmed for reservation ${reservation.id}",
      referenceId = reservation.id?.toString()
    )

    redisTemplate.opsForZSet().remove(RESERVATION_EXPIRY_KEY, reservationId.toString())

    val now = java.time.Instant.now()
    val event = ReservationConfirmedEvent.newBuilder()
      .setReservationId(reservationId)
      .setOrderId(reservation.orderId)
      .setTimestamp(Timestamp.newBuilder()
        .setSeconds(now.epochSecond)
        .setNanos(now.nano)
        .build())
      .build()
    kafkaTemplate.send("reservation-confirmed", reservation.orderId, event)

    logger.info("Confirmed reservation: id=$reservationId, orderId=${reservation.orderId}")
  }

  @Transactional
  @Retryable(
    value = [OptimisticLockingFailureException::class],
    maxAttempts = 5,
    backoff = Backoff(delay = 50)
  )
  fun cancelReservation(reservationId: Long) {
    val reservation = inventoryReservationRepository.findById(reservationId)
    val inventory = inventoryRepository.findById(reservation.inventoryId)

    val inventoryId = inventory.id
      ?: throw IllegalStateException("Inventory must be persisted")

    reservation.markCancelled()
    inventoryReservationRepository.save(reservation)

    val beforeAvailable = inventory.availableQuantity
    inventory.releaseReservedStock(reservation.quantity)
    inventoryRepository.save(inventory)
    val afterAvailable = inventory.availableQuantity

    inventoryHistoryService.recordChange(
      inventoryId = inventoryId,
      changeType = InventoryChangeType.RELEASE,
      quantity = reservation.quantity,
      beforeQuantity = beforeAvailable,
      afterQuantity = afterAvailable,
      reason = "Stock reservation cancelled for reservation ${reservation.id}",
      referenceId = reservation.id?.toString()
    )

    redisTemplate.opsForZSet().remove(RESERVATION_EXPIRY_KEY, reservationId.toString())

    val now = java.time.Instant.now()
    val event = ReservationCancelledEvent.newBuilder()
      .setReservationId(reservationId)
      .setReason("MANUAL_CANCEL")
      .setTimestamp(Timestamp.newBuilder()
        .setSeconds(now.epochSecond)
        .setNanos(now.nano)
        .build())
      .build()
    kafkaTemplate.send("reservation-cancelled", reservation.orderId, event)

    logger.info("Cancelled reservation: id=$reservationId, orderId=${reservation.orderId}")
  }


  /**
   * 주문 ID로 모든 예약을 확정합니다.
   */
  @Transactional
  fun confirmReservationsByOrderId(orderId: String) {
    val reservations = inventoryReservationRepository.findActiveReservationsByOrderId(orderId)
    
    if (reservations.isEmpty()) {
      logger.warn("No active reservations found for order: $orderId")
      return
    }

    reservations.forEach { reservation ->
      reservation.id?.let { confirmReservation(it) }
    }

    logger.info("Confirmed ${reservations.size} reservations for order: $orderId")
  }

  /**
   * 주문 ID로 모든 예약을 취소합니다.
   */
  @Transactional
  fun cancelReservationsByOrderId(orderId: String) {
    val reservations = inventoryReservationRepository.findActiveReservationsByOrderId(orderId)
    
    if (reservations.isEmpty()) {
      logger.warn("No active reservations found for order: $orderId")
      return
    }

    reservations.forEach { reservation ->
      reservation.id?.let { cancelReservation(it) }
    }

    logger.info("Cancelled ${reservations.size} reservations for order: $orderId")
  }
}