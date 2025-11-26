package com.ecommerce.inventory.service

import com.ecommerce.inventory.entity.InventoryHistory
import com.ecommerce.inventory.entity.InventoryReservation
import com.ecommerce.inventory.enums.InventoryChangeType
import com.ecommerce.inventory.enums.ReservationStatus
import com.ecommerce.inventory.repository.Inventory.InventoryRepository
import com.ecommerce.inventory.repository.InventoryHistory.InventoryHistoryRepository
import com.ecommerce.inventory.repository.InventoryReservation.InventoryReservationRepository
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class InventoryReservationService(
  private val inventoryRepository: InventoryRepository,
  private val inventoryHistoryRepository: InventoryHistoryRepository,
  private val inventoryReservationRepository: InventoryReservationRepository
) {

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
      return existingReservation
    }

    val beforeAvailable = inventory.availableQuantity
    inventory.reserveStock(quantity)
    inventoryRepository.save(inventory)
    val afterAvailable = inventory.availableQuantity

    val reservation = InventoryReservation(
      inventoryId = inventoryId,
      orderId = orderId,
      quantity = quantity,
      status = ReservationStatus.ACTIVE,
      expiresAt = LocalDateTime.now().plusMinutes(15)
    )
    val savedReservation = inventoryReservationRepository.save(reservation)

    val history = InventoryHistory(
      inventoryId = inventoryId,
      changeType = InventoryChangeType.RESERVE,
      quantity = quantity,
      beforeQuantity = beforeAvailable,
      afterQuantity = afterAvailable,
      reason = "Stock reserved for order $orderId",
      referenceId = savedReservation.id?.toString()
    )
    inventoryHistoryRepository.save(history)

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

    val history = InventoryHistory(
      inventoryId = inventoryId,
      changeType = InventoryChangeType.DECREASE,
      quantity = reservation.quantity,
      beforeQuantity = beforeTotal,
      afterQuantity = afterTotal,
      reason = "Stock reservation confirmed for reservation ${reservation.id}",
      referenceId = reservation.id?.toString()
    )
    inventoryHistoryRepository.save(history)
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

    val history = InventoryHistory(
      inventoryId = inventoryId,
      changeType = InventoryChangeType.RELEASE,
      quantity = reservation.quantity,
      beforeQuantity = beforeAvailable,
      afterQuantity = afterAvailable,
      reason = "Stock reservation cancelled for reservation ${reservation.id}",
      referenceId = reservation.id?.toString()
    )
    inventoryHistoryRepository.save(history)
  }
}