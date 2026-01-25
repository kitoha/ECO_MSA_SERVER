package com.ecommerce.cart.controller

import com.ecommerce.cart.dto.AddItemRequest
import com.ecommerce.cart.dto.CartResponse
import com.ecommerce.cart.dto.UpdateQuantityRequest
import com.ecommerce.cart.service.CartService
import com.ecommerce.cart.security.AuthConstants
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/carts")
class CartController(
    private val cartService: CartService
) {

    /**
     * 장바구니 조회
     */
    @GetMapping
    fun getCart(
        @RequestHeader(AuthConstants.USER_ID_HEADER) userId: Long
    ): ResponseEntity<CartResponse> {
        val cart = cartService.getOrCreateCart(userId)
        return ResponseEntity.ok(CartResponse.from(cart))
    }

    /**
     * 장바구니에 상품 추가
     */
    @PostMapping("/items")
    fun addItemToCart(
        @RequestHeader(AuthConstants.USER_ID_HEADER) userId: Long,
        @RequestBody request: AddItemRequest
    ): ResponseEntity<CartResponse> {
        val cart = cartService.addItemToCart(userId, request.productId, request.quantity)
        return ResponseEntity.ok(CartResponse.from(cart))
    }

    /**
     * 장바구니 아이템 수량 변경
     */
    @PutMapping("/items/{itemId}")
    fun updateItemQuantity(
        @RequestHeader(AuthConstants.USER_ID_HEADER) userId: Long,
        @PathVariable itemId: Long,
        @RequestBody request: UpdateQuantityRequest
    ): ResponseEntity<CartResponse> {
        val cart = cartService.updateItemQuantity(userId, itemId, request.quantity)
        return ResponseEntity.ok(CartResponse.from(cart))
    }

    /**
     * 장바구니 아이템 삭제
     */
    @DeleteMapping("/items/{itemId}")
    fun removeItemFromCart(
        @RequestHeader(AuthConstants.USER_ID_HEADER) userId: Long,
        @PathVariable itemId: Long
    ): ResponseEntity<CartResponse> {
        val cart = cartService.removeItemFromCart(userId, itemId)
        return ResponseEntity.ok(CartResponse.from(cart))
    }

    /**
     * 장바구니 비우기
     */
    @DeleteMapping
    fun clearCart(
        @RequestHeader(AuthConstants.USER_ID_HEADER) userId: Long
    ): ResponseEntity<CartResponse> {
        val cart = cartService.clearCart(userId)
        return ResponseEntity.ok(CartResponse.from(cart))
    }
}