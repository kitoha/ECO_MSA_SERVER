package com.ecommerce.cart.entity

import com.ecommerce.cart.generator.TsidGenerator
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "cart_items")
class CartItem(
    @Id
    @Column(name = "id")
    val id: Long = TsidGenerator.generate(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    val cart: Cart,

    @Column(nullable = false)
    val productId: Long,

    @Column(nullable = false, length = 200)
    var productName: String,

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,

    @Column(nullable = false)
    var quantity: Int
) : BaseEntity() {

    fun updateQuantity(newQuantity: Int) {
        require(newQuantity > 0) { "수량은 1 이상이어야 합니다" }
        this.quantity = newQuantity
    }

    fun updatePrice(newPrice: BigDecimal) {
        require(newPrice >= BigDecimal.ZERO) { "가격은 0 이상이어야 합니다" }
        this.price = newPrice
    }

    fun updateProductInfo(newName: String, newPrice: BigDecimal) {
        this.productName = newName
        updatePrice(newPrice)
    }

    fun getSubtotal(): BigDecimal {
        return price.multiply(BigDecimal(quantity))
    }
}
