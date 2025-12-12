package com.ecommerce.enums

/**
 * 결제 상태
 */
enum class PaymentStatus {
  PENDING, // 결제 대기 - 결제 요청이 생성된 상태
  PROCESSING, // 결제 처리중 - PG사에 결제 요청을 보낸 상태
  COMPLETED, // 결제 완료 - 결제가 성공적으로 완료됨
  FAILED, // 결제 실패 - 결제가 실패함
  CANCELLED, // 결제 취소 - 사용자가 결제를 취소함
  REFUNDED // 환불 완료 - 결제가 환불됨
}
