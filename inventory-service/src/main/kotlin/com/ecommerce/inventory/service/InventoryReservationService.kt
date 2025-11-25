package com.ecommerce.inventory.service

import com.ecommerce.inventory.repository.Inventory.InventoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InventoryReservationService(
  private val inventoryRepository: InventoryRepository,
  private val inventoryHistoryService: InventoryHistoryService
) {

  @Transactional
  fun createReservation(orderId: String, productId: String, quantity: Int) {
    // TODO: 구현 필요
    // 1. productId로 inventory 조회
    // 2. InventoryReservation 생성
    // 3. inventory.reserveStock() 호출
    // 4. 예약 정보 저장
  }

  @Transactional
  fun confirmReservation(reservationId: Long) {
    // TODO: 구현 필요
    // 1. 예약 조회
    // 2. 상태를 COMPLETED로 변경
    // 3. reserved_quantity -> total_quantity 차감
  }

  @Transactional
  fun cancelReservation(reservationId: Long) {
    // TODO: 구현 필요
    // 1. 예약 조회
    // 2. 상태를 CANCELLED로 변경
    // 3. inventory.releaseReservedStock() 호출
  }
}