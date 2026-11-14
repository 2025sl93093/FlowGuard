# FlowGuard

**Self-hosted CI/CD pipeline health monitor.** FlowGuard tracks GitHub Actions pipeline durations over time, detects performance regressions and flaky tests, and sends email alerts — all without sending your data to any third-party SaaS.

> Datadog CI Visibility costs $500+/month and sends your pipeline metadata to external servers. FlowGuard is free, self-hosted, and takes 2 minutes to deploy.

## The Problem

Engineering teams running microservices have no easy, free way to answer:
- Which CI job got 4× slower this week?
- Is this test genuinely broken or just flaky?
- When did my build time start regressing?

Existing tools (Datadog, BuildPulse, Trunk Flaky Tests) are SaaS-only and expensive. FlowGuard fills this gap.

## Features

- **Pipeline tracking** — stores every GitHub Actions workflow run and job with duration history
- **Regression detection** — alerts when a job duration exceeds its rolling baseline by 3×
- **Flaky test detection** — flags jobs with >20% failure rate over a rolling window
- **Email alerts** — sends via Gmail SMTP when regressions or flakiness are detected
- **React dashboard** — 5-page UI with Recharts visualizations, auto-refreshes every 30s
- **REST API** — all data accessible via JSON endpoints, documented at `/swagger-ui.html`

## Architecture

```
GitHub Actions
     │
     │ webhook (HMAC-SHA256)
     ▼
┌─────────────┐     RabbitMQ      ┌──────────────────┐
│   NGINX     │──────────────────▶│  pipeline-service │  PostgreSQL
│ API Gateway │                   └──────────────────┘
│   :80       │──────────────────▶┌──────────────────┐
└─────────────┘     RabbitMQ      │ analytics-service │  PostgreSQL
     ▲                            └────────┬─────────┘
     │                                     │ RabbitMQ alert
     │                            ┌────────▼─────────┐
     │                            │notification-svc  │  PostgreSQL
     │                            └──────────────────┘
     │
     │ REST
┌────┴────────┐
│  dashboard  │
│  React :3000│
└─────────────┘
```

**Core principle: Single Responsibility + Loose Coupling via async messaging**

Each service owns one domain concern and its own database. Services communicate via RabbitMQ queues so they can be scaled and deployed independently.

| Service | Port | Responsibility |
|---|---|---|
| webhook-service | 8081 | Receives and validates GitHub webhooks, publishes events |
| pipeline-service | 8082 | Stores pipeline run/job/step records, serves history |
| analytics-service | 8083 | Detects regressions and flaky tests, publishes alerts |
| notification-service | 8084 | Sends email alerts with cooldown deduplication |

## Quick Start (Docker Compose)

**Prerequisites:** Docker Desktop, Git

```bash
# 1. Clone
git clone https://github.com/yourusername/flowguard.git
cd flowguard

# 2. Configure
cp .env.example .env
# Edit .env with your Gmail App Password and GitHub webhook secret

# 3. Start everything
docker compose up --build

# 4. Open dashboard
open http://localhost:3000

# 5. Send a test event
curl -X POST http://localhost:80/api/webhooks/github \
  -H "Content-Type: application/json" \
  -H "X-GitHub-Event: workflow_run" \
  -d @test-payloads/workflow_run_success.json
```

**Services after startup:**
- Dashboard: http://localhost:3000
- RabbitMQ Management: http://localhost:15672 (flowguard/flowguard123)
- API Gateway: http://localhost:80

## Connect Real GitHub Actions

```bash
# 1. Install ngrok
brew install ngrok/ngrok/ngrok
ngrok config add-authtoken YOUR_NGROK_TOKEN

# 2. Expose local port 80
ngrok http 80
# Copy the https://xxxxx.ngrok-free.app URL

# 3. Add webhook in GitHub
# Your repo → Settings → Webhooks → Add webhook
# Payload URL: https://xxxxx.ngrok-free.app/api/webhooks/github
# Content type: application/json
# Secret: value from your .env GITHUB_WEBHOOK_SECRET
# Events: Workflow runs + Workflow jobs
```

## Kubernetes Deployment (Minikube)

```bash
# 1. Start Minikube
minikube start --memory=4096 --cpus=4

# 2. Apply all manifests
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/secrets/
kubectl apply -f k8s/storage/
kubectl apply -f k8s/postgres/
kubectl apply -f k8s/rabbitmq/
kubectl apply -f k8s/configmaps/
kubectl apply -f k8s/services/
kubectl apply -f k8s/gateway/
kubectl apply -f k8s/dashboard/

# 3. Wait for pods
kubectl get pods -n flowguard -w

# 4. Access dashboard
minikube service dashboard-service -n flowguard
```

## Demo — Trigger a Regression

```bash
# Build baseline (12 runs × 60 seconds each)
for i in $(seq 1 12); do
  ./test-payloads/send-baseline.sh $i
  sleep 0.5
done

# Send a slow run (480 seconds = 8× baseline)
curl -X POST http://localhost:80/api/webhooks/github \
  -H "Content-Type: application/json" \
  -H "X-GitHub-Event: workflow_run" \
  -d @test-payloads/workflow_run_slow.json

# Check regressions dashboard tab — new entry appears within seconds
# Check email inbox — alert email sent
```

## API Reference

| Endpoint | Method | Description |
|---|---|---|
| `/api/webhooks/github` | POST | Receive GitHub webhook |
| `/api/webhooks/events` | GET | List raw events |
| `/api/pipelines/runs` | GET | List pipeline runs (`?repo=owner/repo`) |
| `/api/pipelines/jobs/{name}/history` | GET | Job duration history |
| `/api/pipelines/repos` | GET | List tracked repositories |
| `/api/analytics/summary` | GET | Dashboard summary cards |
| `/api/analytics/trends/{jobName}` | GET | Duration trend data |
| `/api/analytics/regressions` | GET | Open regressions |
| `/api/analytics/flaky` | GET | Flaky jobs |
| `/api/notifications` | GET | Notification history |
| `/api/notifications/test-email` | POST | Send test email |

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Issues and PRs are welcome.

## License

[MIT](LICENSE)
