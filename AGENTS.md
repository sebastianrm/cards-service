# AGENTS.md — cards-service

> **🤖 FUENTE CANÓNICA PARA AGENTES DE IA — Lógica de negocio de cards-service**
>
> Este `AGENTS.md` es el documento de referencia **definitivo para agentes de IA** sobre la lógica de negocio, las reglas, la arquitectura, el estilo de código y las instrucciones de desarrollo del microservicio `cards-service`.
>
> Para **humanos**, consultar [`README.md`](./README.md) en la misma carpeta.
>
> Los archivos de la raíz del repositorio (`AGENTS.md`, `README.md`, `Proyecto General vf.md`) contienen reglas de oro de plataforma, índice del monorepo y requisitos generales compartidos — **no** la lógica de negocio específica de este servicio.

---

## Project Overview

**cards-service** es el microservicio de **tarjetas de crédito y débito** de la plataforma bancaria **Yanki** (Parte III — Event-driven).

- **Puerto:** `8085`
- **Base de datos:** MongoDB `yanki_cards`
- **Cache:** Redis
- **Mensajería:** Apache Kafka
- **Stack:** Java 17, Spring Boot 3.3.5, Spring WebFlux, RxJava 3, Reactive MongoDB, Reactive Redis, Spring Kafka

**Estado actual:** 🟢 Implementación completa de lógica de negocio, Kafka, Redis y pruebas (auditoría 2026-11-15). Los 13 endpoints implementados con arquitectura hexagonal. Kafka producers y consumers con idempotencia. Redis cache con patrón cache-aside. 116 pruebas pasando (65 unitarias + 51 de integración con mocks). **⚠️ 17 violaciones de bloqueo reactivo** (`.block()`/`.blockingGet()`) detectadas en `CardController` y adaptadores MongoDB — severidad ALTA. Ver [`README.md`](./README.md) para el reporte completo de auditoría.

---

## Setup Commands

- **Install deps:** `mvn clean install -DskipTests`
- **Start dev server:** `mvn spring-boot:run` (port 8085)
- **Run tests:** `mvn test`
- **Full build with quality gate:** `mvn clean verify`
- **Checkstyle only:** `mvn checkstyle:check`
- **JaCoCo report:** `mvn jacoco:prepare-agent jacoco:report` → `target/site/jacoco/index.html`
- **Docker build:** `docker build -t yanki/cards-service:1.0.0 .`

### Required infrastructure

MongoDB, Redis, Kafka, Eureka, Config Server must be running. Use `docker-compose.full.yml` from the monorepo root.

### Environment variables

```powershell
$env:SERVER_PORT=8085
$env:MONGODB_URI="mongodb://localhost:27017/yanki_cards"
$env:REDIS_HOST=localhost
$env:REDIS_PORT=6379
$env:KAFKA_BOOTSTRAP_SERVERS=localhost:9092
$env:KAFKA_GROUP_ID=cards-service
$env:EUREKA_URL=http://localhost:8761/eureka/
$env:CONFIG_SERVER_URL=http://localhost:8888
$env:YANKI_CARD_STANDARD_LIMIT=5000
$env:YANKI_CARD_VIP_LIMIT_MULTIPLIER=2.0
$env:YANKI_CARD_PYME_LIMIT_MULTIPLIER=3.0
```

---

## Code Style

- **Language:** English for class/method/package/URL names. **Spanish for Javadoc** (mandatory on every public class and method).
- **Indentation:** 4 spaces, no tabs, max 160 chars/line.
- **Imports:** Explicit only. **Never** `import ...*;` nor unused imports.
- **Money:** `java.math.BigDecimal`, scale 2, `RoundingMode.HALF_UP`, default currency `PEN`. **Never** `double`/`float` for amounts.
- **Dates:** `java.time.LocalDateTime` / `LocalDate`. Never `java.util.Date`.
- **IDs:** `String` (UUID).
- **Lombok:** `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`.
- **Injection:** Constructor only (`@RequiredArgsConstructor`).
- **Collections:** Lambdas and Streams mandatory.

---

## Architecture Constraints

### ✅ Hexagonal Architecture — VERIFIED

Consult [`../system-prompt-auditoria-hexagonal-yanki.md`](../system-prompt-auditoria-hexagonal-yanki.md) for the full protocol. Before generating or modifying code, **always read that file**.

**Audit status (2026-11-15):**

