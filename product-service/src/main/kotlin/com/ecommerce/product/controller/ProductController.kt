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
  fun getProduct(@PathVariable id: String): ResponseEntity<ProductResponse> {
    val response = productService.getProduct(id)
    return ResponseEntity.ok(response)
  }


  @GetMapping("/batch")
  fun getProductsByIds(
    @RequestParam ids: List<String>
  ): ResponseEntity<List<ProductResponse>> {
    require(ids.isNotEmpty()) { "상품 ID는 최소 1개 이상 필요합니다" }
    require(ids.size <= 100) {
      "상품 ID는 최대 100개까지 조회 가능합니다. 요청: ${ids.size}개. " +
      "더 많은 데이터가 필요한 경우 여러 번 나눠서 요청해주세요."
    }

    val response = productService.getProductsByIds(ids)
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
    @PathVariable id: String,
    @RequestBody request: UpdateProductRequest
  ): ResponseEntity<ProductResponse> {
    val response = productService.updateProduct(id, request)
    return ResponseEntity.ok(response)
  }

  @DeleteMapping("/{id}")
  fun deleteProduct(@PathVariable id: String): ResponseEntity<Void> {
    productService.deleteProduct(id)
    return ResponseEntity.noContent().build()
  }
}
