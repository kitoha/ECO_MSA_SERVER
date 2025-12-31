package com.ecommerce.cart.controller

import com.ecommerce.cart.dto.AddItemRequest
import com.ecommerce.cart.dto.CartResponse
import com.ecommerce.cart.dto.UpdateQuantityRequest
import com.ecommerce.cart.service.CartService
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
    @GetMapping("/{userId}")
    fun getCart(@PathVariable userId: Long): ResponseEntity<CartResponse> {
        val cart = cartService.getOrCreateCart(userId)
        return ResponseEntity.ok(CartResponse.from(cart))
    }

    /**
     * 장바구니에 상품 추가
     */
    @PostMapping("/{userId}/items")
    fun addItemToCart(
        @PathVariable userId: Long,
        @RequestBody request: AddItemRequest
    ): ResponseEntity<CartResponse> {
        val cart = cartService.addItemToCart(userId, request.productId, request.quantity)
        return ResponseEntity.ok(CartResponse.from(cart))
    }

    /**
     * 장바구니 아이템 수량 변경
     */
    @PutMapping("/{userId}/items/{itemId}")
    fun updateItemQuantity(
        @PathVariable userId: Long,
        @PathVariable itemId: Long,
        @RequestBody request: UpdateQuantityRequest
    ): ResponseEntity<CartResponse> {
        val cart = cartService.updateItemQuantity(userId, itemId, request.quantity)
        return ResponseEntity.ok(CartResponse.from(cart))
    }

    /**
     * 장바구니 아이템 삭제
     */
    @DeleteMapping("/{userId}/items/{itemId}")
    fun removeItemFromCart(
        @PathVariable userId: Long,
        @PathVariable itemId: Long
    ): ResponseEntity<CartResponse> {
        val cart = cartService.removeItemFromCart(userId, itemId)
        return ResponseEntity.ok(CartResponse.from(cart))
    }

    /**
     * 장바구니 비우기
     */
    @DeleteMapping("/{userId}")
    fun clearCart(@PathVariable userId: Long): ResponseEntity<CartResponse> {
        val cart = cartService.clearCart(userId)
        return ResponseEntity.ok(CartResponse.from(cart))
    }
}
