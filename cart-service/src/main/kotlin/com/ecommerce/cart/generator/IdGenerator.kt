package com.ecommerce.cart.generator

interface IdGenerator {
    fun generate(): Long
    fun encode(id: Long): String
    fun decode(tsid: String): Long
}
