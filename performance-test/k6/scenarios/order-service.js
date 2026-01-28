import http from 'k6/http';
import { group } from 'k6';
import {
    BASE_URL,
    checkDetailedResponse,
    parseJsonResponse,
    randomInt,
    thinkTime,
    logError,
    defaultHeaders,
    createPaginationParams,
} from '../utils/common.js';
import { defaultThresholds, scenarioThresholds } from '../config/thresholds.js';
import { basicLoad } from '../config/stages.js';

/**
 * Order Service 부하 테스트
 *
 * 테스트 항목:
 * 1. 주문 생성 (가장 중요한 트랜잭션)
 * 2. 주문 목록 조회
 * 3. 주문 상세 조회
 * 4. 주문 취소
 * 5. 주문 상태별 조회
 */

export const options = {
    stages: basicLoad,
    thresholds: {
        ...defaultThresholds,
        ...scenarioThresholds.orderCreate,
    },
};

const PRODUCT_ID_RANGE = { min: 1, max: 100 };
const USER_ID_RANGE = { min: 1, max: 50 };

export function setup() {
    console.log('Starting Order Service Load Test');
    console.log(`Base URL: ${BASE_URL}`);

    const healthCheck = http.get(`${BASE_URL}/actuator/health`);
    if (healthCheck.status !== 200) {
        throw new Error('Order Service is not healthy!');
    }

    return { startTime: new Date().toISOString() };
}

export default function () {
    const userId = randomInt(USER_ID_RANGE.min, USER_ID_RANGE.max);
    const rand = Math.random();

    if (rand < 0.4) {
        // 40%: 주문 생성 (가장 중요)
        scenario_createOrder(userId);
    } else if (rand < 0.7) {
        // 30%: 주문 목록 조회
        scenario_listOrders(userId);
    } else if (rand < 0.9) {
        // 20%: 주문 상세 조회
        const orders = scenario_listOrders(userId);
        if (orders && orders.length > 0) {
            const orderId = orders[0].id;
            scenario_getOrderDetail(orderId);
        }
    } else {
        // 10%: 주문 생성 후 취소
        const orderId = scenario_createOrder(userId);
        if (orderId) {
            scenario_cancelOrder(orderId);
        }
    }

    thinkTime(1, 3);
}

/**
 * 주문 생성 - 가장 중요한 트랜잭션
 */
function scenario_createOrder(userId) {
    return group('Create Order', function () {
        // 주문 아이템 생성 (1-3개)
        const itemCount = randomInt(1, 3);
        const orderItems = [];

        for (let i = 0; i < itemCount; i++) {
            orderItems.push({
                productId: randomInt(PRODUCT_ID_RANGE.min, PRODUCT_ID_RANGE.max),
                quantity: randomInt(1, 5),
                price: randomInt(10000, 100000), // KRW
            });
        }

        const headers = { ...defaultHeaders, 'X-User-Id': userId.toString() };

        const payload = {
            orderItems: orderItems,
            shippingAddress: {
                recipientName: `User ${userId}`,
                phoneNumber: '010-1234-5678',
                postalCode: '12345',
                address: 'Seoul, Korea',
                detailAddress: 'Apt 101',
            },
        };

        const response = http.post(
            `${BASE_URL}/api/v1/orders`,
            JSON.stringify(payload),
            {
                headers: headers,
                timeout: '30s', // 주문 생성은 시간이 걸릴 수 있음
            }
        );

        const passed = checkDetailedResponse(response, {
            'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
            'has order id': (r) => {
                const body = parseJsonResponse(r);
                return body && body.id;
            },
            'has order status': (r) => {
                const body = parseJsonResponse(r);
                return body && body.status;
            },
            'response time < 2000ms': (r) => r.timings.duration < 2000, // 주문은 더 여유있게
        });

        if (!passed) {
            logError(`Create order for user ${userId} failed`, response);
            return null;
        }

        const body = parseJsonResponse(response);
        return body ? body.id : null;
    });
}

/**
 * 주문 목록 조회
 */
function scenario_listOrders(userId) {
    return group('List Orders', function () {
        const headers = { ...defaultHeaders, 'X-User-Id': userId.toString() };

        const response = http.get(
            `${BASE_URL}/api/v1/orders/my`,
            { headers: headers }
        );

        const passed = checkDetailedResponse(response, {
            'status is 200': (r) => r.status === 200,
            'has orders array': (r) => {
                const body = parseJsonResponse(r);
                return body && Array.isArray(body);
            },
            'response time < 400ms': (r) => r.timings.duration < 400,
        });

        if (!passed) {
            logError(`List orders for user ${userId} failed`, response);
            return null;
        }

        const body = parseJsonResponse(response);
        return body;
    });
}

/**
 * 주문 상세 조회
 */
function scenario_getOrderDetail(orderId) {
    return group('Get Order Detail', function () {
        const response = http.get(
            `${BASE_URL}/api/v1/orders/${orderId}`,
            { headers: defaultHeaders }
        );

        const passed = checkDetailedResponse(response, {
            'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
            'has order details': (r) => {
                if (r.status === 200) {
                    const body = parseJsonResponse(r);
                    return body && body.id && body.orderItems;
                }
                return true;
            },
            'response time < 300ms': (r) => r.timings.duration < 300,
        });

        if (!passed && response.status !== 404) {
            logError(`Get order ${orderId} failed`, response);
        }
    });
}

/**
 * 주문 취소
 */
function scenario_cancelOrder(orderId) {
    return group('Cancel Order', function () {
        const response = http.post(
            `${BASE_URL}/api/v1/orders/${orderId}/cancel`,
            null,
            { headers: defaultHeaders }
        );

        const passed = checkDetailedResponse(response, {
            'status is 200 or 400': (r) => r.status === 200 || r.status === 400,
            'response time < 1000ms': (r) => r.timings.duration < 1000,
        });

        if (!passed && response.status !== 400) {
            logError(`Cancel order ${orderId} failed`, response);
        }
    });
}



export function teardown(data) {
    console.log('✅ Order Service Load Test Completed');
    console.log(`Started at: ${data.startTime}`);
    console.log(`Ended at: ${new Date().toISOString()}`);
}