| Severity | Violation | Status | Resolution |
|---|---|---|---|
| **HIGH** | `.block()`/`.blockingGet()` in CardController | ⚠️ Active | 10 calls converting RxJava3 → Reactor; must use `RxJava3Adapter` instead |
| **HIGH** | `.block()` in MongoDB adapters | ⚠️ Active | 7 calls in `CardMongoAdapter`, `ConsumptionMongoAdapter`, `CustomerProfileViewMongoAdapter`, `CustomerRiskViewMongoAdapter`, `ProcessedEventMongoAdapter` |
| **HIGH** | `@Document`, `@Id` in `domain.model` | ✅ Resolved | Moved to `infrastructure.persistence.document.*Document` |
| **HIGH** | `ReactiveMongoRepository` in `domain.repository` | ✅ Resolved | Moved to `infrastructure.persistence.repository.SpringData*Repository` |
| **HIGH** | `Mono`/`Flux` (Reactor) in `domain.service` | ✅ Resolved | Services use RxJava 3 (`Single`, `Maybe`, `Flowable`) |
| **HIGH** | Missing `domain.port.in` and `domain.port.out` | ✅ Resolved | 13 input ports + 6 output ports (persistence, cache, event publisher) |
| **HIGH** | Missing `infrastructure/` layer | ✅ Resolved | Full layer: document, repository, mapper, adapter, cache, messaging |
| **MEDIUM** | `double` for monetary amounts | ✅ Resolved | `BigDecimal` in `Card` and `Consumption` |
| **MEDIUM** | Wildcard imports | ✅ Resolved | Explicit imports in all files |
| **MEDIUM** | Javadoc in English | ✅ Resolved | Spanish Javadoc in all new files |
| **MEDIUM** | Controller injects service directly | ✅ Resolved | Controller invokes `domain.port.in.*` |
| **MEDIUM** | Checkstyle plugin missing | ⚠️ Active | `pom.xml` lacks `maven-checkstyle-plugin` |
| **MEDIUM** | JaCoCo excludes infrastructure | ⚠️ Active | Only 26 domain classes analyzed; `infrastructure/**` and `interfaces/**` excluded |

**Current package structure (verified):**

```
com.yanki.cards
├── domain
│   ├── model/                 # Pure entities — NO @Document, NO @Id
│   ├── port
│   │   ├── in/                # Use case interfaces (RxJava 3: Single, Maybe, Completable, Flowable)
│   │   └── out/               # Persistence, cache, event publisher ports (RxJava 3)
│   ├── service/               # Use case implementations — depend ONLY on ports
│   ├── event/                 # Domain event payloads (Java records)
│   └── exception/             # BusinessException, ErrorCode
├── infrastructure
│   ├── config/                # RedisConfig, KafkaConfig (OpenAPI en com.yanki.cards.config)
│   ├── persistence
│   │   ├── document/          # @Document classes (CardDocument, etc.)
│   │   ├── repository/        # ReactiveMongoRepository interfaces
│   │   ├── mapper/            # Document ↔ Domain mappers
│   │   └── adapter/           # Implements domain.port.out.* persistence ports
│   ├── cache/adapter/         # Implements CachePort (Redis)
│   └── messaging
│       ├── adapter/           # Implements EventPublisherPort (Kafka producer)
│       └── consumer/          # Kafka listeners → invokes domain.port.in.*
└── interfaces/rest
    ├── controller/            # @RestController → invokes domain.port.in.*
    ├── dto/                   # Request/Response DTOs
    ├── mapper/                # DTO ↔ Domain mappers
    └── GlobalExceptionHandler.java
```

**Rules:**
- Domain must **never** import `org.springframework.data.*`, `org.springframework.kafka.*`, `org.springframework.web.*`, or `reactor.core.*`.
- Domain services use **RxJava 3** (`Single`, `Maybe`, `Completable`, `Flowable`) only.
- Controllers convert between RxJava and Reactor (`Mono`/`Flux`) at the boundary using `RxJava3Adapter`.
- **Never** `.block()`, `.blockingGet()`, `.blockingFirst()`, `Thread.sleep()`.
- **No REST between microservices.** All inter-service communication via Kafka.
- **Idempotency:** All Kafka consumers check `processed_events` with key `<topic>:<entityId>:<occurredAt>`.

---

## Testing Strategy

- **Unit:** ✅ 65 pruebas implementadas — Domain services with mocked `domain.port.out.*` + RxJava3 `TestObserver`
  - 13 test classes covering all 13 domain services
  - Happy path + error scenarios for each use case
  - Key business rules tested:
    - Credit card issuance with overdue debt blocking
    - Debit card account validation
    - Consumption credit limit validation
    - Refund eligibility (only AUTHORIZED status)
    - Card state transitions (block/unblock/close)
