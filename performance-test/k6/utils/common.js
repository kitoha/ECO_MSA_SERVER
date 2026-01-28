import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import crypto from 'k6/crypto';
import encoding from 'k6/encoding';

/**
 * 공통 설정 및 유틸리티 함수
 */

// 환경 설정
export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
export const THINK_TIME = parseFloat(__ENV.THINK_TIME) || 1; // seconds

// JWT 설정
const JWT_SECRET_BASE64 = 'c2VjcmV0LWtleS1mb3ItZWNvbW1lcmNlLW1zYS1wcm9qZWN0LWdlbmVyYXRlZC1ieS1haS1hc3Npc3RhbnQ=';
const JWT_SECRET = encoding.b64decode(JWT_SECRET_BASE64, 'std');

/**
 * 테스트용 JWT 토큰 생성 (HS256)
 */
export function generateToken(userId) {
    const header = encoding.b64encode(JSON.stringify({ alg: 'HS256', typ: 'JWT' }), 'rawurl');
    const payload = encoding.b64encode(JSON.stringify({
        sub: userId.toString(),
        iat: Math.floor(Date.now() / 1000),
        exp: Math.floor(Date.now() / 1000) + 3600,
    }), 'rawurl');

    const toSign = `${header}.${payload}`;
    const signature = crypto.hmac('sha256', JWT_SECRET, toSign, 'base64rawurl');
    
    return `${header}.${payload}.${signature}`;
}

export const errorRate = new Rate('errors');
export const successRate = new Rate('success');
export const customTrend = new Trend('custom_duration');
export const requestCounter = new Counter('requests_total');

/**
 * HTTP 요청 기본 옵션
 */
export const defaultHeaders = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
};

/**
 * 공통 체크 함수
 */
export function checkResponse(response, expectedStatus = 200) {
    const result = check(response, {
        [`status is ${expectedStatus}`]: (r) => r.status === expectedStatus,
        'response time < 500ms': (r) => r.timings.duration < 500,
        'response time < 1000ms': (r) => r.timings.duration < 1000,
        'has body': (r) => r.body && r.body.length > 0,
    });

    const isHttpError = response.status >= 400;
    if (isHttpError) {
        errorRate.add(1);
    }
    successRate.add(!isHttpError);
    requestCounter.add(1);

    return result;
}

/**
 * 상세한 응답 체크
 */
export function checkDetailedResponse(response, checks = {}) {
    const defaultChecks = {
        'status is 200': (r) => r.status === 200,
        'response time < 1000ms': (r) => r.timings.duration < 1000,
    };

    const allChecks = { ...defaultChecks, ...checks };
    const result = check(response, allChecks);

    // errorRate는 실제 HTTP 실패(4xx, 5xx)만 카운트
    const isHttpError = response.status >= 400;
    errorRate.add(isHttpError);
    successRate.add(!isHttpError);
    requestCounter.add(1);

    return result;
}

/**
 * JSON 응답 파싱
 */
export function parseJsonResponse(response) {
    try {
        return JSON.parse(response.body);
    } catch (e) {
        console.error(`Failed to parse JSON: ${e.message}`);
        console.error(`Response body: ${response.body}`);
        return null;
    }
}

/**
 * 랜덤 사용자 생성
 */
export function generateRandomUser() {
    const timestamp = Date.now();
    const random = Math.floor(Math.random() * 10000);
    return {
        userId: `user_${timestamp}_${random}`,
        email: `user_${timestamp}_${random}@test.com`,
        name: `Test User ${random}`,
    };
}

/**
 * 랜덤 정수 생성
 */
export function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

/**
 * 랜덤 요소 선택
 */
export function randomItem(array) {
    return array[Math.floor(Math.random() * array.length)];
}

/**
 * Think Time (사용자 대기 시간 시뮬레이션)
 */
export function thinkTime(min = 1, max = 3) {
    const delay = randomInt(min, max);
    sleep(delay);
}

/**
 * 로그 헬퍼
 */
export function logInfo(message) {
    if (__ENV.DEBUG === 'true') {
        console.log(`[INFO] ${message}`);
    }
}

export function logError(message, response = null) {
    console.error(`[ERROR] ${message}`);
    if (response) {
        console.error(`Status: ${response.status}`);
        console.error(`Body: ${response.body}`);
    }
}

/**
 * 시나리오 그룹 래퍼
 */
export function scenario(name, fn) {
    return group(name, fn);
}

/**
 * 에러 핸들링
 */
export function handleError(error, context = '') {
    logError(`${context}: ${error.message}`);
    errorRate.add(1);
    successRate.add(0);
}

/**
 * 페이지네이션 파라미터 생성
 */
export function createPaginationParams(page = 0, size = 20) {
    return `page=${page}&size=${size}`;
}

/**
 * 성능 메트릭 수집
 */
export function recordMetric(name, value) {
    customTrend.add(value, { metric: name });
}

/**
 * 배치 요청 헬퍼
 */
export function createBatchRequests(requests) {
    return requests.map(req => ({
        method: req.method || 'GET',
        url: `${BASE_URL}${req.path}`,
        headers: { ...defaultHeaders, ...req.headers },
        body: req.body ? JSON.stringify(req.body) : undefined,
    }));
}

/**
 * 타임스탬프 생성
 */
export function timestamp() {
    return new Date().toISOString();
}

/**
 * 테스트 데이터 ID 범위 생성
 */
export function generateIdRange(min, max) {
    const ids = [];
    for (let i = min; i <= max; i++) {
        ids.push(i);
    }
    return ids;
}

/**
 * 가중치 기반 랜덤 선택
 * @param {Array} items - [{ value: any, weight: number }]
 */
export function weightedRandom(items) {
    const totalWeight = items.reduce((sum, item) => sum + item.weight, 0);
    let random = Math.random() * totalWeight;

    for (const item of items) {
        if (random < item.weight) {
            return item.value;
        }
        random -= item.weight;
    }

    return items[items.length - 1].value;
}

/**
 * 성능 임계값 체크
 */
export function checkPerformance(response, thresholds = {}) {
    const {
        maxDuration = 1000,
        maxWaiting = 800,
        maxConnecting = 100,
    } = thresholds;

    return check(response, {
        'duration within threshold': (r) => r.timings.duration < maxDuration,
        'waiting within threshold': (r) => r.timings.waiting < maxWaiting,
        'connecting within threshold': (r) => r.timings.connecting < maxConnecting,
    });
}

/**
 * 타임아웃 설정
 */
export const timeoutOptions = {
    timeout: '30s',
};
