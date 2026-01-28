/**
 * 기본 부하 테스트
 * - Ramp-up: 1분
 * - Steady: 3분
 * - Ramp-down: 1분
 */
export const basicLoad = [
    { duration: '1m', target: 50 },   // Ramp-up to 50 VUs
    { duration: '3m', target: 50 },   // Stay at 50 VUs
    { duration: '1m', target: 0 },    // Ramp-down to 0
];

/**
 * 스트레스 테스트
 * - 점진적으로 부하 증가하여 시스템 한계 찾기
 */
export const stressTest = [
    { duration: '2m', target: 100 },   // Ramp-up to 100
    { duration: '3m', target: 100 },   // Stay at 100
    { duration: '2m', target: 200 },   // Increase to 200
    { duration: '3m', target: 200 },   // Stay at 200
    { duration: '2m', target: 300 },   // Increase to 300
    { duration: '3m', target: 300 },   // Stay at 300
    { duration: '2m', target: 0 },     // Ramp-down
];

/**
 * 스파이크 테스트
 * - 급격한 트래픽 증가 시뮬레이션 (블랙프라이데이, 한정판 출시)
 */
export const spikeTest = [
    { duration: '1m', target: 50 },    // Normal load
    { duration: '30s', target: 500 },  // Sudden spike
    { duration: '3m', target: 500 },   // Maintain spike
    { duration: '30s', target: 50 },   // Back to normal
    { duration: '2m', target: 50 },    // Recovery period
    { duration: '30s', target: 0 },    // Ramp-down
];

/**
 * 내구성 테스트 (Soak Test)
 * - 장시간 일정 부하 유지하여 메모리 누수 등 확인
 */
export const soakTest = [
    { duration: '5m', target: 100 },   // Ramp-up
    { duration: '60m', target: 100 },  // Sustained load for 1 hour
    { duration: '5m', target: 0 },     // Ramp-down
];

/**
 * 연기 테스트 (Smoke Test)
 * - 최소 부하로 기본 동작 확인
 */
export const smokeTest = [
    { duration: '30s', target: 5 },    // Minimal load
    { duration: '1m', target: 5 },     // Stay at minimal
    { duration: '30s', target: 0 },    // Ramp-down
];

/**
 * 점진적 증가 테스트
 * - 시스템 용량 파악
 */
export const capacityTest = [
    { duration: '2m', target: 50 },
    { duration: '2m', target: 100 },
    { duration: '2m', target: 150 },
    { duration: '2m', target: 200 },
    { duration: '2m', target: 250 },
    { duration: '2m', target: 300 },
    { duration: '2m', target: 0 },
];

/**
 * 피크 타임 시뮬레이션
 * - 하루 중 피크 시간대 패턴 재현
 */
export const peakHourSimulation = [
    { duration: '2m', target: 20 },    // 오전 시간
    { duration: '3m', target: 50 },    // 점심 시간
    { duration: '2m', target: 30 },    // 오후 시간
    { duration: '5m', target: 150 },   // 저녁 피크 타임
    { duration: '3m', target: 50 },    // 야간 시간
    { duration: '2m', target: 0 },     // 새벽
];

/**
 * 사용자 정의 부하 생성 함수
 */
export function createCustomStages(config) {
    const {
        rampUpDuration = '1m',
        rampUpTarget = 50,
        steadyDuration = '5m',
        rampDownDuration = '1m'
    } = config;

    return [
        { duration: rampUpDuration, target: rampUpTarget },
        { duration: steadyDuration, target: rampUpTarget },
        { duration: rampDownDuration, target: 0 },
    ];
}
