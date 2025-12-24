package com.ecommerce.config

import com.ecommerce.repository.PaymentQueryRepository
import com.ecommerce.repository.PaymentTransactionQueryRepository
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestJpaConfig {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Bean
    fun jpaQueryFactory(): JPAQueryFactory {
        return JPAQueryFactory(entityManager)
    }

    @Bean
    fun paymentQueryRepository(jpaQueryFactory: JPAQueryFactory): PaymentQueryRepository {
        return PaymentQueryRepository(jpaQueryFactory)
    }

    @Bean
    fun paymentTransactionQueryRepository(jpaQueryFactory: JPAQueryFactory): PaymentTransactionQueryRepository {
        return PaymentTransactionQueryRepository(jpaQueryFactory)
    }
}
