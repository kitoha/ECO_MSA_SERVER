package com.ecommerce.repository

import com.ecommerce.config.JpaConfig
import com.ecommerce.config.TestJpaConfig
import com.ecommerce.entity.Payment
import com.ecommerce.enums.PaymentMethod
import com.ecommerce.enums.PaymentStatus
import com.ecommerce.generator.TsidGenerator
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.LocalDateTime

@DataJpaTest
@Transactional
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(TestJpaConfig::class, JpaConfig::class)
class PaymentQueryRepositoryIntegrationTest(
    @Autowired private val paymentJpaRepository: PaymentJpaRepository,
    @Autowired private val paymentQueryRepository: PaymentQueryRepository
) : BehaviorSpec({

    extensions(SpringExtension)

    val idGenerator = TsidGenerator()

    given("실제 DB에 결제 데이터가 저장되어 있을 때") {
        lateinit var payment1: Payment
        lateinit var payment2: Payment
        lateinit var payment3: Payment

        beforeEach {
            // 매번 DB 정리
            paymentJpaRepository.deleteAll()

            // 테스트 데이터 생성
            payment1 = paymentJpaRepository.save(
                Payment(
                    id = idGenerator.generate(),
                    orderId = "ORDER-001",
                    userId = "USER-001",
                    amount = BigDecimal("100000"),
                    status = PaymentStatus.COMPLETED,
                    paymentMethod = PaymentMethod.CARD,
                    pgProvider = "TOSS",
                    pgPaymentKey = "PG-KEY-001"
                )
            )

            payment2 = paymentJpaRepository.save(
                Payment(
                    id = idGenerator.generate(),
                    orderId = "ORDER-002",
                    userId = "USER-001",
                    amount = BigDecimal("50000"),
                    status = PaymentStatus.PENDING,
                    paymentMethod = PaymentMethod.BANK_TRANSFER
                )
            )

            payment3 = paymentJpaRepository.save(
                Payment(
                    id = idGenerator.generate(),
                    orderId = "ORDER-003",
                    userId = "USER-002",
                    amount = BigDecimal("75000"),
                    status = PaymentStatus.COMPLETED,
                    paymentMethod = PaymentMethod.EASY_PAY,
                    pgProvider = "TOSS",
                    pgPaymentKey = "PG-KEY-003"
                )
            )
        }

        afterEach {
            paymentJpaRepository.deleteAll()
        }

        `when`("userId로 결제를 조회하면") {
            then("해당 사용자의 모든 결제가 조회되어야 한다") {
                val result = paymentQueryRepository.findByUserId("USER-001")

                result shouldHaveSize 2
                result[0].id shouldBe payment2.id // 최신순 정렬
                result[1].id shouldBe payment1.id
            }
        }

        `when`("status로 결제를 조회하면") {
            then("해당 상태의 모든 결제가 조회되어야 한다") {
                val result = paymentQueryRepository.findByStatus(PaymentStatus.COMPLETED)

                result shouldHaveSize 2
                result.map { it.id } shouldBe listOf(payment3.id, payment1.id) // 최신순
            }
        }

        `when`("userId와 status로 결제를 조회하면") {
            then("조건에 맞는 결제만 조회되어야 한다") {
                val result = paymentQueryRepository.findByUserIdAndStatus("USER-001", PaymentStatus.COMPLETED)

                result shouldHaveSize 1
                result[0].id shouldBe payment1.id
            }
        }

        `when`("생성 날짜 범위로 결제를 조회하면") {
            val startDate = LocalDateTime.now().minusDays(1)
            val endDate = LocalDateTime.now().plusDays(1)

            then("해당 기간의 모든 결제가 조회되어야 한다") {
                val result = paymentQueryRepository.findByCreatedAtBetween(startDate, endDate)

                result shouldHaveSize 3
            }
        }

        `when`("userId와 생성 날짜 범위로 결제를 조회하면") {
            val startDate = LocalDateTime.now().minusDays(1)
            val endDate = LocalDateTime.now().plusDays(1)

            then("조건에 맞는 결제만 조회되어야 한다") {
                val result = paymentQueryRepository.findByUserIdAndCreatedAtBetween(
                    "USER-001",
                    startDate,
                    endDate
                )

                result shouldHaveSize 2
                result.map { it.id } shouldBe listOf(payment2.id, payment1.id)
            }
        }

        `when`("동적 검색을 수행하면") {
            then("모든 조건이 null일 때 전체 결제가 조회되어야 한다") {
                val result = paymentQueryRepository.searchPayments(
                    userId = null,
                    status = null,
                    startDate = null,
                    endDate = null
                )

                result shouldHaveSize 3
            }

            then("userId만 지정하면 해당 사용자의 결제만 조회되어야 한다") {
                val result = paymentQueryRepository.searchPayments(
                    userId = "USER-001",
                    status = null,
                    startDate = null,
                    endDate = null
                )

                result shouldHaveSize 2
            }

            then("userId와 status를 지정하면 조건에 맞는 결제만 조회되어야 한다") {
                val result = paymentQueryRepository.searchPayments(
                    userId = "USER-001",
                    status = PaymentStatus.PENDING,
                    startDate = null,
                    endDate = null
                )

                result shouldHaveSize 1
                result[0].id shouldBe payment2.id
            }

            then("모든 조건을 지정하면 모든 조건에 맞는 결제만 조회되어야 한다") {
                val startDate = LocalDateTime.now().minusDays(1)
                val endDate = LocalDateTime.now().plusDays(1)

                val result = paymentQueryRepository.searchPayments(
                    userId = "USER-001",
                    status = PaymentStatus.COMPLETED,
                    startDate = startDate,
                    endDate = endDate
                )

                result shouldHaveSize 1
                result[0].id shouldBe payment1.id
            }
        }

        `when`("삭제된 결제가 있을 때") {
            lateinit var deletedPayment: Payment

            beforeEach {
                deletedPayment = paymentJpaRepository.save(
                    Payment(
                        id = idGenerator.generate(),
                        orderId = "ORDER-004",
                        userId = "USER-001",
                        amount = BigDecimal("30000"),
                        status = PaymentStatus.CANCELLED,
                        paymentMethod = PaymentMethod.CARD
                    ).apply {
                        deletedAt = LocalDateTime.now()
                    }
                )
            }

            then("삭제된 결제는 조회되지 않아야 한다") {
                val result = paymentQueryRepository.findByUserId("USER-001")

                result shouldHaveSize 2
                result.none { it.id == deletedPayment.id } shouldBe true
            }
        }
    }
}) {
    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
    }
}
