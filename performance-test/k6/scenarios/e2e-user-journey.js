import http from 'k6/http';
import { group, sleep } from 'k6';
import {
    BASE_URL,
    checkDetailedResponse,
    parseJsonResponse,
    randomInt,
    thinkTime,
    logError,
    logInfo,
    defaultHeaders,
    generateToken,
} from '../utils/common.js';
import { defaultThresholds, scenarioThresholds } from '../config/thresholds.js';
import { basicLoad, spikeTest } from '../config/stages.js';

export const options = {
    stages: basicLoad,
    thresholds: {
        ...defaultThresholds,
        ...scenarioThresholds.e2e,
    },
};

const CATEGORY_IDS = [1, 2, 3, 4, 5];

export function setup() {
    console.log('Starting E2E User Journey Load Test with Bypass');
    return { startTime: new Date().toISOString() };
}

export default function () {
    const userId = randomInt(1, 100);
    const token = generateToken(userId);
    
    // 모든 요청에 사용할 공통 헤더 (테스트 우회 포함)
    const authHeaders = {
        ...defaultHeaders,
        'Authorization': `Bearer ${token}`,
        'X-Test-Request': 'performance-test' 
    };

    const journey = {
        userId,
        authHeaders,
        products: [],
        cartItems: [],
        orderId: null,
    };

    // Step 1: 상품 탐색
    journey.products = step_browsing(journey);
    thinkTime(1, 2);

    // Step 2: 상품 상세
    if (journey.products.length > 0) {
        step_viewProductDetails(journey);
        thinkTime(1, 2);
    }

    // Step 3: 장바구니 추가
    if (journey.products.length > 0) {
        step_addToCart(journey);
        thinkTime(1, 2);
    }

    // Step 4: 장바구니 확인
    step_reviewCart(journey);
    thinkTime(1, 2);

    // Step 5: 주문 생성
    if (Math.random() < 0.8 && journey.cartItems.length > 0) {
        journey.orderId = step_checkout(journey);
        
        // Step 6: 주문 확인
        if (journey.orderId) {
            step_confirmOrder(journey);
        }
    }

    sleep(1);
}

function step_browsing(journey) {
    return group('Product Browsing', function () {
        const response = http.get(`${BASE_URL}/api/v1/products`, { headers: journey.authHeaders });
        if (checkDetailedResponse(response, { 'status is 200': (r) => r.status === 200 })) {
            return parseJsonResponse(response) || [];
        }
        return [];
    });
}

function step_viewProductDetails(journey) {
    group('View Product Details', function () {
        const product = journey.products[0];
        http.get(`${BASE_URL}/api/v1/products/${product.id}`, { headers: journey.authHeaders });
    });
}

function step_addToCart(journey) {
    group('Add to Cart', function () {
        const product = journey.products[0];
        const payload = JSON.stringify({ productId: product.id, quantity: 1 });
        const response = http.post(`${BASE_URL}/api/v1/carts/items`, payload, { headers: journey.authHeaders });
        
        if (checkDetailedResponse(response, { 'added to cart': (r) => r.status === 200 || r.status === 201 })) {
            const body = parseJsonResponse(response);
            if (body && body.items) {
                journey.cartItems = body.items;
            } else {
                // 구조가 다를 경우를 대비해 더미 아이템 추가 (주문 단계 진행을 위해)
                journey.cartItems.push({ productId: product.id, quantity: 1 });
            }
        }
    });
}

function step_reviewCart(journey) {
    group('Review Cart', function () {
        http.get(`${BASE_URL}/api/v1/carts`, { headers: journey.authHeaders });
    });
}

function step_checkout(journey) {
    return group('Checkout', function () {
        const payload = JSON.stringify({
            items: journey.cartItems.map(item => ({
                productId: item.productId,
                quantity: item.quantity || 1,
                price: 10000
            })),
            shippingAddress: 'Seoul, Korea (Simulated Address)',
            shippingName: `User ${journey.userId}`,
            shippingPhone: '010-1234-5678'
        });
        const response = http.post(`${BASE_URL}/api/v1/orders`, payload, { headers: journey.authHeaders });
        if (checkDetailedResponse(response, { 'order created': (r) => r.status === 200 || r.status === 201 })) {
            const body = parseJsonResponse(response);
            return body ? body.id : null;
        }
        return null;
    });
}

function step_confirmOrder(journey) {
    group('Confirm Order', function () {
        http.get(`${BASE_URL}/api/v1/orders/${journey.orderId}`, { headers: journey.authHeaders });
    });
}