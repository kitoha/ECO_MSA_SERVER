package com.ecommerce.service

import com.ecommerce.client.MockPaymentGateway
import com.ecommerce.client.PaymentGateway
import com.ecommerce.config.JpaConfig
import com.ecommerce.config.TestJpaConfig
import com.ecommerce.entity.Payment
import com.ecommerce.enums.PaymentMethod
import com.ecommerce.enums.PaymentStatus
import com.ecommerce.enums.TransactionType
import com.ecommerce.exception.*
import com.ecommerce.generator.TsidGenerator
import com.ecommerce.repository.PaymentJpaRepository
import com.ecommerce.repository.PaymentRepository
import com.ecommerce.request.CreatePaymentRequest
import com.ecommerce.request.PaymentApprovalRequest
import com.ecommerce.request.PaymentRefundRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal

@DataJpaTest
@Transactional
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(
    PaymentCommandServiceIntegrationTest.TestConfig::class,
    TestJpaConfig::class,
    JpaConfig::class
)
class PaymentCommandServiceIntegrationTest(
    @Autowired private val paymentCommandService: PaymentCommandService,
    @Autowired private val paymentJpaRepository: PaymentJpaRepository,
    @Autowired private val eventPublisher: PaymentEventPublisher
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

        describe("결제 생성") {
            val request = CreatePaymentRequest(
                orderId = "ORDER-001",
                userId = "USER-001",
                amount = BigDecimal("100000"),
                paymentMethod = PaymentMethod.CARD
            )

            it("유효한 요청으로 결제를 생성하면 PENDING 상태로 생성되어야 한다") {
                val response = paymentCommandService.createPayment(request)

                response.shouldNotBeNull()
                response.orderId shouldBe "ORDER-001"
                response.userId shouldBe "USER-001"
                response.amount shouldBe BigDecimal("100000")
                response.status shouldBe PaymentStatus.PENDING
                response.paymentMethod shouldBe PaymentMethod.CARD

                // 이벤트 발행 검증
                verify(atLeast = 1) { eventPublisher.publishPaymentCreated(any()) }
            }

            it("이미 존재하는 주문 ID로 결제를 생성하려고 하면 예외가 발생해야 한다") {
                paymentCommandService.createPayment(request)

                shouldThrow<DuplicateOrderPaymentException> {
                    paymentCommandService.createPayment(request)
                }
            }
        }

        describe("결제 승인") {
            it("PENDING 상태의 결제를 승인하면 COMPLETED 상태로 변경되어야 한다") {
                // 데이터 준비
                val payment = paymentJpaRepository.save(
                    Payment(
                        id = idGenerator.generate(),
                        orderId = "ORDER-002",
                        userId = "USER-002",
                        amount = BigDecimal("50000"),
                        status = PaymentStatus.PENDING,
                        paymentMethod = PaymentMethod.CARD
                    )
                )

                val approvalRequest = PaymentApprovalRequest(
                    pgProvider = "TOSS",
                    pgPaymentKey = "PG-KEY-002",
                    pgTransactionId = "TXN-002"
                )

                // 테스트 실행
                val response = paymentCommandService.approvePayment(payment.id, approvalRequest)

                // 검증
                response.status shouldBe PaymentStatus.COMPLETED
                response.pgProvider shouldBe "TOSS"
                response.pgPaymentKey shouldBe "PG-KEY-002"
                response.approvedAt.shouldNotBeNull()

                val transactions = response.transactions
                transactions.shouldNotBeNull()
                transactions shouldHaveSize 1
                transactions[0].transactionType shouldBe TransactionType.AUTH

                verify(atLeast = 1) { eventPublisher.publishPaymentCompleted(any()) }
            }

            it("이미 완료된 결제를 승인하려고 하면 예외가 발생해야 한다") {
                val payment = paymentJpaRepository.save(
                    Payment(
                        id = idGenerator.generate(),
                        orderId = "ORDER-003",
                        userId = "USER-003",
                        amount = BigDecimal("50000"),
                        status = PaymentStatus.COMPLETED,
                        paymentMethod = PaymentMethod.CARD
                    )
                )

                val approvalRequest = PaymentApprovalRequest(
                    pgProvider = "TOSS",
                    pgPaymentKey = "PG-KEY-003",
                    pgTransactionId = "TXN-003"
                )

                shouldThrow<PaymentAlreadyCompletedException> {
                    paymentCommandService.approvePayment(payment.id, approvalRequest)
                }
            }
        }

        describe("결제 취소") {
            it("PENDING 상태의 결제를 취소하면 CANCELLED 상태로 변경되어야 한다") {
                val payment = paymentJpaRepository.save(
                    Payment(
                        id = idGenerator.generate(),
                        orderId = "ORDER-004",
                        userId = "USER-004",
                        amount = BigDecimal("30000"),
                        status = PaymentStatus.PENDING,
                        paymentMethod = PaymentMethod.CARD
                    )
                )

                val response = paymentCommandService.cancelPayment(payment.id, "사용자 요청")

                response.status shouldBe PaymentStatus.CANCELLED
                response.failureReason shouldBe "사용자 요청"

                val transactions = response.transactions
                transactions.shouldNotBeNull()
                transactions.any { it.transactionType == TransactionType.CANCEL } shouldBe true

                verify(atLeast = 1) { eventPublisher.publishPaymentCancelled(any(), any()) }
            }

            it("PROCESSING 상태의 결제를 취소하면 PG 취소가 호출되고 CANCELLED 상태로 변경되어야 한다") {
                val payment = paymentJpaRepository.save(
                    Payment(
                        id = idGenerator.generate(),
                        orderId = "ORDER-005",
                        userId = "USER-005",
                        amount = BigDecimal("40000"),
                        status = PaymentStatus.PROCESSING,
                        paymentMethod = PaymentMethod.CARD,
                        pgProvider = "TOSS",
                        pgPaymentKey = "PG-KEY-005"
                    )
                )

                val response = paymentCommandService.cancelPayment(payment.id, "PG 오류")

                response.status shouldBe PaymentStatus.CANCELLED
                response.failureReason shouldBe "PG 오류"
            }

            it("이미 취소된 결제를 다시 취소하려고 하면 예외가 발생해야 한다") {
                val payment = paymentJpaRepository.save(
                    Payment(
                        id = idGenerator.generate(),
                        orderId = "ORDER-006",
                        userId = "USER-006",
                        amount = BigDecimal("30000"),
                        status = PaymentStatus.CANCELLED,
                        paymentMethod = PaymentMethod.CARD
                    )
                )

                shouldThrow<PaymentAlreadyCancelledException> {
                    paymentCommandService.cancelPayment(payment.id, "재취소 시도")
                }
            }

            it("완료된 결제를 취소하려고 하면 예외가 발생해야 한다") {
                val payment = paymentJpaRepository.save(
                    Payment(
                        id = idGenerator.generate(),
                        orderId = "ORDER-007",
                        userId = "USER-007",
                        amount = BigDecimal("30000"),
                        status = PaymentStatus.COMPLETED,
                        paymentMethod = PaymentMethod.CARD
                    )
                )

                shouldThrow<InvalidPaymentStateException> {
                    paymentCommandService.cancelPayment(payment.id, "완료 후 취소 시도")
                }
            }
        }

        describe("환불 처리") {
            it("COMPLETED 상태의 결제를 환불하면 REFUNDED 상태로 변경되어야 한다") {
                val payment = paymentJpaRepository.save(
                    Payment(
                        id = idGenerator.generate(),
                        orderId = "ORDER-008",
                        userId = "USER-008",
                        amount = BigDecimal("60000"),
                        status = PaymentStatus.COMPLETED,
                        paymentMethod = PaymentMethod.CARD,
                        pgProvider = "TOSS",
                        pgPaymentKey = "PG-KEY-008"
                    )
                )

                val refundRequest = PaymentRefundRequest(reason = "단순 변심")
                val response = paymentCommandService.refundPayment(payment.id, refundRequest)

                response.status shouldBe PaymentStatus.REFUNDED

                val transactions = response.transactions
                transactions.shouldNotBeNull()
                transactions.any { it.transactionType == TransactionType.REFUND } shouldBe true

                verify(atLeast = 1) { eventPublisher.publishPaymentRefunded(any(), any()) }
            }

            it("PENDING 상태의 결제를 환불하려고 하면 예외가 발생해야 한다") {
                val payment = paymentJpaRepository.save(
                    Payment(
                        id = idGenerator.generate(),
                        orderId = "ORDER-009",
                        userId = "USER-009",
                        amount = BigDecimal("60000"),
                        status = PaymentStatus.PENDING,
                        paymentMethod = PaymentMethod.CARD
                    )
                )

                val refundRequest = PaymentRefundRequest(reason = "환불 시도")

                shouldThrow<PaymentRefundException> {
                    paymentCommandService.refundPayment(payment.id, refundRequest)
                }
            }
        }

        describe("결제 실패 처리") {
            it("결제를 실패 처리하면 FAILED 상태로 변경되어야 한다") {
                val payment = paymentJpaRepository.save(
                    Payment(
                        id = idGenerator.generate(),
                        orderId = "ORDER-010",
                        userId = "USER-010",
                        amount = BigDecimal("20000"),
                        status = PaymentStatus.PROCESSING,
                        paymentMethod = PaymentMethod.CARD
                    )
                )

                val response = paymentCommandService.failPayment(payment.id, "잔액 부족")

                response.status shouldBe PaymentStatus.FAILED
                response.failureReason shouldBe "잔액 부족"

                val transactions = response.transactions
                transactions.shouldNotBeNull()
                transactions shouldHaveSize 1

                verify(atLeast = 1) { eventPublisher.publishPaymentFailed(any(), any()) }
            }
        }

        describe("예외 처리") {
            it("존재하지 않는 결제 ID로 승인하려고 하면 예외가 발생해야 한다") {
                val request = PaymentApprovalRequest(
                    pgProvider = "TOSS",
                    pgPaymentKey = "PG-KEY-999",
                    pgTransactionId = "TXN-999"
                )

                shouldThrow<PaymentNotFoundException> {
                    paymentCommandService.approvePayment(999999L, request)
                }
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
