package com.ecommerce.cart.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "carts")
class Cart(
    @Id
    @Column(name = "id")
    val id: Long,

    @Column(nullable = false, unique = true)
    val userId: Long
) : BaseEntity() {

    @OneToMany(
        mappedBy = "cart",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private val _items: MutableList<CartItem> = mutableListOf()

    val items: List<CartItem>
        get() = _items.toList()

    fun addItem(productId: Long, productName: String, price: BigDecimal, quantity: Int) {
        require(quantity > 0) { "수량은 1 이상이어야 합니다" }
        require(price >= BigDecimal.ZERO) { "가격은 0 이상이어야 합니다" }

        val existingItem = _items.find { it.productId == productId && !it.isDeleted() }

        if (existingItem != null) {
            existingItem.updateQuantity(existingItem.quantity + quantity)
        } else {
            val newItem = CartItem(
                cart = this,
                productId = productId,
                productName = productName,
                price = price,
                quantity = quantity
            )
            _items.add(newItem)
        }
    }

    fun updateItemQuantity(itemId: Long, newQuantity: Int) {
        require(newQuantity > 0) { "수량은 1 이상이어야 합니다" }

        val item = _items.find { it.id == itemId && !it.isDeleted() }
            ?: throw IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다: $itemId")

        item.updateQuantity(newQuantity)
    }

    fun removeItem(itemId: Long) {
        val item = _items.find { it.id == itemId && !it.isDeleted() }
            ?: throw IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다: $itemId")

        item.softDelete()
    }

    fun clear() {
        _items.forEach { it.softDelete() }
    }

    fun getTotalPrice(): BigDecimal {
        return _items
            .filter { !it.isDeleted() }
            .map { it.getSubtotal() }
            .fold(BigDecimal.ZERO, BigDecimal::add)
    }

    fun getTotalItemCount(): Int {
        return _items
            .filter { !it.isDeleted() }
            .sumOf { it.quantity }
    }

    fun getActiveItems(): List<CartItem> {
        return _items.filter { !it.isDeleted() }
    }
}
