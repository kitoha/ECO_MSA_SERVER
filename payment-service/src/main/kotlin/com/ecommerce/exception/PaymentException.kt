package com.ecommerce.exception

sealed class PaymentException(message: String) : RuntimeException(message)

class PaymentNotFoundException(id: Long) : PaymentException("결제 정보를 찾을 수 없습니다: $id")

class PaymentNotFoundByOrderIdException(orderId: String) : PaymentException("주문 ID로 결제 정보를 찾을 수 없습니다: $orderId")

class PaymentNotFoundByPgPaymentKeyException(pgPaymentKey: String) : PaymentException("PG 결제 키로 결제 정보를 찾을 수 없습니다: $pgPaymentKey")

class InvalidPaymentStateException(message: String) : PaymentException(message)

class InvalidPaymentAmountException(message: String) : PaymentException(message)

class PaymentProcessingException(message: String) : PaymentException(message)

class PaymentAlreadyCompletedException(id: Long) : PaymentException("이미 완료된 결제입니다: $id")

class PaymentAlreadyCancelledException(id: Long) : PaymentException("이미 취소된 결제입니다: $id")

class PaymentRefundException(message: String) : PaymentException(message)

class DuplicateOrderPaymentException(orderId: String) : PaymentException("주문 ID에 대한 결제가 이미 존재합니다: $orderId")

class PaymentGatewayException(message: String, cause: Throwable? = null) : PaymentException(message) {
  init {
    if (cause != null) {
      initCause(cause)
    }
  }
}

class PaymentCancellationException(message: String) : PaymentException(message)
