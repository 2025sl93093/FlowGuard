#!/bin/bash
# send-slow-event.sh — sends a single slow job event to trigger regression detection
# Usage: ./test-payloads/send-slow-event.sh [gateway_url]

GATEWAY=${1:-http://localhost:80}
SECRET="flowguard-secret-123"
RUN_ID=99999
JOB_ID=99001

PAYLOAD='{
  "action": "completed",
  "workflow_job": {
    "id": '"$JOB_ID"',
    "run_id": '"$RUN_ID"',
    "name": "build-and-test",
    "status": "completed",
    "conclusion": "success",
    "started_at": "2024-01-15T11:00:00Z",
    "completed_at": "2024-01-15T11:08:00Z",
    "head_branch": "main",
    "steps": []
  },
  "repository": {
    "full_name": "demo-org/demo-repo"
  }
}'

SIG="sha256=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$SECRET" | awk '{print $2}')"

echo "Sending slow event (480s) to trigger regression..."

curl -s -o /dev/null -w "HTTP %{http_code}\n" -X POST "$GATEWAY/api/webhooks/github" \
  -H "Content-Type: application/json" \
  -H "X-GitHub-Event: workflow_job" \
  -H "X-Hub-Signature-256: $SIG" \
  -d "$PAYLOAD"

echo ""
echo "Check regressions at: $GATEWAY/api/analytics/regressions"
echo "Check dashboard at: http://localhost:3000"
