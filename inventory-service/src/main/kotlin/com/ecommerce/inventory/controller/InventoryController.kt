package com.ecommerce.inventory.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

/*
| `GET` | `/products/{productId}` | 특정 상품의 재고 조회 |
| `GET` | `/products` | 여러 상품의 재고 일괄 조회 |
| `POST` | `/reserve` | 재고 예약 (주문 시) |
| `POST` | `/release` | 재고 예약 해제 (주문 취소 시) |
| `PUT` | `/products/{productId}` | 재고 수량 조정 |
| `GET` | `/products/{productId}/history` | 재고 변동 이력 조회 |
 */

@RestController
class InventoryController {

  @GetMapping("/products/{productId}")
  fun getProductInventoryById() : String {
    return "Get product inventory"
  }

  @GetMapping("/products")
  fun getAllProductInventories() : String {
    return "Get all product inventories"
  }

  @PostMapping("/reserve")
  fun reserveInventory() : String {
    return "Reserve inventory"
  }

  @PostMapping("/release")
  fun releaseInventory() : String {
    return "Release inventory"
  }

  @PostMapping("/products/{productId}")
  fun adjustInventory() : String {
    return "Adjust inventory"
  }

  @GetMapping("/products/{productId}/history")
  fun getInventoryHistory() : String {
    return "Get inventory history"
  }
}