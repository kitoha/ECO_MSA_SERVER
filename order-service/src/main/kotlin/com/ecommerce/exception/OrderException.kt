package com.ecommerce.exception

sealed class OrderException(message: String) : RuntimeException(message) {
    class OrderNotFoundException(orderId: Long) :
        OrderException("주문을 찾을 수 없습니다. orderId=$orderId")

    class OrderNotFoundByNumberException(orderNumber: String) :
        OrderException("주문을 찾을 수 없습니다. orderNumber=$orderNumber")

    class InvalidOrderStatusTransitionException(from: String, to: String) :
        OrderException("주문 상태를 $from 에서 $to 로 변경할 수 없습니다")

    class OrderNotCancellableException(orderNumber: String, status: String) :
        OrderException("취소 가능한 상태가 아닙니다. orderNumber=$orderNumber, status=$status")

    class OrderItemNotFoundException(itemId: Long) :
        OrderException("주문 항목을 찾을 수 없습니다. itemId=$itemId")

    class InvalidQuantityException(quantity: Int) :
        OrderException("유효하지 않은 수량입니다: $quantity")

    class ProductNotAvailableException(productId: String) :
        OrderException("상품을 사용할 수 없습니다. productId=$productId")

    class InsufficientStockException(productId: String, requested: Int, available: Int) :
        OrderException("재고가 부족합니다. productId=$productId, 요청=$requested, 가용=$available")

    class OrderAlreadyExistsException(orderNumber: String) :
        OrderException("이미 존재하는 주문번호입니다. orderNumber=$orderNumber")

    class PaymentRequiredException(orderNumber: String) :
        OrderException("결제가 필요합니다. orderNumber=$orderNumber")
}
