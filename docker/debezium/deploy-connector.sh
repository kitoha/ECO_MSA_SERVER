# Description: Script to deploy Debezium Outbox Connector to Kafka Connect
# This script checks if Kafka Connect is running, deletes any existing connector with the same name
# local configuration file, and deploys a new connector using the provided JSON configuration.
#!/bin/bash

set -e

KAFKA_CONNECT_URL="http://localhost:8090"
CONNECTOR_NAME="order-outbox-connector"
CONNECTOR_CONFIG="docker/debezium/order-outbox-connector.json"

echo " Deploying Debezium Outbox Connector..."
echo ""

# Wait for Kafka Connect to be ready
echo " Waiting for Kafka Connect to start..."
MAX_RETRIES=30
RETRY_COUNT=0

while ! curl -s "${KAFKA_CONNECT_URL}" > /dev/null; do
  RETRY_COUNT=$((RETRY_COUNT + 1))
  if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
    echo "❌ Kafka Connect did not start in time"
    exit 1
  fi
  echo "   Attempt ${RETRY_COUNT}/${MAX_RETRIES}..."
  sleep 5
done

echo "✅ Kafka Connect is ready!"
echo ""

# Check if connector already exists
if curl -s "${KAFKA_CONNECT_URL}/connectors/${CONNECTOR_NAME}" > /dev/null 2>&1; then
  echo "Connector '${CONNECTOR_NAME}' already exists. Deleting..."
  curl -X DELETE "${KAFKA_CONNECT_URL}/connectors/${CONNECTOR_NAME}"
  echo ""
  sleep 2
fi

# Deploy connector
echo "Deploying connector from ${CONNECTOR_CONFIG}..."
RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  --data @"${CONNECTOR_CONFIG}" \
  "${KAFKA_CONNECT_URL}/connectors")

if echo "$RESPONSE" | grep -q "error"; then
  echo "Failed to deploy connector:"
  echo "$RESPONSE" | jq .
  exit 1
fi

echo "Connector deployed successfully!"
echo ""

# Wait a bit for connector to initialize
sleep 3

# Check connector status
echo "Connector Status:"
curl -s "${KAFKA_CONNECT_URL}/connectors/${CONNECTOR_NAME}/status" | jq .


