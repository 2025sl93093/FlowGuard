#!/bin/bash
# send-baseline.sh — sends N successful workflow_job events to build a baseline
# Usage: ./test-payloads/send-baseline.sh [count] [duration_seconds] [gateway_url]

COUNT=${1:-12}
DURATION=${2:-60}
GATEWAY=${3:-http://localhost:80}
SECRET="flowguard-secret-123"
REPO="demo-org/demo-repo"
JOB_NAME="build-and-test"

echo "Sending $COUNT baseline events (each ${DURATION}s) to $GATEWAY..."

for i in $(seq 1 $COUNT); do
  RUN_ID=$((10000 + i))
  JOB_ID=$((20000 + i))
  STARTED="2024-01-15T10:00:30Z"
  COMPLETED="2024-01-15T10:01:30Z"

  PAYLOAD=$(cat <<JSON
{
  "action": "completed",
  "workflow_job": {
    "id": $JOB_ID,
    "run_id": $RUN_ID,
    "name": "$JOB_NAME",
    "status": "completed",
    "conclusion": "success",
    "started_at": "$STARTED",
    "completed_at": "$COMPLETED",
    "head_branch": "main",
    "steps": [
      {
        "number": 1,
        "name": "Set up JDK 21",
        "conclusion": "success",
        "started_at": "$STARTED",
        "completed_at": "$COMPLETED"
      }
    ]
  },
  "repository": {
    "full_name": "$REPO"
  }
}
JSON
)

  SIG="sha256=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$SECRET" | awk '{print $2}')"

  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$GATEWAY/api/webhooks/github" \
    -H "Content-Type: application/json" \
    -H "X-GitHub-Event: workflow_job" \
    -H "X-Hub-Signature-256: $SIG" \
    -d "$PAYLOAD")

  echo "  Event $i/$COUNT — HTTP $HTTP_CODE (job_id=$JOB_ID, duration=${DURATION}s)"
  sleep 0.3
done

echo ""
echo "Done. Now send a slow event to trigger a regression:"
echo "  ./test-payloads/send-slow-event.sh"
