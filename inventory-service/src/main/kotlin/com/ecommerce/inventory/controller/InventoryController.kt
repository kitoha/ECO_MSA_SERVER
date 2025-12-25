package com.ecommerce.inventory.controller

import com.ecommerce.inventory.entity.InventoryHistory
import com.ecommerce.inventory.request.AdjustStockRequest
import com.ecommerce.inventory.request.ReleaseInventoryRequest
import com.ecommerce.inventory.request.ReserveInventoryRequest
import com.ecommerce.inventory.response.InventoryResponse
import com.ecommerce.inventory.response.ReservationResponse
import com.ecommerce.inventory.service.InventoryHistoryService
import com.ecommerce.inventory.service.InventoryManagerService
import com.ecommerce.inventory.service.InventoryReservationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/*
| `GET` | `/products/{productId}` | 특정 상품의 재고 조회 |
| `GET` | `/products` | 여러 상품의 재고 일괄 조회 |
| `POST` | `/reserve` | 재고 예약 (주문 시) |
| `POST` | `/release` | 재고 예약 해제 (주문 취소 시) |
| `POST` | `/products/{productId}/adjust` | 재고 수량 조정 (입고/출고) |
| `GET` | `/products/{productId}/history` | 재고 변동 이력 조회 |
 */

@RestController
@RequestMapping("/api/v1/inventory")
class InventoryController(
  private val inventoryManagerService: InventoryManagerService,
  private val inventoryReservationService: InventoryReservationService,
  private val inventoryHistoryService: InventoryHistoryService
) {

  /**
   * 특정 상품의 재고 조회
   */
  @GetMapping("/products/{productId}")
  fun getProductInventoryById(@PathVariable productId: String): ResponseEntity<InventoryResponse> {
    val inventory = inventoryManagerService.getInventoryByProductId(productId)
    val response = InventoryResponse(
      id = inventory.id!!,
      productId = inventory.productId,
      availableQuantity = inventory.availableQuantity,
      reservedQuantity = inventory.reservedQuantity,
      totalQuantity = inventory.totalQuantity
    )
    return ResponseEntity.ok(response)
  }

  /**
   * 여러 상품의 재고 일괄 조회
   */
  @GetMapping("/products")
  fun getAllProductInventories(@RequestParam productIds: List<String>): ResponseEntity<List<InventoryResponse>> {
    val inventories = inventoryManagerService.getInventoriesByProductIds(productIds)
    val responses = inventories.map { inventory ->
      InventoryResponse(
        id = inventory.id!!,
        productId = inventory.productId,
        availableQuantity = inventory.availableQuantity,
        reservedQuantity = inventory.reservedQuantity,
        totalQuantity = inventory.totalQuantity
      )
    }
    return ResponseEntity.ok(responses)
  }

  /**
   * 재고 예약 (주문 시)
   */
  @PostMapping("/reserve")
  fun reserveInventory(@Valid @RequestBody request: ReserveInventoryRequest): ResponseEntity<ReservationResponse> {
    val reservation = inventoryReservationService.createReservation(
      orderId = request.orderId,
      productId = request.productId,
      quantity = request.quantity
    )
    val response = ReservationResponse(
      id = reservation.id!!,
      inventoryId = reservation.inventoryId,
      orderId = reservation.orderId,
      quantity = reservation.quantity,
      status = reservation.status,
      expiresAt = reservation.expiresAt,
      createdAt = reservation.createdAt
    )
    return ResponseEntity.status(HttpStatus.CREATED).body(response)
  }

  /**
   * 재고 예약 해제 (주문 취소 시)
   */
  @PostMapping("/release")
  fun releaseInventory(@Valid @RequestBody request: ReleaseInventoryRequest): ResponseEntity<Void> {
    inventoryReservationService.cancelReservation(request.reservationId)
    return ResponseEntity.noContent().build()
  }

  /**
   * 재고 수량 조정 (입고/출고)
   */
  @PostMapping("/products/{productId}/adjust")
  fun adjustInventory(
    @PathVariable productId: String,
    @Valid @RequestBody request: AdjustStockRequest
  ): ResponseEntity<Void> {
    inventoryManagerService.adjustStock(
      productId = productId,
      quantity = request.quantity,
      inventoryChangeType = request.changeType,
      reason = request.reason,
      referenceId = request.referenceId
    )
    return ResponseEntity.ok().build()
  }

  /**
   * 재고 변동 이력 조회
   */
  @GetMapping("/products/{productId}/history")
  fun getInventoryHistory(@PathVariable productId: String): ResponseEntity<List<InventoryHistory>> {
    val inventory = inventoryManagerService.getInventoryByProductId(productId)
    val history = inventoryHistoryService.getHistoryByInventoryId(inventory.id!!)
    return ResponseEntity.ok(history)
  }
}