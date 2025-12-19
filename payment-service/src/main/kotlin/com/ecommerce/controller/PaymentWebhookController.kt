package com.ecommerce.controller

import com.ecommerce.request.PaymentWebhookRequest
import com.ecommerce.service.PaymentWebhookService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments/webhook")
class PaymentWebhookController(
  private val webhookService: PaymentWebhookService
) {

  private val logger = LoggerFactory.getLogger(PaymentWebhookController::class.java)

  @PostMapping
  fun handleWebhook(
    @RequestHeader("X-Webhook-Signature") signature: String,
    @RequestBody request: PaymentWebhookRequest,
    @RequestBody payload: String
  ): ResponseEntity<Map<String, String>> {
    return try {
      logger.info("Received webhook: eventType=${request.eventType}, orderId=${request.orderId}")

      webhookService.processPaymentWebhook(request, signature, payload)

      ResponseEntity.ok(mapOf("status" to "success"))
    } catch (e: SecurityException) {
      logger.error("Webhook signature verification failed", e)
      ResponseEntity.status(401).body(mapOf("error" to "Invalid signature"))
    } catch (e: Exception) {
      logger.error("Failed to process webhook", e)
      ResponseEntity.status(500).body(mapOf("error" to "Unknown error"))
    }
  }
}
