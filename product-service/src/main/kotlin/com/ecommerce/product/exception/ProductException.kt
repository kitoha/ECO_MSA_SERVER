package com.ecommerce.product.exception

sealed class ProductException(message: String) : RuntimeException(message)

class ProductNotFoundException(id: Long) : ProductException("상품을 찾을 수 없습니다: $id")

class CategoryNotFoundException(id: Long) : ProductException("카테고리를 찾을 수 없습니다: $id")

class InvalidProductPriceException(message: String) : ProductException(message)

class InvalidProductStatusException(message: String) : ProductException(message)
