package com.ecommerce.cart.exception

sealed class CartException(message: String) : RuntimeException(message) {
    class CartNotFoundException(userId: Long) :
        CartException("장바구니를 찾을 수 없습니다. userId=$userId")

    class CartItemNotFoundException(itemId: Long) :
        CartException("장바구니 아이템을 찾을 수 없습니다. itemId=$itemId")

    class InvalidQuantityException(quantity: Int) :
        CartException("유효하지 않은 수량입니다: $quantity")

    class ProductNotAvailableException(productId: Long) :
        CartException("상품을 사용할 수 없습니다. productId=$productId")

    class CartAlreadyExistsException(userId: Long) :
        CartException("이미 장바구니가 존재합니다. userId=$userId")
}
