package com.ecommerce.exception

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class OrderExceptionTest : BehaviorSpec({

    given("OrderException.OrderNotFoundException가 주어졌을 때") {
        `when`("orderId로 예외를 생성하면") {
            val exception = OrderException.OrderNotFoundException(100L)

            then("올바른 메시지를 가져야 한다") {
                exception.message shouldBe "주문을 찾을 수 없습니다. orderId=100"
            }

            then("OrderException의 하위 클래스여야 한다") {
                exception.shouldBeInstanceOf<OrderException>()
            }

            then("RuntimeException의 하위 클래스여야 한다") {
                exception.shouldBeInstanceOf<RuntimeException>()
            }
        }
    }

    given("OrderException.OrderNotFoundByNumberException가 주어졌을 때") {
        `when`("orderNumber로 예외를 생성하면") {
            val exception = OrderException.OrderNotFoundByNumberException("ORD-20240101-001")

            then("올바른 메시지를 가져야 한다") {
                exception.message shouldBe "주문을 찾을 수 없습니다. orderNumber=ORD-20240101-001"
            }

            then("OrderException의 하위 클래스여야 한다") {
                exception.shouldBeInstanceOf<OrderException>()
            }
        }
    }

    given("OrderException.InvalidOrderStatusTransitionException가 주어졌을 때") {
        `when`("from, to 상태로 예외를 생성하면") {
            val exception = OrderException.InvalidOrderStatusTransitionException("PENDING", "DELIVERED")

            then("올바른 메시지를 가져야 한다") {
                exception.message shouldBe "주문 상태를 PENDING 에서 DELIVERED 로 변경할 수 없습니다"
            }

            then("OrderException의 하위 클래스여야 한다") {
                exception.shouldBeInstanceOf<OrderException>()
            }
        }
    }

    given("OrderException.OrderNotCancellableException가 주어졌을 때") {
        `when`("orderNumber와 status로 예외를 생성하면") {
            val exception = OrderException.OrderNotCancellableException("ORD-001", "SHIPPED")

            then("올바른 메시지를 가져야 한다") {
                exception.message shouldBe "취소 가능한 상태가 아닙니다. orderNumber=ORD-001, status=SHIPPED"
            }

            then("OrderException의 하위 클래스여야 한다") {
                exception.shouldBeInstanceOf<OrderException>()
            }
        }
    }

    given("OrderException.OrderItemNotFoundException가 주어졌을 때") {
        `when`("itemId로 예외를 생성하면") {
            val exception = OrderException.OrderItemNotFoundException(1L)

            then("올바른 메시지를 가져야 한다") {
                exception.message shouldBe "주문 항목을 찾을 수 없습니다. itemId=1"
            }

            then("OrderException의 하위 클래스여야 한다") {
                exception.shouldBeInstanceOf<OrderException>()
            }
        }
    }

    given("OrderException.InvalidQuantityException가 주어졌을 때") {
        `when`("음수 수량으로 예외를 생성하면") {
            val exception = OrderException.InvalidQuantityException(-1)

            then("올바른 메시지를 가져야 한다") {
                exception.message shouldBe "유효하지 않은 수량입니다: -1"
            }

            then("OrderException의 하위 클래스여야 한다") {
                exception.shouldBeInstanceOf<OrderException>()
            }
        }

        `when`("0 수량으로 예외를 생성하면") {
            val exception = OrderException.InvalidQuantityException(0)

            then("올바른 메시지를 가져야 한다") {
                exception.message shouldBe "유효하지 않은 수량입니다: 0"
            }
        }
    }

    given("OrderException.ProductNotAvailableException가 주어졌을 때") {
        `when`("productId로 예외를 생성하면") {
            val exception = OrderException.ProductNotAvailableException("PROD-001")

            then("올바른 메시지를 가져야 한다") {
                exception.message shouldBe "상품을 사용할 수 없습니다. productId=PROD-001"
            }

            then("OrderException의 하위 클래스여야 한다") {
                exception.shouldBeInstanceOf<OrderException>()
            }
        }
    }

    given("OrderException.InsufficientStockException가 주어졌을 때") {
        `when`("productId, requested, available로 예외를 생성하면") {
            val exception = OrderException.InsufficientStockException("PROD-001", 10, 5)

            then("올바른 메시지를 가져야 한다") {
                exception.message shouldBe "재고가 부족합니다. productId=PROD-001, 요청=10, 가용=5"
            }

            then("OrderException의 하위 클래스여야 한다") {
                exception.shouldBeInstanceOf<OrderException>()
            }
        }

        `when`("가용 재고가 0인 경우") {
            val exception = OrderException.InsufficientStockException("PROD-002", 5, 0)

            then("올바른 메시지를 가져야 한다") {
                exception.message shouldBe "재고가 부족합니다. productId=PROD-002, 요청=5, 가용=0"
            }
        }
    }

    given("OrderException.OrderAlreadyExistsException가 주어졌을 때") {
        `when`("orderNumber로 예외를 생성하면") {
            val exception = OrderException.OrderAlreadyExistsException("ORD-001")

            then("올바른 메시지를 가져야 한다") {
                exception.message shouldBe "이미 존재하는 주문번호입니다. orderNumber=ORD-001"
            }

            then("OrderException의 하위 클래스여야 한다") {
                exception.shouldBeInstanceOf<OrderException>()
            }
        }
    }

    given("OrderException.PaymentRequiredException가 주어졌을 때") {
        `when`("orderNumber로 예외를 생성하면") {
            val exception = OrderException.PaymentRequiredException("ORD-001")

            then("올바른 메시지를 가져야 한다") {
                exception.message shouldBe "결제가 필요합니다. orderNumber=ORD-001"
            }

            then("OrderException의 하위 클래스여야 한다") {
                exception.shouldBeInstanceOf<OrderException>()
            }
        }
    }
})
