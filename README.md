# Order Ops — event-driven e-commerce platform

A real, runnable microservices system: three Spring Boot services talk to each other
exclusively through Kafka, backed by two different databases, fully traced end-to-end,
with a Next.js live ops console on top and a streaming ETL pipeline feeding an
analytics warehouse underneath.

## Architecture

```
Next.js dashboard ──HTTP/SSE──▶ order-service (Spring Boot, Postgres)
                                       │  publishes order.created
                                       ▼
                                     Kafka
                                       │  consumed by
                                       ▼
                              inventory-service (Spring Boot, Postgres)
                                       │  publishes inventory.reserved / .rejected
                                       ▼
                                     Kafka
                                       │  consumed by
                                       ▼
                               payment-service (Spring Boot, MySQL)
                                       │  publishes payment.processed / .failed
                                       ▼
                                     Kafka
                          ┌────────────┴────────────┐
                          ▼                          ▼
                 order-service (closes            etl-service
                 the saga loop, updates    (extracts every event, loads a
                 status, pushes to           star-schema fact table,
                 the dashboard via SSE)       rolls up daily revenue)

  All four services export OpenTelemetry traces → Jaeger
  All four services export Prometheus metrics    → Grafana
```

This is **choreography, not orchestration** — there's no central coordinator. Each
service reacts only to the event in front of it, which is what makes it a genuine
demonstration of event-driven design rather than "REST calls with a message queue
strapped on."

### Why the design choices are real, not decorative

- **Two different databases** (Postgres for order/inventory, MySQL for payment) —
  demonstrates working across relational engines, not just one ORM config.
- **Optimistic locking** (`@Version` on `Product`) — concurrent orders can't oversell
  the same SKU.
- **Saga compensation** — a failed payment or a stock-out doesn't leave the order
  stuck; `order-service` listens for the failure event and moves the order to a
  terminal state.
- **Cross-service JSON contracts, not shared Java types** — each service defines its
  own copy of the event DTOs and Kafka's type-info headers are disabled
  (`KafkaConsumerConfig` in each service). This is deliberate: it's what a real
  polyglot system looks like, and it's a good interview talking point ("how do you
  version an event schema across services you don't own?").
- **ETL is a separate consumer group reading the same topics**, not a batch job that
  reaches into another service's database. It denormalizes into a fact table
  (`fact_order_line`) and rolls it up on a schedule (`DailyAggregationJob`) — a small
  but real extract → transform → load pipeline.

## Run it

Requires Docker and Docker Compose.

```bash
docker compose up --build
```

First build takes a few minutes (Maven pulls dependencies per service). Once healthy:

| Service              | URL                              |
|----------------------|-----------------------------------|
| Dashboard             | http://localhost:3000            |
| Order API             | http://localhost:8081/api/orders |
| Inventory API          | http://localhost:8082/api/products |
| Payment API            | http://localhost:8083/api/payments |
| Analytics API (ETL)    | http://localhost:8084/api/analytics/revenue-by-product |
| Kafka UI               | http://localhost:8085            |
| Jaeger (traces)        | http://localhost:16686           |
| Prometheus             | http://localhost:9090            |
| Grafana                | http://localhost:3001 (admin/admin) |

Open the dashboard, place an order, and watch it move through
`pending → stock reserved → confirmed` live via SSE. Then open Jaeger and search for
`order-service` — you'll see one trace spanning all three services and the Kafka hops
between them.

## Local (non-Docker) development

Each Spring Boot service is a standalone Maven project:

```bash
cd order-service
mvn spring-boot:run
```

You'll need Kafka + Postgres + MySQL running locally (or just run those pieces via
`docker compose up kafka postgres mysql jaeger`) and point each service's
`application.yml` at `localhost`.

For the frontend:

```bash
cd frontend
npm install
npm run dev
```

## What to say about this on a resume

- Designed and built an event-driven e-commerce platform with three Spring Boot
  microservices communicating exclusively via Kafka, implementing a choreography-based
  saga with automatic compensation on inventory or payment failure
- Built a real-time Next.js dashboard consuming live order-state events over
  Server-Sent Events, backed by a Spring Boot REST API
- Instrumented all services with OpenTelemetry distributed tracing (Jaeger) and
  Prometheus/Grafana metrics dashboards, including Kafka consumer lag and per-service
  request latency
- Built a streaming ETL pipeline that denormalizes Kafka order events into a
  star-schema analytics table and a scheduled aggregation job, exposed via a
  reporting API
- Worked across two relational database engines (PostgreSQL, MySQL) in the same
  system, with optimistic locking to prevent overselling under concurrent load

## Natural next steps (good "what would you add" answers in an interview)

- An API gateway (Spring Cloud Gateway) in front of the three services instead of the
  frontend calling each directly
- Dead-letter topics + retry policies on the Kafka consumers
- Outbox pattern instead of dual-write (currently each service writes to its DB, then
  publishes — an outbox table would make that atomic)
- Contract testing between services (e.g. Pact) for the event schemas
- Kubernetes manifests / Helm chart instead of docker-compose for a production story
