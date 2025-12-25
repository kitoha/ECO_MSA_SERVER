package com.ecommerce.repository

import com.ecommerce.entity.PaymentTransaction
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentTransactionJpaRepository : JpaRepository<PaymentTransaction, Long>
