package com.ecommerce.inventory.exception

sealed class InventoryException(message: String) : RuntimeException(message) {
    class InventoryNotFoundException(productId: String) :
        InventoryException("재고 정보를 찾을 수 없습니다. productId=$productId")

    class InventoryNotFoundByIdException(inventoryId: Long) :
        InventoryException("재고 정보를 찾을 수 없습니다. inventoryId=$inventoryId")

    class InsufficientStockException(productId: String, requested: Int, available: Int) :
        InventoryException("사용 가능한 재고가 부족합니다. productId=$productId, 요청=$requested, 가용=$available")

    class InsufficientReservedStockException(productId: String, requested: Int, reserved: Int) :
        InventoryException("예약된 재고가 부족합니다. productId=$productId, 요청=$requested, 예약=$reserved")

    class InvalidQuantityException(quantity: Int) :
        InventoryException("유효하지 않은 수량입니다: $quantity")

    class ReservationNotFoundException(reservationId: Long) :
        InventoryException("예약 정보를 찾을 수 없습니다. reservationId=$reservationId")

    class ReservationExpiredException(reservationId: Long) :
        InventoryException("만료된 예약입니다. reservationId=$reservationId")

    class ReservationAlreadyCompletedException(reservationId: Long) :
        InventoryException("이미 완료된 예약입니다. reservationId=$reservationId")

    class ReservationAlreadyCancelledException(reservationId: Long) :
        InventoryException("이미 취소된 예약입니다. reservationId=$reservationId")

    class InvalidReservationStatusException(reservationId: Long, currentStatus: String, expectedStatus: String) :
        InventoryException("예약 상태가 올바르지 않습니다. reservationId=$reservationId, 현재=$currentStatus, 기대=$expectedStatus")

    class DuplicateReservationException(orderId: String, productId: String) :
        InventoryException("이미 존재하는 예약입니다. orderId=$orderId, productId=$productId")

    class InvalidInventoryChangeTypeException(changeType: String) :
        InventoryException("유효하지 않은 재고 변경 타입입니다: $changeType")
}
