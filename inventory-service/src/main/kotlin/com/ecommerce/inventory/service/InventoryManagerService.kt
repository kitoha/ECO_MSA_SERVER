package com.ecommerce.inventory.service

import com.ecommerce.inventory.entity.Inventory
import com.ecommerce.inventory.entity.InventoryHistory
import com.ecommerce.inventory.enums.InventoryChangeType
import com.ecommerce.inventory.repository.Inventory.InventoryRepository
import com.ecommerce.inventory.repository.InventoryHistory.InventoryHistoryRepository
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InventoryManagerService(
  private val inventoryRepository: InventoryRepository,
  private val inventoryHistoryRepository: InventoryHistoryRepository
) {

  @Transactional
  @Retryable(
    value = [OptimisticLockingFailureException::class],
    maxAttempts = 5,
    backoff = Backoff(delay = 50)
  )
  fun adjustStock(
    productId: String,
    quantity: Int,
    inventoryChangeType: InventoryChangeType,
    reason: String?,
    referenceId: String? = null
  ) {

    val inventory = inventoryRepository.findByProductId(productId)
      ?: throw IllegalArgumentException("Product not found: $productId")

    val beforeQuantity = inventory.availableQuantity

    when(inventoryChangeType){
      InventoryChangeType.INCREASE -> inventory.increaseStock(quantity)
      InventoryChangeType.DECREASE -> inventory.decreaseStock(quantity)
      else -> throw IllegalArgumentException("Invalid inventory change type: $inventoryChangeType")
    }

    inventoryRepository.save(inventory)

    inventory.id?.let { inventoryId ->
      saveHistory(
        inventoryId = inventoryId,
        changeType = inventoryChangeType,
        quantity = quantity,
        beforeQuantity = beforeQuantity,
        afterQuantity = inventory.availableQuantity,
        reason = reason,
        referenceId = referenceId
      )
    }
  }

  fun saveHistory(
    inventoryId: Long,
    changeType: InventoryChangeType,
    quantity: Int,
    beforeQuantity: Int,
    afterQuantity: Int,
    reason: String?,
    referenceId: String? = null
  ) {
    val history = InventoryHistory(
      inventoryId = inventoryId,
      changeType = changeType,
      quantity = quantity,
      beforeQuantity = beforeQuantity,
      afterQuantity = afterQuantity,
      reason = reason,
      referenceId = referenceId
    )
    inventoryHistoryRepository.save(history)
  }

  @Transactional
  fun reserveStock(
    productId: String,
    quantity: Int,
    reason: String?,
    referenceId: String? = null
  ) {
    val inventory = inventoryRepository.findByProductId(productId)
      ?: throw IllegalArgumentException("Product not found: $productId")

    val beforeQuantity = inventory.availableQuantity

    inventory.reserveStock(quantity)

    inventoryRepository.save(inventory)

    inventory.id?.let { inventoryId ->
      saveHistory(
        inventoryId = inventoryId,
        changeType = InventoryChangeType.RESERVE,
        quantity = quantity,
        beforeQuantity = beforeQuantity,
        afterQuantity = inventory.availableQuantity,
        reason = reason,
        referenceId = referenceId
      )
    }
  }

  @Transactional
  fun releaseReservedStock(
    productId: String,
    quantity: Int,
    reason: String?,
    referenceId: String? = null
  ){
    val inventory = inventoryRepository.findByProductId(productId)
      ?: throw IllegalArgumentException("Product not found: $productId")

    val beforeQuantity = inventory.availableQuantity

    inventory.releaseReservedStock(quantity)

    inventoryRepository.save(inventory)

    inventory.id?.let { inventoryId ->
      saveHistory(
        inventoryId = inventoryId,
        changeType = InventoryChangeType.RELEASE,
        quantity = quantity,
        beforeQuantity = beforeQuantity,
        afterQuantity = inventory.availableQuantity,
        reason = reason,
        referenceId = referenceId
      )
    }
  }
}