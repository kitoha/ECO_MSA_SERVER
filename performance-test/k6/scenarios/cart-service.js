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
    generateRandomUser,
} from '../utils/common.js';
import { defaultThresholds, scenarioThresholds } from '../config/thresholds.js';
import { basicLoad } from '../config/stages.js';

/**
 * Cart Service 부하 테스트
 *
 * 테스트 항목:
 * 1. 장바구니 조회
 * 2. 장바구니 아이템 추가
 * 3. 장바구니 아이템 수량 변경
 * 4. 장바구니 아이템 삭제
 * 5. 장바구니 전체 비우기
 */

export const options = {
    stages: basicLoad,
    thresholds: {
        ...defaultThresholds,
        ...scenarioThresholds.cart,
    },
};

// 테스트 데이터
const PRODUCT_ID_RANGE = { min: 1, max: 100 };
const USER_ID_RANGE = { min: 1, max: 50 };

export function setup() {
    console.log('Starting Cart Service Load Test');
    console.log(`Base URL: ${BASE_URL}`);

    const healthCheck = http.get(`${BASE_URL}/actuator/health`);
    if (healthCheck.status !== 200) {
        throw new Error('Cart Service is not healthy!');
    }

    return { startTime: new Date().toISOString() };
}

export default function () {
    const userId = randomInt(USER_ID_RANGE.min, USER_ID_RANGE.max);

    // 시나리오 분배
    const rand = Math.random();

    if (rand < 0.3) {
        // 30%: 장바구니 조회만
        scenario_viewCart(userId);
    } else if (rand < 0.6) {
        // 30%: 조회 + 아이템 추가
        scenario_viewCart(userId);
        scenario_addItemToCart(userId);
    } else if (rand < 0.85) {
        // 25%: 조회 + 추가 + 수량 변경
        scenario_viewCart(userId);
        const itemId = scenario_addItemToCart(userId);
        if (itemId) {
            scenario_updateCartItem(userId, itemId);
        }
    } else {
        // 15%: 전체 플로우 (추가 + 변경 + 삭제)
        scenario_viewCart(userId);
        const itemId = scenario_addItemToCart(userId);
        if (itemId) {
            scenario_updateCartItem(userId, itemId);
            scenario_removeCartItem(userId, itemId);
        }
    }

    thinkTime(1, 2);
}

/**
 * 장바구니 조회
 */
function scenario_viewCart(userId) {
    return group('View Cart', function () {
        const headers = { ...defaultHeaders, 'X-User-Id': userId.toString() };
        const response = http.get(
            `${BASE_URL}/api/v1/carts`,
            { headers: headers }
        );

        const passed = checkDetailedResponse(response, {
            'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
            'response time < 300ms': (r) => r.timings.duration < 300,
        });

        if (!passed && response.status !== 404) {
            logError(`View cart for user ${userId} failed`, response);
        }

        return response.status === 200 ? parseJsonResponse(response) : null;
    });
}

/**
 * 장바구니에 아이템 추가
 */
function scenario_addItemToCart(userId) {
    return group('Add Item to Cart', function () {
        const productId = randomInt(PRODUCT_ID_RANGE.min, PRODUCT_ID_RANGE.max);
        const quantity = randomInt(1, 5);
        const headers = { ...defaultHeaders, 'X-User-Id': userId.toString() };

        const payload = {
            productId: productId,
            quantity: quantity,
        };

        const response = http.post(
            `${BASE_URL}/api/v1/carts/items`,
            JSON.stringify(payload),
            { headers: headers }
        );

        const passed = checkDetailedResponse(response, {
            'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
            'has cart item': (r) => {
                const body = parseJsonResponse(r);
                return body && body.id;
            },
            'response time < 500ms': (r) => r.timings.duration < 500,
        });

        if (!passed) {
            logError(`Add item to cart for user ${userId} failed`, response);
            return null;
        }

        const body = parseJsonResponse(response);
        return body ? body.id : null;
    });
}

/**
 * 장바구니 아이템 수량 변경
 */
function scenario_updateCartItem(userId, itemId) {
    return group('Update Cart Item', function () {
        const newQuantity = randomInt(1, 10);
        const headers = { ...defaultHeaders, 'X-User-Id': userId.toString() };

        const payload = {
            quantity: newQuantity,
        };

        const response = http.put(
            `${BASE_URL}/api/v1/carts/items/${itemId}`,
            JSON.stringify(payload),
            { headers: headers }
        );

        const passed = checkDetailedResponse(response, {
            'status is 200': (r) => r.status === 200,
            'quantity updated': (r) => {
                const body = parseJsonResponse(r);
                return body && body.quantity === newQuantity;
            },
            'response time < 400ms': (r) => r.timings.duration < 400,
        });

        if (!passed) {
            logError(`Update cart item ${itemId} for user ${userId} failed`, response);
        }
    });
}

/**
 * 장바구니 아이템 삭제
 */
function scenario_removeCartItem(userId, itemId) {
    return group('Remove Cart Item', function () {
        const headers = { ...defaultHeaders, 'X-User-Id': userId.toString() };
        const response = http.del(
            `${BASE_URL}/api/v1/carts/items/${itemId}`,
            null,
            { headers: headers }
        );

        const passed = checkDetailedResponse(response, {
            'status is 200 or 204': (r) => r.status === 200 || r.status === 204,
            'response time < 300ms': (r) => r.timings.duration < 300,
        });

        if (!passed) {
            logError(`Remove cart item ${itemId} for user ${userId} failed`, response);
        }
    });
}

/**
 * 장바구니 전체 비우기
 */
function scenario_clearCart(userId) {
    return group('Clear Cart', function () {
        const headers = { ...defaultHeaders, 'X-User-Id': userId.toString() };
        const response = http.del(
            `${BASE_URL}/api/v1/carts`,
            null,
            { headers: headers }
        );

        const passed = checkDetailedResponse(response, {
            'status is 200 or 204': (r) => r.status === 200 || r.status === 204,
            'response time < 300ms': (r) => r.timings.duration < 300,
        });

        if (!passed) {
            logError(`Clear cart for user ${userId} failed`, response);
        }
    });
}

export function teardown(data) {
    console.log('✅ Cart Service Load Test Completed');
    console.log(`Started at: ${data.startTime}`);
    console.log(`Ended at: ${new Date().toISOString()}`);
}
