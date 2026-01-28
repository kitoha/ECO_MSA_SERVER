import http from 'k6/http';
import { sleep, group } from 'k6';
import {
    BASE_URL,
    checkResponse,
    checkDetailedResponse,
    parseJsonResponse,
    randomInt,
    thinkTime,
    logError,
    createPaginationParams,
    defaultHeaders,
} from '../utils/common.js';
import { defaultThresholds, scenarioThresholds } from '../config/thresholds.js';
import { basicLoad, stressTest, spikeTest } from '../config/stages.js';

/**
 * Product Service 부하 테스트
 *
 * 테스트 항목:
 * 1. 상품 목록 조회 - GET /api/v1/products
 * 2. 상품 상세 조회 - GET /api/v1/products/{id}
 * 3. 상품 검색 - POST /api/v1/products/search
 */

export const options = {
    stages: basicLoad,
    thresholds: {
        ...defaultThresholds,
        ...scenarioThresholds.productRead,
    },
    ext: {
        loadimpact: {
            projectID: 3629623,
            name: 'Product Service Load Test'
        }
    }
};

// 테스트 데이터
const CATEGORY_IDS = [1, 2, 3, 4];

/**
 * 셋업: 테스트 전 실행
 */
export function setup() {
    console.log('Starting Product Service Load Test');
    console.log(`Base URL: ${BASE_URL}`);

    // Health Check
    const healthCheck = http.get(`${BASE_URL}/actuator/health`);
    if (healthCheck.status !== 200) {
        throw new Error('Product Service is not healthy!');
    }

    // Fetch all product IDs
    const productsResponse = http.get(`${BASE_URL}/api/v1/products?page=0&size=100`);
    let productIds = [];
    if (productsResponse.status === 200) {
        const products = JSON.parse(productsResponse.body);
        productIds = products.map(p => p.id);
        console.log(`Loaded ${productIds.length} product IDs`);
    }

    return {
        startTime: new Date().toISOString(),
        productIds: productIds,
    };
}

/**
 * 메인 테스트 시나리오
 */
export default function (data) {
    const productIds = data.productIds || [];
    
    // 시나리오 1: 상품 목록 조회 (50%)
    if (Math.random() < 0.5) {
        scenario_listProducts();
    }
    // 시나리오 2: 상품 상세 조회 (40%)
    else if (Math.random() < 0.9) {
        scenario_getProductDetail(productIds);
    }
    // 시나리오 3: 상품 검색 (10%)
    else {
        scenario_searchProducts();
    }

    thinkTime(1, 3);
}

/**
 * 시나리오 1: 상품 목록 조회
 */
function scenario_listProducts() {
    group('List Products', function () {
        const page = randomInt(0, 4);
        const size = 20;
        const params = createPaginationParams(page, size);

        const response = http.get(
            `${BASE_URL}/api/v1/products?${params}`,
            { headers: defaultHeaders }
        );

        const passed = checkDetailedResponse(response, {
            'status is 200': (r) => r.status === 200,
            'has products array': (r) => {
                const body = parseJsonResponse(r);
                return body && Array.isArray(body);
            },
            'response time < 300ms': (r) => r.timings.duration < 300,
        });

        if (!passed) {
            logError('List products failed', response);
        }
    });
}

/**
 * 시나리오 2: 상품 상세 조회
 */
function scenario_getProductDetail(productIds) {
    group('Get Product Detail', function () {
        // Use real product ID from the loaded list
        const productId = productIds[randomInt(0, productIds.length - 1)];

        const response = http.get(
            `${BASE_URL}/api/v1/products/${productId}`,
            { headers: defaultHeaders }
        );

        const passed = checkDetailedResponse(response, {
            'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
            'has product data': (r) => {
                if (r.status === 200) {
                    const body = parseJsonResponse(r);
                    return body && body.id && body.name;
                }
                return true; // 404는 정상 케이스
            },
            'response time < 200ms': (r) => r.timings.duration < 200,
        });

        if (!passed && response.status !== 404) {
            logError(`Get product ${productId} failed`, response);
        }
    });
}

/**
 * 시나리오 3: 상품 검색 (POST)
 */

function scenario_searchProducts() {
    group('Search Products', function () {
        const searchTerms = ['Laptop', 'Chair', 'Table', 'Lamp', 'Phone'];
        const keyword = searchTerms[randomInt(0, searchTerms.length - 1)];
        
        const searchRequest = {
            keyword: keyword,
            minPrice: null,
            maxPrice: null,
            categoryId: null,
            status: 'ACTIVE'
        };

        const response = http.post(
            `${BASE_URL}/api/v1/products/search`,
            JSON.stringify(searchRequest),
            { headers: defaultHeaders }
        );

        const passed = checkDetailedResponse(response, {
            'status is 200': (r) => r.status === 200,
            'has search results': (r) => {
                const body = parseJsonResponse(r);
                return body && Array.isArray(body);
            },
            'response time < 500ms': (r) => r.timings.duration < 500,
        });

        if (!passed) {
            logError(`Search products with keyword '${keyword}' failed`, response);
        }
    });
}

/**
 * 티어다운: 테스트 후 실행
 */
export function teardown(data) {
    console.log('✅ Product Service Load Test Completed');
    console.log(`Started at: ${data.startTime}`);
    console.log(`Ended at: ${new Date().toISOString()}`);
}

/**
 * 스트레스 테스트 전용 옵션
 */
export const stressTestOptions = {
    stages: stressTest,
    thresholds: {
        'http_req_failed': ['rate<0.02'], // 스트레스 테스트는 에러율 완화
        'http_req_duration': ['p(95)<1000', 'p(99)<2000'],
    },
};

/**
 * 스파이크 테스트 전용 옵션
 */
export const spikeTestOptions = {
    stages: spikeTest,
    thresholds: {
        'http_req_failed': ['rate<0.05'], // 스파이크는 더 완화
        'http_req_duration': ['p(95)<1500', 'p(99)<3000'],
    },
};
