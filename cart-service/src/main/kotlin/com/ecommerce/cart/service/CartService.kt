package com.ecommerce.cart.service

import com.ecommerce.cart.client.ProductClient
import com.ecommerce.cart.entity.Cart
import com.ecommerce.cart.exception.CartException
import com.ecommerce.cart.generator.TsidGenerator
import com.ecommerce.cart.repository.CartItemRepository
import com.ecommerce.cart.repository.CartRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CartService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val productClient: ProductClient,
    private val idGenerator: TsidGenerator
) {

    private val logger = LoggerFactory.getLogger(CartService::class.java)

    /**
     * 사용자의 장바구니 조회
     */
    @Transactional
    fun getOrCreateCart(userId: Long): Cart {
        return cartRepository.findByUserIdWithItems(userId)
            .orElseGet {
                logger.info("Creating new cart for user: {}", userId)
                val newCart = Cart(
                    id = idGenerator.generate(),
                    userId = userId
                )
                cartRepository.save(newCart)
            }
    }

    /**
     * 장바구니에 상품 추가
     */
    @Transactional
    fun addItemToCart(userId: Long, productId: Long, quantity: Int): Cart {
        require(quantity > 0) { "수량은 1 이상이어야 합니다" }

        val productIdStr = TsidGenerator.encode(productId)
        val product = productClient.getProductById(productIdStr)
            ?: throw CartException.ProductNotAvailableException(productId)

        if (product.status != "ACTIVE") {
            throw CartException.ProductNotAvailableException(productId)
        }

        val cart = getOrCreateCart(userId)

        cart.addItem(
            productId = productId,
            productName = product.name,
            price = product.salePrice,
            quantity = quantity
        )

        val savedCart = cartRepository.save(cart)
        logger.info("Added item to cart. userId={}, productId={}, quantity={}", userId, productId, quantity)

        return savedCart
    }

    /**
     * 장바구니 아이템 수량 변경
     */
    @Transactional
    fun updateItemQuantity(userId: Long, itemId: Long, newQuantity: Int): Cart {
        require(newQuantity > 0) { "수량은 1 이상이어야 합니다" }

        val cart = cartRepository.findByUserIdWithItems(userId)
            .orElseThrow { CartException.CartNotFoundException(userId) }

        cart.updateItemQuantity(itemId, newQuantity)

        val savedCart = cartRepository.save(cart)
        logger.info("Updated item quantity. userId={}, itemId={}, newQuantity={}", userId, itemId, newQuantity)

        return savedCart
    }

    /**
     * 장바구니 아이템 삭제
     */
    @Transactional
    fun removeItemFromCart(userId: Long, itemId: Long): Cart {
        val cart = cartRepository.findByUserIdWithItems(userId)
            .orElseThrow { CartException.CartNotFoundException(userId) }

        cart.removeItem(itemId)

        val savedCart = cartRepository.save(cart)
        logger.info("Removed item from cart. userId={}, itemId={}", userId, itemId)

        return savedCart
    }

    /**
     * 장바구니 비우기
     */
    @Transactional
    fun clearCart(userId: Long): Cart {
        val cart = cartRepository.findByUserIdWithItems(userId)
            .orElseThrow { CartException.CartNotFoundException(userId) }

        cart.clear()

        val savedCart = cartRepository.save(cart)
        logger.info("Cleared cart. userId={}", userId)

        return savedCart
    }

    /**
     * 장바구니 조회
     */
    fun getCart(userId: Long): Cart {
        return cartRepository.findByUserIdWithItems(userId)
            .orElseThrow { CartException.CartNotFoundException(userId) }
    }
}
