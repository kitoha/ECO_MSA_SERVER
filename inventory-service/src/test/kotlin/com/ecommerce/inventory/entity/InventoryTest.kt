package com.ecommerce.inventory.entity

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class InventoryTest : BehaviorSpec({

    lateinit var inventory: Inventory

    beforeEach {
        inventory = Inventory(
            id = 1L,
            productId = "PRODUCT-001",
            availableQuantity = 100,
            reservedQuantity = 0,
            totalQuantity = 100
        )
    }

    given("Inventory 엔티티가 주어졌을 때") {

        `when`("재고를 증가시킬 때") {
            then("정상적으로 재고가 증가해야 한다") {
                inventory.increaseStock(50)

                inventory.availableQuantity shouldBe 150
                inventory.totalQuantity shouldBe 150
                inventory.reservedQuantity shouldBe 0
            }

            then("증가 수량이 0 이하이면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    inventory.increaseStock(0)
                }
                exception.message shouldBe "증가 수량은 양수여야 합니다."
            }

            then("음수로 증가 시도하면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    inventory.increaseStock(-10)
                }
                exception.message shouldBe "증가 수량은 양수여야 합니다."
            }
        }

        `when`("재고를 감소시킬 때") {
            then("정상적으로 재고가 감소해야 한다") {
                inventory.decreaseStock(30)

                inventory.availableQuantity shouldBe 70
                inventory.totalQuantity shouldBe 70
                inventory.reservedQuantity shouldBe 0
            }

            then("감소 수량이 0 이하이면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    inventory.decreaseStock(0)
                }
                exception.message shouldBe "감소 수량은 양수여야 합니다."
            }

            then("사용 가능한 재고보다 많이 감소시키면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    inventory.decreaseStock(150)
                }
                exception.message shouldBe "사용 가능한 재고가 부족합니다."
            }

            then("음수로 감소 시도하면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    inventory.decreaseStock(-10)
                }
                exception.message shouldBe "감소 수량은 양수여야 합니다."
            }
        }

        `when`("재고를 예약할 때") {
            then("정상적으로 재고가 예약되어야 한다") {
                inventory.reserveStock(40)

                inventory.availableQuantity shouldBe 60
                inventory.reservedQuantity shouldBe 40
                inventory.totalQuantity shouldBe 100
            }

            then("예약 수량이 0 이하이면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    inventory.reserveStock(0)
                }
                exception.message shouldBe "예약 수량은 양수여야 합니다."
            }

            then("사용 가능한 재고보다 많이 예약하면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    inventory.reserveStock(150)
                }
                exception.message shouldBe "사용 가능한 재고가 부족합니다."
            }

            then("여러 번 예약할 수 있어야 한다") {
                inventory.reserveStock(30)
                inventory.reserveStock(20)

                inventory.availableQuantity shouldBe 50
                inventory.reservedQuantity shouldBe 50
                inventory.totalQuantity shouldBe 100
            }
        }

        `when`("예약된 재고를 해제할 때") {
            beforeEach {
                inventory.reserveStock(40)
            }

            then("정상적으로 예약이 해제되어야 한다") {
                inventory.releaseReservedStock(20)

                inventory.availableQuantity shouldBe 80
                inventory.reservedQuantity shouldBe 20
                inventory.totalQuantity shouldBe 100
            }

            then("해제 수량이 0 이하이면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    inventory.releaseReservedStock(0)
                }
                exception.message shouldBe "해제 수량은 양수여야 합니다."
            }

            then("예약된 재고보다 많이 해제하면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    inventory.releaseReservedStock(50)
                }
                exception.message shouldBe "예약된 재고가 부족합니다."
            }

            then("모든 예약을 해제할 수 있어야 한다") {
                inventory.releaseReservedStock(40)

                inventory.availableQuantity shouldBe 100
                inventory.reservedQuantity shouldBe 0
                inventory.totalQuantity shouldBe 100
            }
        }

        `when`("예약된 재고를 확정할 때") {
            beforeEach {
                inventory.reserveStock(40)
            }

            then("정상적으로 예약이 확정되어야 한다") {
                inventory.confirmReservedStock(20)

                inventory.availableQuantity shouldBe 60
                inventory.reservedQuantity shouldBe 20
                inventory.totalQuantity shouldBe 80
            }

            then("확정 수량이 0 이하이면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    inventory.confirmReservedStock(0)
                }
                exception.message shouldBe "확정 수량은 양수여야 합니다."
            }

            then("예약된 재고보다 많이 확정하면 예외가 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    inventory.confirmReservedStock(50)
                }
                exception.message shouldBe "예약된 재고가 부족합니다."
            }

            then("모든 예약을 확정할 수 있어야 한다") {
                inventory.confirmReservedStock(40)

                inventory.availableQuantity shouldBe 60
                inventory.reservedQuantity shouldBe 0
                inventory.totalQuantity shouldBe 60
            }
        }

        `when`("복합적인 재고 작업을 수행할 때") {
            then("입고 -> 예약 -> 확정 시나리오가 정상 동작해야 한다") {
                // 초기 상태: available=100, reserved=0, total=100
                inventory.increaseStock(50)
                // available=150, reserved=0, total=150

                inventory.reserveStock(60)
                // available=90, reserved=60, total=150

                inventory.confirmReservedStock(30)
                // available=90, reserved=30, total=120

                inventory.availableQuantity shouldBe 90
                inventory.reservedQuantity shouldBe 30
                inventory.totalQuantity shouldBe 120
            }

            then("예약 -> 해제 -> 재예약 시나리오가 정상 동작해야 한다") {
                inventory.reserveStock(50)
                // available=50, reserved=50, total=100

                inventory.releaseReservedStock(20)
                // available=70, reserved=30, total=100

                inventory.reserveStock(20)
                // available=50, reserved=50, total=100

                inventory.availableQuantity shouldBe 50
                inventory.reservedQuantity shouldBe 50
                inventory.totalQuantity shouldBe 100
            }
        }
    }
})
