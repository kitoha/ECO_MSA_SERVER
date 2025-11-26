package com.ecommerce.inventory.service

import com.ecommerce.inventory.entity.InventoryHistory
import com.ecommerce.inventory.enums.InventoryChangeType
import com.ecommerce.inventory.repository.InventoryHistory.InventoryHistoryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class InventoryHistoryService(
    private val inventoryHistoryRepository: InventoryHistoryRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(InventoryHistoryService::class.java)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun recordChange(
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

        logger.info(
            "Recorded inventory history: inventoryId=$inventoryId, " +
                    "changeType=$changeType, quantity=$quantity, " +
                    "before=$beforeQuantity, after=$afterQuantity, " +
                    "reason=$reason, referenceId=$referenceId"
        )
    }

    fun getHistoryByInventoryId(inventoryId: Long): List<InventoryHistory> {
        return inventoryHistoryRepository.findByInventoryId(inventoryId)
    }

    fun getHistoryByReferenceId(referenceId: String): List<InventoryHistory> {
        return inventoryHistoryRepository.findByReferenceId(referenceId)
    }
}
