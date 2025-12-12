package com.ecommerce.enums

/**
 * 결제 트랜잭션 타입
 */
enum class TransactionType {
  AUTH, // 인증 - 결제 수단 인증 (카드 유효성 확인)
  CAPTURE, // 승인 - 실제 결제 승인 및 금액 차감
  CANCEL, // 취소 - 결제 취소
  REFUND // 환불 - 결제 환불
}
