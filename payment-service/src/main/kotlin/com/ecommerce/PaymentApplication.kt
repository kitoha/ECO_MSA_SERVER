package com.ecommerce

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableRetry
class PaymentApplication

fun main(args: Array<String>) {
    runApplication<PaymentApplication>(*args)
}