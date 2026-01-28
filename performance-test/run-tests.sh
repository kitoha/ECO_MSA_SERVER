#!/bin/bash
set -e

BASE_URL="${BASE_URL:-http://localhost:8080}"
RESULTS_DIR="results/$(date +%Y%m%d-%H%M%S)"

mkdir -p "$RESULTS_DIR"

echo "ECO MSA Performance Testing"
echo "Base URL: $BASE_URL"
echo "Results: $RESULTS_DIR"
echo ""

# Check k6 installation
if ! command -v k6 &> /dev/null; then
    echo "Error: k6 not found. Install from https://k6.io"
    exit 1
fi

# Check API Gateway health
if ! curl -f -s "$BASE_URL/actuator/health" > /dev/null; then
    echo "Error: API Gateway not responding at $BASE_URL"
    echo "Start services: docker-compose up -d"
    exit 1
fi

# Run k6 test
run_test() {
    local name=$1
    local file=$2
    local type=${3:-load}

    echo "Running: $name ($type)"

    k6 run "k6/scenarios/$file" \
        -e BASE_URL="$BASE_URL" \
        -e SCENARIO="$type" \
        --out json="$RESULTS_DIR/$name-$type.json" \
        2>&1 | tee "$RESULTS_DIR/$name-$type-summary.txt"

    echo ""
}

# Test suites
case "${1:-menu}" in
    smoke)
        run_test "product-service" "product-service.js" "smoke"
        ;;
    load)
        run_test "product-service" "product-service.js" "load"
        run_test "cart-service" "cart-service.js" "load"
        run_test "order-service" "order-service.js" "load"
        run_test "e2e-user-journey" "e2e-user-journey.js" "load"
        ;;
    stress)
        run_test "product-service" "product-service.js" "stress"
        run_test "order-service" "order-service.js" "stress"
        run_test "e2e-user-journey" "e2e-user-journey.js" "stress"
        ;;
    e2e)
        run_test "e2e-user-journey" "e2e-user-journey.js" "load"
        ;;
    all)
        echo "Running full test suite (this may take ~2 hours)"
        run_test "product-service" "product-service.js" "smoke"
        sleep 10
        run_test "product-service" "product-service.js" "load"
        run_test "cart-service" "cart-service.js" "load"
        run_test "order-service" "order-service.js" "load"
        run_test "e2e-user-journey" "e2e-user-journey.js" "load"
        sleep 10
        run_test "product-service" "product-service.js" "stress"
        run_test "order-service" "order-service.js" "stress"
        run_test "e2e-user-journey" "e2e-user-journey.js" "stress"
        ;;
    menu|*)
        echo "Usage: $0 [smoke|load|stress|e2e|all]"
        echo ""
        echo "Available test suites:"
        echo "  smoke  - Quick smoke test (~5 min)"
        echo "  load   - Basic load test on all services (~20 min)"
        echo "  stress - Stress test to find limits (~30 min)"
        echo "  e2e    - E2E user journey only"
        echo "  all    - Full test suite (~2 hours)"
        echo ""
        echo "Example:"
        echo "  ./run-tests.sh load"
        echo "  BASE_URL=http://staging.example.com ./run-tests.sh e2e"
        exit 0
        ;;
esac

echo "Tests completed"
echo "Results saved to: $RESULTS_DIR"
echo ""
echo "Next steps:"
echo "  - View Grafana: http://localhost:3001"
echo "  - Analyze results: ls -lh $RESULTS_DIR"
