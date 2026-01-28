
export const defaultThresholds = {
    // HTTP 요청 성공률 (99% 이상)
    'http_req_failed': ['rate<0.01'],

    // 응답 시간 임계값
    'http_req_duration': [
        'p(95)<500',   // 95%가 500ms 이하
        'p(99)<1000',  // 99%가 1000ms 이하
        'avg<300',     // 평균 300ms 이하
    ],

    // 요청 대기 시간
    'http_req_waiting': [
        'p(95)<400',
        'p(99)<800',
    ],

    // 연결 시간
    'http_req_connecting': [
        'p(95)<100',
    ],

    // TLS 핸드셰이크 시간
    'http_req_tls_handshaking': [
        'p(95)<200',
    ],
};

export const strictThresholds = {
    'http_req_failed': ['rate<0.005'],
    'http_req_duration': [
        'p(95)<300',
        'p(99)<500',
        'avg<200',
    ],
    'http_req_waiting': [
        'p(95)<250',
        'p(99)<400',
    ],
};

export const relaxedThresholds = {
    'http_req_failed': ['rate<0.05'],
    'http_req_duration': [
        'p(95)<1000',
        'p(99)<2000',
        'avg<500',
    ],
    'http_req_waiting': [
        'p(95)<800',
        'p(99)<1500',
    ],
};

/**
 * 시나리오별 맞춤 임계값
 */
export const scenarioThresholds = {
    // 상품 조회 (읽기 집약적)
    productRead: {
        'http_req_failed': ['rate<0.01'],
        'http_req_duration': [
            'p(95)<300',
            'p(99)<500',
        ],
    },

    // 주문 생성 (쓰기 집약적)
    orderCreate: {
        'http_req_failed': ['rate<0.02'],
        'http_req_duration': [
            'p(95)<800',
            'p(99)<1500',
        ],
    },

    // 장바구니 (중간 부하)
    cart: {
        'http_req_failed': ['rate<0.01'],
        'http_req_duration': [
            'p(95)<400',
            'p(99)<800',
        ],
    },

    // E2E 사용자 여정
    e2e: {
        'http_req_failed': ['rate<0.02'],
        'http_req_duration': [
            'p(95)<1000',
            'p(99)<2000',
        ],
    },
};
