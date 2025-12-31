package com.ecommerce.cart.repository

import com.ecommerce.cart.entity.CartItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CartItemRepository : JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.id = :itemId AND ci.deletedAt IS NULL")
    fun findActiveById(itemId: Long): Optional<CartItem>

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.deletedAt IS NULL")
    fun findActiveItemsByCartId(cartId: Long): List<CartItem>

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productId = :productId AND ci.deletedAt IS NULL")
    fun findByCartIdAndProductId(cartId: Long, productId: Long): Optional<CartItem>
}
