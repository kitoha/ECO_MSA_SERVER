package com.ecommerce.product.controller

import com.ecommerce.product.dto.ProductResponse
import com.ecommerce.product.dto.ProductSearchRequest
import com.ecommerce.product.dto.RegisterProductRequest
import com.ecommerce.product.dto.UpdateProductRequest
import com.ecommerce.product.service.ProductService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
  private val productService: ProductService
) {

  @PostMapping
  fun registerProduct(
    @RequestBody request: RegisterProductRequest
  ): ResponseEntity<ProductResponse> {
    val response = productService.registerProduct(request)
    return ResponseEntity.status(HttpStatus.CREATED).body(response)
  }

  @GetMapping("/{id}")
  fun getProduct(@PathVariable id: Long): ResponseEntity<ProductResponse> {
    val response = productService.getProduct(id)
    return ResponseEntity.ok(response)
  }

  @GetMapping
  fun getAllProducts(): ResponseEntity<List<ProductResponse>> {
    val response = productService.getAllProducts()
    return ResponseEntity.ok(response)
  }

  @PostMapping("/search")
  fun searchProducts(
    @RequestBody request: ProductSearchRequest
  ): ResponseEntity<List<ProductResponse>> {
    val response = productService.searchProducts(request)
    return ResponseEntity.ok(response)
  }

  @PutMapping("/{id}")
  fun updateProduct(
    @PathVariable id: Long,
    @RequestBody request: UpdateProductRequest
  ): ResponseEntity<ProductResponse> {
    val response = productService.updateProduct(id, request)
    return ResponseEntity.ok(response)
  }

  @DeleteMapping("/{id}")
  fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
    productService.deleteProduct(id)
    return ResponseEntity.noContent().build()
  }
}
