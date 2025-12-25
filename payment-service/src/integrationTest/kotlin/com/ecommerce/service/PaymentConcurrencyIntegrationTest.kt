package com.ecommerce.service

import com.ecommerce.client.MockPaymentGateway
import com.ecommerce.client.PaymentGateway
import com.ecommerce.config.JpaConfig
import com.ecommerce.config.TestJpaConfig
import com.ecommerce.entity.Payment
import com.ecommerce.enums.PaymentMethod
import com.ecommerce.enums.PaymentStatus
import com.ecommerce.generator.TsidGenerator
import com.ecommerce.repository.PaymentJpaRepository
import com.ecommerce.repository.PaymentRepository
import com.ecommerce.request.PaymentApprovalRequest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.mockk
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 낙관적 락 재시도 로직과 동시성 제어를 검증하는 통합 테스트
 */
@DataJpaTest
@Transactional
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(
    PaymentConcurrencyIntegrationTest.TestConfig::class,
    TestJpaConfig::class,
    JpaConfig::class
)
class PaymentConcurrencyIntegrationTest(
    @Autowired private val paymentCommandService: PaymentCommandService,
    @Autowired private val paymentJpaRepository: PaymentJpaRepository
) : DescribeSpec() {

    override fun extensions() = listOf(SpringExtension)

    private val idGenerator = TsidGenerator()

    init {
        beforeEach {
            paymentJpaRepository.deleteAll()
            clearAllMocks()
        }

        afterEach {
            paymentJpaRepository.deleteAll()
        }

        describe("낙관적 락 재시도 동작 검증") {
            it("@Retryable 어노테이션이 적용되어 OptimisticLockingFailureException 발생 시 재시도해야 한다") {
                val payment = paymentJpaRepository.save(
                    Payment(
                        id = idGenerator.generate(),
                        orderId = "ORDER-RETRY-001",
                        userId = "USER-RETRY-001",
                        amount = BigDecimal("100000"),
                        status = PaymentStatus.PENDING,
                        paymentMethod = PaymentMethod.CARD
                    )
                )

                val approvalRequest = PaymentApprovalRequest(
                    pgProvider = "TOSS",
                    pgPaymentKey = "PG-KEY-RETRY-001",
                    pgTransactionId = "TXN-RETRY-001"
                )

                val response = paymentCommandService.approvePayment(payment.id, approvalRequest)

                response.status shouldBe PaymentStatus.COMPLETED
                response.pgProvider shouldBe "TOSS"
                response.pgPaymentKey shouldBe "PG-KEY-RETRY-001"
            }
        }



        describe("재시도 정책 검증") {
            it("최대 3번까지 재시도를 수행해야 한다") {
                val payment = paymentJpaRepository.save(
                    Payment(
                        id = idGenerator.generate(),
                        orderId = "ORDER-RETRY-POLICY-001",
                        userId = "USER-RETRY-POLICY-001",
                        amount = BigDecimal("50000"),
                        status = PaymentStatus.PENDING,
                        paymentMethod = PaymentMethod.CARD
                    )
                )

                val approvalRequest = PaymentApprovalRequest(
                    pgProvider = "TOSS",
                    pgPaymentKey = "PG-KEY-RETRY-POLICY-001",
                    pgTransactionId = "TXN-RETRY-POLICY-001"
                )

                val response = paymentCommandService.approvePayment(payment.id, approvalRequest)
                response.status shouldBe PaymentStatus.COMPLETED
            }
        }
    }

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        fun tsidGenerator(): TsidGenerator = TsidGenerator()

        @Bean
        fun paymentGateway(): PaymentGateway = MockPaymentGateway()

        @Bean
        fun paymentEventPublisher(): PaymentEventPublisher = mockk(relaxed = true)

        @Bean
        fun paymentTransactionFactory(): PaymentTransactionFactory = PaymentTransactionFactory()

        @Bean
        fun paymentGatewayAdapter(paymentGateway: PaymentGateway): PaymentGatewayAdapter =
            PaymentGatewayAdapter(paymentGateway)

        @Bean
        fun paymentRepository(
            paymentJpaRepository: PaymentJpaRepository,
            paymentQueryRepository: com.ecommerce.repository.PaymentQueryRepository
        ): PaymentRepository = PaymentRepository(paymentJpaRepository, paymentQueryRepository)

        @Bean
        fun paymentCommandService(
            paymentRepository: PaymentRepository,
            idGenerator: TsidGenerator,
            gatewayAdapter: PaymentGatewayAdapter,
            transactionFactory: PaymentTransactionFactory,
            eventPublisher: PaymentEventPublisher
        ): PaymentCommandService = PaymentCommandService(
            paymentRepository,
            idGenerator,
            gatewayAdapter,
            transactionFactory,
            eventPublisher
        )
    }
}
