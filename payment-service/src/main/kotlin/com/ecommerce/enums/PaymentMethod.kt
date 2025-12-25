package com.ecommerce.enums

/**
 * 결제 수단
 */
enum class PaymentMethod {
  CARD, // 신용/체크카드
  BANK_TRANSFER, // 계좌이체
  VIRTUAL_ACCOUNT, // 가상계좌
  EASY_PAY, //간편결제 (카카오페이, 네이버페이 등)
  MOBILE //휴대폰 소액결제
}
