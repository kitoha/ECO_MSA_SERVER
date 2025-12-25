package com.ecommerce.inventory.service

import com.ecommerce.inventory.enums.InventoryChangeType
import com.ecommerce.inventory.repository.Inventory.InventoryRepository
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 재고 관리 서비스 (입고 및 출고 처리)
 */
@Service
class InventoryManagerService(
  private val inventoryRepository: InventoryRepository,
  private val inventoryHistoryService: InventoryHistoryService
) {

  /**
   * 재고 조회 (단일 상품)
   */
  fun getInventoryByProductId(productId: String) =
    inventoryRepository.findByProductId(productId)
      ?: throw IllegalArgumentException("Product not found: $productId")

  /**
   * 재고 조회 (여러 상품)
   */
  fun getInventoriesByProductIds(productIds: List<String>) =
    inventoryRepository.findByProductIdIn(productIds)

  /**
   * 재고 조정 함수
   */
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

    inventoryHistoryService.recordChange(
      inventoryId = inventory.id!!,
      changeType = inventoryChangeType,
      quantity = quantity,
      beforeQuantity = beforeQuantity,
      afterQuantity = inventory.availableQuantity,
      reason = reason,
      referenceId = referenceId
    )
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

    inventoryHistoryService.recordChange(
      inventoryId = inventory.id!!,
      changeType = InventoryChangeType.RESERVE,
      quantity = quantity,
      beforeQuantity = beforeQuantity,
      afterQuantity = inventory.availableQuantity,
      reason = reason,
      referenceId = referenceId
    )
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

    inventoryHistoryService.recordChange(
      inventoryId = inventory.id!!,
      changeType = InventoryChangeType.RELEASE,
      quantity = quantity,
      beforeQuantity = beforeQuantity,
      afterQuantity = inventory.availableQuantity,
      reason = reason,
      referenceId = referenceId
    )
  }
}