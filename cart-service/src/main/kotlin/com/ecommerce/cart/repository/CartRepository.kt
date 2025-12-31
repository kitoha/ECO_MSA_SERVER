package com.ecommerce.cart.repository

import com.ecommerce.cart.entity.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CartRepository : JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c._items WHERE c.userId = :userId AND c.deletedAt IS NULL")
    fun findByUserIdWithItems(userId: Long): Optional<Cart>

    @Query("SELECT c FROM Cart c WHERE c.userId = :userId AND c.deletedAt IS NULL")
    fun findByUserId(userId: Long): Optional<Cart>

    fun existsByUserIdAndDeletedAtIsNull(userId: Long): Boolean
}
