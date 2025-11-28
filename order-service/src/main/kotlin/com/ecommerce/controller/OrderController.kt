package com.ecommerce.controller

import com.ecommerce.request.CreateOrderRequest
import com.ecommerce.response.OrderResponse
import com.ecommerce.service.OrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderController(
  private val orderService: OrderService
) {
  @PostMapping
  fun createOrder(@RequestBody request: CreateOrderRequest): OrderResponse {
    return orderService.createOrder(request)
  }

  @GetMapping("/{orderId}")
  fun getOrder(@PathVariable orderId: Long): OrderResponse {
    return orderService.getOrder(orderId)
  }

  @GetMapping("/user/{userId}")
  fun getOrdersByUser(@PathVariable userId: String): List<OrderResponse> {
    return orderService.getOrdersByUser(userId)
  }

  @PostMapping("/{orderId}/cancel")
  fun cancelOrder(@PathVariable orderId: Long) {
    orderService.cancelOrder(orderId)
  }
}
