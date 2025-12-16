package com.ecommerce.controller

import com.ecommerce.request.CreatePaymentRequest
import com.ecommerce.request.PaymentApprovalRequest
import com.ecommerce.request.PaymentRefundRequest
import com.ecommerce.response.PaymentResponse
import com.ecommerce.service.PaymentService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
class PaymentController(
  private val paymentService: PaymentService
) {

  @PostMapping
  fun createPayment(@Valid @RequestBody request: CreatePaymentRequest): PaymentResponse {
    return paymentService.createPayment(request)
  }

  @GetMapping("/{paymentId}")
  fun getPayment(@PathVariable paymentId: Long): PaymentResponse {
    return paymentService.getPayment(paymentId)
  }

  @GetMapping("/order/{orderId}")
  fun getPaymentByOrderId(@PathVariable orderId: String): PaymentResponse {
    return paymentService.getPaymentByOrderId(orderId)
  }

  @GetMapping("/user/{userId}")
  fun getPaymentsByUserId(@PathVariable userId: String): List<PaymentResponse> {
    return paymentService.getPaymentsByUserId(userId)
  }

  @PostMapping("/{paymentId}/approve")
  fun approvePayment(
    @PathVariable paymentId: Long,
    @Valid @RequestBody request: PaymentApprovalRequest
  ): PaymentResponse {
    return paymentService.approvePayment(paymentId, request)
  }

  @PostMapping("/{paymentId}/cancel")
  fun cancelPayment(
    @PathVariable paymentId: Long,
    @RequestParam(required = false, defaultValue = "사용자 요청") reason: String
  ): PaymentResponse {
    return paymentService.cancelPayment(paymentId, reason)
  }

  @PostMapping("/{paymentId}/refund")
  fun refundPayment(
    @PathVariable paymentId: Long,
    @Valid @RequestBody request: PaymentRefundRequest
  ): PaymentResponse {
    return paymentService.refundPayment(paymentId, request)
  }
}
