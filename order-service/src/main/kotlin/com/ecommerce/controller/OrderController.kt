package com.ecommerce.controller

import com.ecommerce.order.security.AuthConstants
import com.ecommerce.request.CreateOrderRequest
import com.ecommerce.response.OrderResponse
import com.ecommerce.service.OrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderController(
  private val orderService: OrderService
) {
  @PostMapping
  fun createOrder(
    @RequestHeader(AuthConstants.USER_ID_HEADER) userId: String,
    @RequestBody request: CreateOrderRequest
  ): OrderResponse {
    return orderService.createOrder(request, userId)
  }

  @GetMapping("/{orderId}")
  fun getOrder(@PathVariable orderId: Long): OrderResponse {
    return orderService.getOrder(orderId)
  }

  @GetMapping("/my")
  fun getOrdersByUser(@RequestHeader(AuthConstants.USER_ID_HEADER) userId: String): List<OrderResponse> {
    return orderService.getOrdersByUser(userId)
  }

  @PostMapping("/{orderId}/cancel")
  fun cancelOrder(@PathVariable orderId: Long) {
    orderService.cancelOrder(orderId)
  }
}