- **Integration:** ✅ 51 pruebas implementadas con mocks — Controller, adaptadores MongoDB, mappers, GlobalExceptionHandler
  - 17 pruebas de Controller REST (`WebTestClient` + mocks de `domain.port.in.*`)
  - 14 pruebas de adaptadores MongoDB (`CardMongoAdapter`, `ConsumptionMongoAdapter` con mocks de `SpringData*Repository`)
  - 14 pruebas de mappers (`CardDocumentMapper`, `ConsumptionDocumentMapper` — round-trip, null handling, enum conversion)
  - 6 pruebas de `GlobalExceptionHandler` (BusinessException, IllegalArgumentException, IllegalStateException, unexpected)
- **Event:** ✅ Implementado — Kafka producers y consumers con idempotencia
  - `CardKafkaEventAdapter` publica 7 tipos de evento (CardOpened, CardClosed, CardBlocked, CardUnblocked, ConsumptionRegistered, ConsumptionRefunded, PaymentRegistered)
  - `CustomerProfileConsumer` consume CustomerRegistered/Updated con idempotencia via `processed_events`
  - `AccountClosedConsumer` consume AccountClosed y cierra tarjetas débito vinculadas
  - `CardRedisCacheAdapter` con caché de tarjeta completa (TTL 5m) y límite (TTL 2m)
  - Patrón cache-aside en `GetCardServiceImpl` y `GetCardLimitServiceImpl`
- **Contract:** OpenAPI/Swagger fully documented (springdoc 2.6.0)
  - `OpenAPIConfig` in `com.yanki.cards.config` with API info, servers, contact
  - All 13 endpoints documented with `@Operation`, `@Parameter`, `@ApiResponses`
  - All 10 DTOs documented with `@Schema` (descriptions, examples, allowable values)
  - Tags: Cards, Consumptions, Payments
  - Access: `/swagger-ui.html` and `/v3/api-docs`
  - Standalone spec: `API/cards-service.openapi.yml` — OpenAPI 3.2.1 YAML with all 13 endpoints, 10 schemas, request/response examples, and error examples
- **Postman Collection:** ✅ Complete collection in `postman/` directory
  - `postman/Yanki-Cards-Service-API.postman_collection.json` — 13 endpoints with automated tests, variables, and example responses
  - `postman/README.md` — Usage guide, error codes table, and test cases references
  - Validated against `CardController.java`, `GlobalExceptionHandler.java`, `ErrorCode.java`, and all DTOs

 ---

## Boundaries

### Files the agent MUST NOT modify without explicit instruction

- `../AGENTS.md` — Root platform rules
- `../system-prompt-auditoria-hexagonal-yanki.md` — Hexagonal architecture standard
- `pom.xml` — Unless adding dependencies explicitly requested

### Database boundaries

- **Database-per-service:** Only `yanki_cards`. Never access other services' databases.
- **Collections:** `cards`, `consumptions`, `customer_profile_view`, `customer_risk_view`, `processed_events`

### Kafka topics

**Published (7):**
- `yanki.card.opened.v1`, `yanki.card.closed.v1`, `yanki.card.blocked.v1`, `yanki.card.unblocked.v1`, `yanki.card.consumption.registered.v1`, `yanki.card.consumption.refunded.v1`, `yanki.card.payment.registered.v1`

**Consumed (4):**
- `yanki.customer.registered.v1`, `yanki.customer.updated.v1`, `yanki.customer.deleted.v1`, `yanki.account.closed.v1`

### Redis cache

- **Key pattern:** `yanki:cards:card:{cardId}` (TTL 5m) y `yanki:cards:limit:{cardId}` (TTL 2m)
- **Invalidation:** On block, unblock, close, consumption, payment, refund
- **Config:** `yanki.cache.keyPrefix`, `yanki.cache.cardTtl`, `yanki.cache.limitTtl` in `CacheProperties`

---

## Business Rules (Critical)

| Rule | Detail |
|---|---|
| Personal credit: max 1 active card | Count by customerId + CREDIT + PERSONAL + ACTIVE/ISSUED |
| Enterprise credit: unlimited | No quantity limit |
| Debit: max 1 per account | Count by linkedAccountId + DEBIT |
| Debit: accountId required | Reject if null |
| Credit issuance: customer ACTIVE | Check `customer_profile_view` |
| Credit issuance: no overdue debt | Check `customer_risk_view.hasOverdueDebt` |
| Debit issuance: account OPEN | Verify linked account |
| Debit issuance: account belongs to customer | Verify customerId match |
| Credit consumption: amount ≤ availableLimit | From Redis or recalculate from MongoDB |
| Credit consumption: card ACTIVE, not expired | Validate status and expiryDate |
| Debit consumption: 202 Accepted | Async result via `debit.payment.settled.v1` |
| Credit payment: increases availableLimit | Invalidate Redis cache |
| Block: only ACTIVE cards | Reject if BLOCKED, CLOSED, EXPIRED |
| Unblock: only BLOCKED cards | Reject if not blocked |
| Close: credit with zero balance | Reject if balance ≠ 0 |
| Close: not already CLOSED | Reject if already closed |
| Risk: overdue debt blocks credit issuance | `credit.overdue.v1` → `customer_risk_view` |
| Top-up: async orchestration | wallet → cards → accounts → cards → wallet |
| Monthly billing: AUTHORIZED → SETTLED | Scheduled process per period |

