package com.ecommerce.product.controller

import com.ecommerce.product.dto.CategoryProductStats
import com.ecommerce.product.service.ProductService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/categories")
class CategoryController(
  private val productService: ProductService
) {

  @GetMapping("/stats")
  fun getProductStatsByCategory(): ResponseEntity<List<CategoryProductStats>> {
    val response = productService.getProductStatsByCategory()
    return ResponseEntity.ok(response)
  }
}
