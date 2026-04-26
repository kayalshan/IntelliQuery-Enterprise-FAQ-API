# IntelliQuery — Enterprise FAQ API

> An AI-powered FAQ engine built on **Spring Boot 3**, **OpenAI GPT-4**, **Redis**, and **Apache Kafka** — designed for high availability, observability, and production-grade resilience.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Features](#features)
- [Quick Start](#quick-start)
- [API Reference](#api-reference)
- [Configuration](#configuration)
- [Observability](#observability)
- [Development](#development)
- [Project Structure](#project-structure)

---

## Overview

**IntelliQuery** is an enterprise FAQ API that answers natural language questions using OpenAI GPT-4. It is built with production reliability in mind: responses are cached in Redis to minimise latency and cost, every interaction is streamed to Kafka for downstream processing, and the OpenAI integration is protected by a circuit breaker and retry policy. All endpoints are secured with API key authentication and fully instrumented with Prometheus metrics and distributed tracing.

---

## Architecture

```
Client
  │  X-API-Key header
  ▼
SecurityFilter (ApiKeyAuthFilter)
  ▼
FaqController  ──  POST /api/v1/faq/ask
  ▼
FaqService
  ├──► Redis Cache (faqCache)          ── HIT ──► QuestionResponse
  │
  └──► OpenAiService
         ├─ CircuitBreaker (Resilience4j)
         ├─ Retry (3 attempts, 1 s backoff)
         └─ HTTP POST → OpenAI GPT-4
  ▼
FaqEventPublisher (async)
  └──► Kafka topic: faq-events
```

---

## Features
```
| Capability | Implementation |
|---|---|
| AI-powered answers | OpenAI GPT-4 via REST |
| Response caching | Redis — 1 hr TTL, key = normalised question |
| Event streaming | Apache Kafka (`faq-events` topic) |
| Circuit breaker | Resilience4j — 50% failure rate → open for 30 s |
| Retry | 3 attempts with 1 s backoff |
| Authentication | API key via `X-API-Key` header |
| Input validation | Jakarta Bean Validation on all inputs |
| Observability | Micrometer + Prometheus + Grafana |
| Distributed tracing | Micrometer Tracing (Brave / Zipkin) |
| API documentation | SpringDoc OpenAPI 3 / Swagger UI |
| Structured logging | MDC request IDs, JSON format in production |
| Health checks | Redis, Kafka, and OpenAI custom health indicators |
| Graceful shutdown | Spring lifecycle with 30 s drain period |
| Containerisation | Multi-stage Docker build with layered JARs |
```
---

## Quick Start

### Prerequisites

- Java 21+
- Docker & Docker Compose
- An OpenAI API key

### Run locally

```bash
# 1. Clone and enter the project
git clone <repo-url>
cd intelliquery   # or faq-api

# 2. Export required environment variables
export OPENAI_API_KEY=sk-...
export APP_API_KEY=my-secure-key

# 3. Start all services
docker-compose up -d

# 4. Confirm the API is healthy
curl http://localhost:8080/actuator/health
```

### Ask a question

```bash
curl -X POST http://localhost:8080/api/v1/faq/ask \
  -H "Content-Type: application/json" \
  -H "X-API-Key: my-secure-key" \
  -d '{"question": "What is SuperWidget?"}'
```

**Sample response:**

```json
{
  "question": "What is SuperWidget?",
  "answer": "SuperWidget is a versatile productivity tool that helps teams...",
  "cached": false,
  "latencyMs": 842,
  "timestamp": "2025-01-15T10:30:00Z",
  "requestId": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## API Reference
```
| Method | Path | Auth required | Description |
|---|---|---|---|
| `POST` | `/api/v1/faq/ask` | ✅ | Submit an FAQ question |
| `GET` | `/actuator/health` | ❌ | Service health check |
| `GET` | `/actuator/metrics` | ✅ | Application metrics |
| `GET` | `/actuator/prometheus` | ✅ | Prometheus scrape endpoint |
| `GET` | `/swagger-ui.html` | ❌ | Interactive API documentation |
```

### Request body

```json
{
  "question": "string  (3–500 characters, required)",
  "context":  "string  (optional)"
}
```

---

## Configuration

IntelliQuery follows the 12-factor app methodology — all configuration is provided via environment variables.

```
| Variable | Default | Description |
|---|---|---|
| `OPENAI_API_KEY` | *(required)* | OpenAI secret key |
| `APP_API_KEY` | `changeme-in-production` | API key issued to clients |
| `SPRING_REDIS_HOST` | `localhost` | Redis hostname |
| `SPRING_REDIS_PORT` | `6379` | Redis port |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker addresses |
```
For the full configuration reference, see `src/main/resources/application.yml`.

---

## Observability
```
| URL | Service |
|---|---|
| http://localhost:3000 | Grafana (default credentials: `admin` / `admin`) |
| http://localhost:9090 | Prometheus |
| http://localhost:8081 | Kafka UI |
| http://localhost:8080/swagger-ui.html | Swagger / API Docs |
```

### Key metrics

```
| Metric | Description |
|---|---|
| `faq.openai.request` | Histogram of OpenAI API call durations |
| `faq.cache.hits` / `faq.cache.misses` | Cache effectiveness |
| `resilience4j.circuitbreaker.*` | Circuit breaker state and transitions |
| `http.server.requests` | Standard Spring MVC HTTP metrics |
```
---

## Kafka — KRaft Mode
 
Kafka runs in **KRaft mode** — Zookeeper is not used. Three listeners are configured:
 
| Listener | Port | Used by |
|---|---|---|
| `PLAINTEXT` | `9092` | Spring Boot app inside Docker |
| `EXTERNAL` | `9094` | Host machine / external tools (kafkacat, etc.) |
| `CONTROLLER` | `9093` | KRaft internal consensus — not exposed |
 
Connect from your host machine using `localhost:9094`. The app container connects internally via `kafka:9092` — no changes to `application.yml` are needed.
 
---


## Development

```bash
# Run the test suite
./mvnw test

# Run locally with the dev profile (verbose logging)
./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=dev \
  -Dspring-boot.run.jvmArguments="-DOPENAI_API_KEY=sk-... -DAPP_API_KEY=dev-key"

# Build Docker image
docker build -t intelliquery:local .

# Verify code coverage (80% minimum enforced)
./mvnw verify
open target/site/jacoco/index.html
```

---

## Project Structure

```
intelliquery/
├── src/
│   ├── main/java/com/sonataxai/faqapi/
│   │   ├── FaqApiApplication.java
│   │   ├── config/           # Redis, Kafka, Security, OpenAPI configuration
│   │   ├── controller/       # REST controllers
│   │   ├── dto/              # Request and response DTOs
│   │   ├── exception/        # Custom exceptions and global exception handler
│   │   ├── health/           # Custom Actuator health indicators
│   │   ├── kafka/            # Kafka event publisher
│   │   ├── model/            # Domain models (FaqEvent)
│   │   ├── security/         # API key authentication filter
│   │   ├── service/          # Business logic (FaqService, OpenAiService)
│   │   └── util/             # MDC filter and helper utilities
│   ├── main/resources/
│   │   ├── application.yml       # Multi-profile configuration
│   │   └── logback-spring.xml    # Structured logging (JSON in production)
│   └── test/                     # Unit and integration tests
├── monitoring/
│   └── prometheus.yml
├── .github/workflows/ci-cd.yml   # CI/CD pipeline
├── docker-compose.yml            # Full local development stack
├── Dockerfile                    # Multi-stage build
└── pom.xml
```

---

## License

Kayalvizhi Enterprise Java Solutions — @2026 All rights reserved.