---

## REST Endpoints

Base path: `/api/cards`

| Method | Endpoint | Status |
|---|---|---|
| `POST` | `/api/cards` | ✅ Implemented |
| `GET` | `/api/cards` | ✅ Implemented |
| `GET` | `/api/cards/{id}` | ✅ Implemented |
| `GET` | `/api/cards/{id}/limit` | ✅ Implemented |
| `GET` | `/api/cards/{id}/consumptions` | ✅ Implemented |
| `GET` | `/api/cards/{id}/consumptions/{consumptionId}` | ✅ Implemented |
| `POST` | `/api/cards/{id}/consumptions` | ✅ Implemented |
| `POST` | `/api/cards/{id}/consumptions/{consumptionId}/refund` | ✅ Implemented |
| `GET` | `/api/cards/{id}/statement` | ✅ Implemented |
| `POST` | `/api/cards/{id}/payments` | ✅ Implemented |
| `PUT` | `/api/cards/{id}/block` | ✅ Implemented |
| `PUT` | `/api/cards/{id}/unblock` | ✅ Implemented |
| `DELETE` | `/api/cards/{id}` | ✅ Implemented |

---

## Error Handling

Unified `ApiError` response: `timestamp`, `status`, `error`, `code`, `message`, `path`, `details`.

Key error codes: `CARD_NOT_FOUND` (404), `CARD_BLOCKED` (403), `CARD_EXPIRED` (403), `CUSTOMER_HAS_OVERDUE_DEBT` (403), `PERSONAL_CREDIT_CARD_ALREADY_EXISTS` (409), `DEBIT_CARD_ALREADY_EXISTS_FOR_ACCOUNT` (409), `INSUFFICIENT_CREDIT_LIMIT` (400), `INVALID_AMOUNT` (400), `OUTSTANDING_BALANCE` (409).

Full catalog in [`README.md`](./README.md#8-cobertura-http-por-endpoint).

---

## Security

- **Auth:** JWT validation delegated to api-gateway (not local).
- **Authorization:** Roles via `X-User-Roles` header (ADMIN, OPERATOR, CUSTOMER).
- **Data protection:** CVV stored as hash (`cvvHash`), never plaintext.
- **Card data:** `cardNumber` unique index; never log full card numbers.

---

## Observability

- **Health:** `GET /actuator/health` (Mongo, Redis, Kafka components)
- **Metrics:** `GET /actuator/prometheus`
- **Logging:** JSON structured with `traceId`, `spanId` (W3C TraceContext)
- **Tracing:** `traceparent`/`tracestate` header propagation, 10% sampling

---

## References

- [`README.md`](./README.md) — Business logic, API docs, data model, Kafka events, Redis cache, audit report.
- [`../AGENTS.md`](../AGENTS.md) — Root platform rules (traceability + hexagonal architecture golden rules).
- [`../system-prompt-auditoria-hexagonal-yanki.md`](../system-prompt-auditoria-hexagonal-yanki.md) — **Mandatory** hexagonal architecture audit protocol.
- [`../CONVENTIONS.md`](../CONVENTIONS.md) — Global technical contract for all microservices.
- [`../CASOS_USO/CU-04-Gestion-Tarjetas.md`](../CASOS_USO/CU-04-Gestion-Tarjetas.md) — Card management use case.
- [`../CASOS_USO/CU-07-Consumos-Tarjetas.md`](../CASOS_USO/CU-07-Consumos-Tarjetas.md) — Card consumption use case.
- [`../CASOS_PRUEBA/RTM.md`](../CASOS_PRUEBA/RTM.md) — Traceability matrix (CU ↔ SEC ↔ CP).
- [`CASOS_PRUEBA/MOCKS.md`](CASOS_PRUEBA/MOCKS.md) — Mock dependencies for testing.
- [`postman/README.md`](postman/README.md) — Postman collection documentation and usage guide.
- [`postman/Yanki-Cards-Service-API.postman_collection.json`](postman/Yanki-Cards-Service-API.postman_collection.json) — Importable Postman collection.
- [`API/cards-service.openapi.yml`](API/cards-service.openapi.yml) — Standalone OpenAPI 3.2.1 specification (YAML) with all 13 endpoints, 10 schemas, and examples.
