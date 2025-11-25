package com.ecommerce.inventory

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.Retryable

@Retryable
@SpringBootApplication
class InventoryApplication

fun main(args: Array<String>) {
  runApplication<InventoryApplication>(*args)
}