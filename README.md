# cards-service

> **📌 FUENTE CANÓNICA PARA HUMANOS — Lógica de negocio de cards-service**
>
> Este `README.md` es el documento de referencia **definitivo para humanos** sobre la lógica de negocio, las reglas, los endpoints, el modelo de datos y la arquitectura del microservicio `cards-service`.
>
> Para **agentes de IA**, consultar [`AGENTS.md`](./AGENTS.md) en la misma carpeta.
>
> Los archivos de la raíz del repositorio (`AGENTS.md`, `README.md`, `Proyecto General vf.md`) contienen reglas de oro de plataforma, índice del monorepo y requisitos generales compartidos — **no** la lógica de negocio específica de este servicio.

Microservicio de **tarjetas de crédito y débito** de la plataforma bancaria **Yanki**.

Gestiona la **emisión de tarjetas**, **consumos**, **pagos**, **top-up wallet**, **consulta de límites**, **bloqueo/desbloqueo**, **cierre de tarjetas** y la **coordinación de pagos débito** en una arquitectura **event-driven**. Este documento está diseñado para que un agente pueda **implementar el microservicio de forma autónoma** sin depender de información externa adicional.

- **Puerto:** `8085`
- **Base de datos propia:** MongoDB `yanki_cards`
- **Colecciones principales:** `cards`, `consumptions`, `customer_profile_view`, `customer_risk_view`, `processed_events`
- **Cache:** Redis
- **Mensajería:** Apache Kafka
- **Descubrimiento:** Eureka
- **Configuración externa:** Config Server
- **Estilo arquitectónico:** Reactive + Database-per-service + Event-driven
- **Estado esperado:** Servicio completo de Parte III (Event-driven)

---

## ✅ Estado Actual de Desarrollo (Auditoría 2026-11-22)

> **Estado:** 🟢 **Implementación completa de lógica de negocio** — 13 endpoints implementados con arquitectura hexagonal.
> **Tests:** 149 pruebas en 22 clases de test. BUILD SUCCESS.
> **✅ Sin violaciones activas:** Cero operaciones de bloqueo reactivo — `CardController` usa `RxJava3Adapter` y los adaptadores MongoDB son completamente reactivos.

| Componente | Estado | Detalle |
|---|---|---|
| **Modelo de dominio** | ✅ Completo | 8 entidades/enum puras, sin @Document, sin @Id, BigDecimal para montos |
| **Puertos hexagonales** | ✅ Completo | 15 puertos de entrada (todos los use cases) + 6 puertos de salida (persistencia, cache, eventos) |
| **Capa infrastructure** | ✅ Completo | 5 documentos MongoDB, 5 repositorios Spring Data, 5 mappers, 5 adaptadores de puertos |
| **Servicios de dominio** | ✅ Completo | 14 implementaciones de use case con RxJava 3 cubriendo toda la lógica de negocio |
| **Controladores REST** | ✅ Completo | `CardController` con 13 endpoints usando puertos de entrada |
| **DTOs** | ✅ Completo | 10 DTOs (requests y responses) + ApiError + 2 mappers REST |
| **ErrorCode** | ✅ Completo | 27 códigos de error cubriendo todos los escenarios |
| **GlobalExceptionHandler** | ✅ Completo | Implementado correctamente |
| **Kafka producers** | ✅ Completo | `CardKafkaEventAdapter` publica 7 tipos de eventos (CardOpened, CardClosed, CardBlocked, CardUnblocked, ConsumptionRegistered, ConsumptionRefunded, PaymentRegistered) |
| **Kafka consumers** | ✅ Completo | `CustomerProfileConsumer` (CustomerRegistered/Updated) + `AccountClosedConsumer` con idempotencia via `processed_events` |
| **Redis cache** | ✅ Completo | `CardRedisCacheAdapter` con caché de tarjeta completa y límite disponible (TTL configurable) |
| **Tests unitarios** | ✅ Completo | 149 pruebas JUnit 5 con Mockito cubriendo 14 servicios de dominio, adaptadores, mappers, consumers, Controller y GlobalExceptionHandler |
| **Tests de integración** | ✅ Con mocks | Controller (WebTestClient), adaptadores MongoDB, mappers, GlobalExceptionHandler, CustomerProfileConsumer |
| **Docker** | ✅ Completo | `Dockerfile` presente |
| **OpenAPI/Swagger** | ✅ Completo | springdoc 2.6.0, 13 endpoints documentados, spec YAML standalone |
| **Postman** | ✅ Completo | Colección con 13 endpoints, tests automatizados |

### Arquitectura Hexagonal — Estado de Auditoría (2026-11-22)

| Severidad | Hallazgo | Estado | Detalle |
|---|---|---|---|
| ✅ Resuelto | `.block()`/`.blockingGet()` en CardController | ✅ | `CardController` usa `RxJava3Adapter.singleToMono`/`completableToMono` para convertir RxJava3 → Reactor |
| ✅ Resuelto | `.block()` en adaptadores MongoDB | ✅ | Adaptadores MongoDB son completamente reactivos sin bloqueo |
| ✅ Resuelto | `@Document` en dominio | ✅ | Entidades puras en `domain.model`; `*Document` en `infrastructure.persistence.document` |
| ✅ Resuelto | `@Id` en dominio | ✅ | Identificadores sin anotaciones en dominio; `@Id` solo en documentos |
| ✅ Resuelto | `ReactiveMongoRepository` en dominio | ✅ | Repositorios Spring Data en `infrastructure.persistence.repository` |
| ✅ Resuelto | `Mono`/`Flux` en servicio de dominio | ✅ | Servicios usan RxJava 3 (`Single`, `Maybe`, `Flowable`) |
| ✅ Resuelto | Dominio importa infraestructura | ✅ | Cero imports de `org.springframework.data/web/kafka` o `reactor.core` en `domain/` |
| ✅ Resuelto | `double` para montos | ✅ | `BigDecimal` en `Card` y `Consumption` |
| ✅ Resuelto | Controlador acoplado a servicio | ✅ | Controller invoca `domain.port.in.*` |
| ✅ Resuelto | Javadoc en español | ✅ | Javadoc en español en todos los archivos |
| ✅ Resuelto | Idempotencia Kafka consumers | ✅ | `processed_events` con key `<topic>:<entityId>:<occurredAt>` |
| ⚠️ Mejora | Checkstyle plugin ausente | ⚠️ | `pom.xml` sin plugin `maven-checkstyle-plugin` |
| ⚠️ Mejora | JaCoCo excluye capas externas | ⚠️ | `infrastructure/**` e `interfaces/**` excluidas del reporte JaCoCo |

### Endpoints Implementados (13/13)

| Método | Endpoint | Estado | Use Case |
|---|---|---|---|
| `POST` | `/api/cards` | ✅ | `IssueCardUseCase` |
| `GET` | `/api/cards` | ✅ | `FindCardsUseCase` |
| `GET` | `/api/cards/{id}` | ✅ | `GetCardUseCase` |
| `GET` | `/api/cards/{id}/limit` | ✅ | `GetCardLimitUseCase` |
| `GET` | `/api/cards/{id}/consumptions` | ✅ | `GetConsumptionsUseCase` |
| `GET` | `/api/cards/{id}/consumptions/{consumptionId}` | ✅ | `GetConsumptionDetailUseCase` |
| `POST` | `/api/cards/{id}/consumptions` | ✅ | `RegisterConsumptionUseCase` |
| `POST` | `/api/cards/{id}/consumptions/{consumptionId}/refund` | ✅ | `RefundConsumptionUseCase` |
| `GET` | `/api/cards/{id}/statement` | ✅ | `GetStatementUseCase` |
| `POST` | `/api/cards/{id}/payments` | ✅ | `RegisterPaymentUseCase` |
| `PUT` | `/api/cards/{id}/block` | ✅ | `BlockCardUseCase` |
| `PUT` | `/api/cards/{id}/unblock` | ✅ | `UnblockCardUseCase` |
| `DELETE` | `/api/cards/{id}` | ✅ | `CloseCardUseCase` |

### Componentes Faltantes (Mejoras futuras)

1. **Tests de integración**: Tests con `@SpringBootTest` y `Testcontainers` para MongoDB, Kafka y Redis
2. **Monitoreo avanzado**: Métricas de consumo de Kafka y hit rate de Redis
3. **Dead Letter Queue**: Manejo de mensajes Kafka envenenados con reintentos

---

## 1. Fuentes de Verdad Utilizadas

Este README consolida la información relevante proveniente de:

- `README.md` raíz del monorepo
- `CONVENTIONS.md`
- `docs/EVENTOS.md`
- `../docs/CASOS_USO`
- `../docs/CASOS_USO`
- `CASOS_PRUEBA/CASOS_PRUEBA_QA.md`
- `cards-service/CASOS_PRUEBA/*.md`
- `init-mongo-parte1.js`

Toda decisión de diseño e implementación de este microservicio debe alinearse con estas fuentes.

---

## 2. Objetivo del Microservicio

| Responsabilidad | Detalle |
|---|---|
| Emisión de tarjetas | Crédito personal, crédito empresarial y débito |
| Administración de tarjetas | Consulta, bloqueo, desbloqueo y cierre |
| Consumos | Registro de consumos crédito y débito |
| Pagos | Pago de tarjeta de crédito y solicitud asíncrona de débito |
| Top-up wallet | Orquestación con wallet-service y accounts-service |
| Límites | Cálculo, consulta e invalidación de límite disponible |
| Integración | Publicación y consumo de eventos Kafka |
| Vistas locales | Perfil y riesgo del cliente para reglas de negocio |
| Riesgo | Bloqueo por deuda vencida |
| Idempotencia | Control de procesamiento de eventos inbound |

---

## 3. Stack Tecnológico Obligatorio

| Capa | Tecnología | Versión / Observación |
|---|---|---|
| Lenguaje | Java | 17 |
| Framework | Spring Boot | 3.3.5 |
| Cloud | Spring Cloud | 2023.0.3 |
| Web reactiva | Spring WebFlux | Endpoints no bloqueantes |
| Persistencia | Spring Data Reactive MongoDB | Sin `@Query` |
| Cache | Spring Data Redis Reactive | TTL y recalculo |
| Eventos | Spring Kafka | JSON, producer/consumer |
| Reactividad adicional | RxJava 3 | Permitido en capa de servicios |
| Documentación | springdoc-openapi | Swagger UI + `/v3/api-docs` |
| Resiliencia | Resilience4j | Circuit breaker 2s si aplica |
| Calidad | Checkstyle / JaCoCo | Cero errores / cobertura |
| Utilidades | Lombok | Constructor injection y builders |
| Testing | JUnit 5, Mockito, reactor-test | StepVerifier |
| Build | Maven | Proyecto independiente |

### Restricciones técnicas obligatorias

1. **Database-per-service**: solo puede usar `yanki_cards`.
2. **No REST entre microservicios nuevos de Parte III**: comunicación por Kafka.
3. **Sin `block()` ni `Thread.sleep()`** en flujos productivos.
4. **Sin `@Query`** en repositorios; usar métodos derivados y `ReactiveMongoTemplate` para agregaciones.
5. **Toda configuración por environment variables o Config Server**.
6. **Nombres en inglés** para clases, métodos y endpoints.
7. **Javadoc en español**.
8. **Error contract unificado** con `ApiError`.

---

## 4. Alcance Funcional Derivado de Casos de Uso

Este microservicio cubre principalmente:

- **CU-04 Gestión de Tarjetas**
- **CU-07 Consumos en Tarjetas de Crédito**

Y participa en flujos relacionados con:

- Wallet Yanki (`wallet.topup.requested/settled`)
- Bloqueo transversal por deuda vencida (`credit.overdue`)
- Requisitos indirectos de perfiles VIP/PYME

### Casos QA del catálogo global directamente relacionados

- **CP-024** Emisión de tarjeta de crédito válida
- **CP-025** Emisión de tarjeta de débito asociada a cuenta
- **CP-026** Consumo con tarjeta de crédito dentro del límite
- **CP-027** Rechazo de consumo por exceder límite
- **CP-028** Pago con tarjeta de débito exitoso
- **CP-029** Pago con tarjeta de débito rechazado por deuda vencida

### Casos documentados localmente

- `CP-001-emision-tarjeta-credito-personal-exitosa.md`
- `CP-002-emision-tarjeta-debito-exitosa.md`
- `CP-003-emision-tarjeta-credito-rechazada-por-deuda-vencida.md`

---

## 5. Modelo de Dominio

### 5.1 Agregados principales

#### Card
Representa una tarjeta emitida por el sistema.

Campos mínimos:
- `id`
- `customerId`
- `type` → `CREDIT | DEBIT`
- `subtype` → `PERSONAL | ENTERPRISE | null`
- `status` → `ISSUED | ACTIVE | BLOCKED | EXPIRED | CLOSED`
- `cardNumber`
- `expiryDate`
- `cvvHash`
- `creditLimit`
- `availableLimit`
- `linkedAccountId`
- `issuedAt`
- `activatedAt`
- `blockedAt`
- `blockReason`
- `closedAt`
- `createdAt`
- `updatedAt`

#### Consumption
Representa un consumo generado por una tarjeta.

Campos mínimos:
- `id`
- `cardId`
- `customerId`
- `amount`
- `merchantId`
- `description`
- `status` → `AUTHORIZED | CONFIRMED | SETTLED | REJECTED | REFUNDED`
- `paymentId`
- `occurredAt`
- `settledAt`
- `createdAt`
- `updatedAt`

#### CustomerProfileView
Vista local del perfil del cliente.

Campos mínimos:
- `customerId`
- `customerType`
- `profile`
- `status`
- `updatedAt`

#### CustomerRiskView
Vista local de riesgo del cliente.

Campos mínimos:
- `customerId`
- `hasOverdueDebt`
- `updatedAt`

#### ProcessedEvent
Control de idempotencia para eventos Kafka.

Campos mínimos:
- `id`
- `topic`
- `entityId`
- `occurredAt`
- `processedAt`

### 5.2 Enums recomendados

- `CardType`
- `CardSubtype`
- `CardStatus`
- `ConsumptionStatus`
- `CustomerProfile`
- `CustomerType`
- `ErrorCode`

---

## 6. Reglas de Negocio

### 6.1 Emisión de tarjeta de crédito

Para emitir una tarjeta de crédito se debe validar que:

1. Exista `customer_profile_view`.
2. El cliente esté `ACTIVE`.
3. No exista deuda vencida en `customer_risk_view`.
4. Si es `PERSONAL`, el cliente no tenga otra tarjeta de crédito personal vigente.
5. Si es `ENTERPRISE`, se permitan múltiples tarjetas.
6. El límite solicitado sea válido y mayor a cero.
7. Se calcule el límite inicial según perfil del cliente si la política lo requiere.
8. Se persista la tarjeta.
9. Se publique `yanki.card.opened.v1`.
10. Se genere cache Redis del límite si aplica.

Resultado:
- Estado final: `ISSUED`
- `availableLimit = creditLimit`

### 6.2 Emisión de tarjeta de débito

Validaciones:

1. `accountId` es obligatorio.
2. La cuenta existe.
3. La cuenta está `OPEN`.
4. La cuenta pertenece al cliente.
5. La cuenta no tiene otra tarjeta débito.
6. Se persiste la tarjeta.
7. Se publica `yanki.card.opened.v1`.

Resultado:
- Estado final: `ACTIVE`
- `linkedAccountId` obligatorio

### 6.3 Consumo con tarjeta de crédito

Validaciones:

1. Tarjeta existe.
2. Tarjeta está `ACTIVE`.
3. Tipo `CREDIT`.
4. No está vencida.
5. No está bloqueada.
6. Se obtiene límite de Redis o se recalcula desde MongoDB.
7. `amount <= availableLimit`.
8. Se registra consumo `AUTHORIZED`.
9. Se descuenta/actualiza límite disponible.
10. Se publica `yanki.card.consumption.registered.v1`.

### 6.4 Consumo con tarjeta de débito

Validaciones:

1. Tarjeta existe.
2. Tarjeta está `ACTIVE`.
3. Tipo `DEBIT`.
4. Tiene cuenta vinculada.
5. Se genera `paymentId`.
6. Se publica `yanki.debit.payment.requested.v1`.
7. El resultado se procesa de forma asíncrona mediante `yanki.debit.payment.settled.v1`.

Resultado:
- Respuesta REST inicial `202 Accepted`
- Estado final del consumo: `CONFIRMED` o `REJECTED`

### 6.5 Pago de tarjeta de crédito

Validaciones:

1. Tarjeta existe.
2. Tipo `CREDIT`.
3. Estado `ACTIVE`.
4. Monto positivo.
5. Si viene `sourceAccountId`, se solicita débito por evento.
6. Si es pago directo, se registra internamente.
7. Se incrementa `availableLimit`.
8. Se invalida cache Redis.
9. Se registra movimiento/pago propio del dominio si el diseño lo incluye.

### 6.6 Top-up wallet

Flujo:

1. Consumir `yanki.wallet.topup.requested.v1`.
2. Validar tarjeta débito.
3. Publicar `yanki.debit.payment.requested.v1`.
4. Esperar `yanki.debit.payment.settled.v1`.
5. Publicar `yanki.wallet.topup.settled.v1` con estado final.

### 6.7 Bloqueo / desbloqueo

- Solo una tarjeta operable puede bloquearse.
- Una tarjeta `BLOCKED` puede desbloquearse a `ACTIVE`.
- Una tarjeta `CLOSED` no puede cambiar de estado operativo.
- Registrar auditoría de motivo y fecha.

### 6.8 Cierre

- Tarjeta debe existir.
- No debe estar cerrada.
- Crédito no debe tener deuda pendiente.
- Emitir `yanki.card.closed.v1`.

### 6.9 Riesgo transversal

- `credit.overdue.v1` actualiza `customer_risk_view`.
- Si `hasOverdueDebt = true`, se bloquea emisión de nuevas tarjetas de crédito.
- El riesgo afecta también operaciones asociadas en flujos de débito/top-up si el proceso negocio lo exige.

---

## 7. Endpoints REST Expuestos

Base path: `/api/cards`

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/cards` | Emitir tarjeta |
| `GET` | `/api/cards` | Listar tarjetas |
| `GET` | `/api/cards/{id}` | Obtener detalle de tarjeta |
| `GET` | `/api/cards/{id}/limit` | Consultar límite disponible |
| `GET` | `/api/cards/{id}/consumptions` | Listar consumos |
| `GET` | `/api/cards/{id}/consumptions/{consumptionId}` | Obtener detalle de consumo |
| `POST` | `/api/cards/{id}/consumptions` | Registrar consumo |
| `POST` | `/api/cards/{id}/consumptions/{consumptionId}/refund` | Reverso/devolución |
| `GET` | `/api/cards/{id}/statement` | Estado de cuenta mensual |
| `POST` | `/api/cards/{id}/payments` | Registrar pago de tarjeta crédito |
| `PUT` | `/api/cards/{id}/block` | Bloquear tarjeta |
| `PUT` | `/api/cards/{id}/unblock` | Desbloquear tarjeta |
| `DELETE` | `/api/cards/{id}` | Cerrar tarjeta |

### 7.1 Observación de diseño
Aunque algunos endpoints no fueron mencionados en la primera versión resumida del servicio, **sí aparecen en CU-07** y deben formar parte de la especificación implementable:
- detalle de consumo
- reverso/refund
- statement mensual

---

## 8. Cobertura HTTP por Endpoint

### 8.1 `POST /api/cards`
#### Éxitos
- `201 Created` emisión de crédito
- `201 Created` emisión de débito

#### Errores
- `400 Bad Request` `ACCOUNT_REQUIRED_FOR_DEBIT`
- `400 Bad Request` `INVALID_CREDIT_LIMIT`
- `403 Forbidden` `CUSTOMER_HAS_OVERDUE_DEBT`
- `404 Not Found` `CUSTOMER_NOT_FOUND`
- `404 Not Found` `ACCOUNT_NOT_FOUND`
- `409 Conflict` `PERSONAL_CREDIT_CARD_ALREADY_EXISTS`
- `409 Conflict` `DEBIT_CARD_ALREADY_EXISTS_FOR_ACCOUNT`
- `422 Unprocessable Entity` `ACCOUNT_CUSTOMER_MISMATCH`
- `422 Unprocessable Entity` `INVALID_CARD_TYPE`

### 8.2 `GET /api/cards`
#### Éxitos
- `200 OK` lista normal o vacía

#### Errores
- `400 Bad Request` `INVALID_FILTERS`

### 8.3 `GET /api/cards/{id}`
#### Éxitos
- `200 OK`

#### Errores
- `404 Not Found` `CARD_NOT_FOUND`

### 8.4 `GET /api/cards/{id}/limit`
#### Éxitos
- `200 OK` desde cache o recalculado

#### Errores
- `404 Not Found` `CARD_NOT_FOUND`
- `422 Unprocessable Entity` `LIMIT_NOT_APPLICABLE`

### 8.5 `GET /api/cards/{id}/consumptions`
#### Éxitos
- `200 OK` con resultados
- `200 OK` vacío

#### Errores
- `404 Not Found` `CARD_NOT_FOUND`

### 8.6 `GET /api/cards/{id}/consumptions/{consumptionId}`
#### Éxitos
- `200 OK`

#### Errores
- `404 Not Found` `CARD_NOT_FOUND`
- `404 Not Found` `CONSUMPTION_NOT_FOUND`

### 8.7 `POST /api/cards/{id}/consumptions`
#### Éxitos
- `201 Created` consumo crédito autorizado
- `202 Accepted` consumo débito pendiente de liquidación

#### Errores
- `400 Bad Request` `INVALID_AMOUNT`
- `400 Bad Request` `INSUFFICIENT_CREDIT_LIMIT`
- `403 Forbidden` `CARD_BLOCKED`
- `403 Forbidden` `CARD_EXPIRED`
- `404 Not Found` `CARD_NOT_FOUND`
- `409 Conflict` `CARD_ALREADY_CLOSED`
- `422 Unprocessable Entity` `INVALID_CARD_TYPE`

### 8.8 `POST /api/cards/{id}/consumptions/{consumptionId}/refund`
#### Éxitos
- `200 OK` reverso aplicado
- `202 Accepted` reverso asíncrono requerido

#### Errores
- `404 Not Found` `CARD_NOT_FOUND`
- `404 Not Found` `CONSUMPTION_NOT_FOUND`
- `409 Conflict` `CONSUMPTION_NOT_REFUNDABLE`

### 8.9 `GET /api/cards/{id}/statement`
#### Éxitos
- `200 OK` estado de cuenta disponible

#### Errores
- `404 Not Found` `CARD_NOT_FOUND`
- `404 Not Found` `STATEMENT_NOT_FOUND`
- `422 Unprocessable Entity` `INVALID_CARD_TYPE`

### 8.10 `POST /api/cards/{id}/payments`
#### Éxitos
- `201 Created`

#### Errores
- `400 Bad Request` `INVALID_AMOUNT`
- `400 Bad Request` `DEBIT_PAYMENT_REJECTED`
- `404 Not Found` `CARD_NOT_FOUND`
- `409 Conflict` `CARD_ALREADY_CLOSED`
- `422 Unprocessable Entity` `INVALID_CARD_TYPE`

### 8.11 `PUT /api/cards/{id}/block`
#### Éxitos
- `200 OK`

#### Errores
- `404 Not Found` `CARD_NOT_FOUND`
- `409 Conflict` `CARD_ALREADY_BLOCKED`
- `409 Conflict` `CARD_ALREADY_CLOSED`

### 8.12 `PUT /api/cards/{id}/unblock`
#### Éxitos
- `200 OK`

#### Errores
- `404 Not Found` `CARD_NOT_FOUND`
- `409 Conflict` `CARD_NOT_BLOCKED`
- `409 Conflict` `CARD_ALREADY_CLOSED`

### 8.13 `DELETE /api/cards/{id}`
#### Éxitos
- `204 No Content`

#### Errores
- `404 Not Found` `CARD_NOT_FOUND`
- `409 Conflict` `CARD_ALREADY_CLOSED`
- `409 Conflict` `OUTSTANDING_BALANCE`

---

## 9. Contratos DTO Recomendados

### 9.1 Requests
- `CreateCardRequest`
- `CreateConsumptionRequest`
- `CreatePaymentRequest`
- `BlockCardRequest`
- `RefundConsumptionRequest`

### 9.2 Responses
- `CardResponse`
- `ConsumptionResponse`
- `PaymentResponse`
- `LimitResponse`
- `StatementResponse`
- `ApiError`

### 9.3 ApiError mínimo
- `timestamp`
- `status`
- `error`
- `code`
- `message`
- `path`
- `details`

---

## 10. Eventos Kafka

### 10.1 Publicados por cards-service

| Topic | Key | Payload sugerido | Trigger |
|---|---|---|---|
| `yanki.card.opened.v1` | `cardId` | `CardOpenedEvent` | Emisión exitosa |
| `yanki.card.closed.v1` | `cardId` | `CardClosedEvent` | Cierre exitoso |
| `yanki.card.consumption.registered.v1` | `cardId` | `CardConsumptionRegisteredEvent` | Consumo crédito autorizado |
| `yanki.debit.payment.requested.v1` | `paymentId` | `DebitPaymentRequestedEvent` | Consumo débito / top-up / pago |
| `yanki.wallet.topup.settled.v1` | `requestId` | `WalletTopupSettledEvent` | Resultado top-up |

### 10.2 Consumidos por cards-service

| Topic | Payload sugerido | Acción |
|---|---|---|
| `yanki.customer.registered.v1` | `CustomerRegisteredEvent` | Crear vistas locales |
| `yanki.customer.updated.v1` | `CustomerUpdatedEvent` | Actualizar vistas locales |
| `yanki.customer.deleted.v1` | `CustomerDeletedEvent` | Marcar vistas inactivas |
| `yanki.account.opened.v1` | `AccountOpenedEvent` | Registrar/validar cuentas para débito |
| `yanki.debit.payment.settled.v1` | `DebitPaymentSettledEvent` | Confirmar/rechazar operaciones asíncronas |
| `yanki.credit.overdue.v1` | `CreditOverdueEvent` | Actualizar riesgo |
| `yanki.wallet.topup.requested.v1` | `WalletTopupRequestedEvent` | Iniciar top-up |

### 10.3 Payload fields mínimos sugeridos

#### CardOpenedEvent
- `cardId`
- `customerId`
- `type`
- `subtype`
- `creditLimit`
- `linkedAccountId`
- `occurredAt`

#### DebitPaymentRequestedEvent
- `paymentId`
- `cardId`
- `customerId`
- `accountId`
- `amount`
- `reason`
- `occurredAt`

#### DebitPaymentSettledEvent
- `paymentId`
- `status`
- `reason`
- `occurredAt`

#### WalletTopupRequestedEvent
- `requestId`
- `walletId`
- `debitCardId`
- `accountId`
- `amount`
- `occurredAt`

#### WalletTopupSettledEvent
- `requestId`
- `status`
- `creditedAmount`
- `reason`
- `occurredAt`

### 10.4 Idempotencia
Todo consumidor Kafka debe registrar una clave única del tipo:

`<topic>:<entityId>:<occurredAt>`

en `processed_events`.

---

## 11. Persistencia e Índices Recomendados

### Colección `cards`
Índices sugeridos:
- `id`
- `customerId`
- `type`
- `status`
- `linkedAccountId` único parcial para débito por cuenta
- `cardNumber` único

### Colección `consumptions`
Índices sugeridos:
- `cardId`
- `customerId`
- `status`
- `occurredAt`
- `paymentId`

### Colecciones de vistas
- `customer_profile_view.customerId` único
- `customer_risk_view.customerId` único

### `processed_events`
- índice único por clave lógica de idempotencia

---

## 12. Redis

Clave principal:
- `yanki:cards:limit:{cardId}`

Contenido sugerido:
```json
{
  "creditLimit": 5000,
  "availableLimit": 4850,
  "consumed": 150
}
```

TTL:
- `15m`

Invalidación:
- cada consumo
- cada pago
- cambios de perfil
- facturación mensual
- cierre de tarjeta

---

## 13. Flujos Asíncronos Clave

### 13.1 Pago débito
Cards publica `yanki.debit.payment.requested.v1` y espera `yanki.debit.payment.settled.v1`.

### 13.2 Top-up wallet
Wallet publica `yanki.wallet.topup.requested.v1`, cards valida, solicita débito a accounts y luego responde a wallet con `yanki.wallet.topup.settled.v1`.

### 13.3 Bloqueo por deuda vencida
Credits publica `yanki.credit.overdue.v1`, cards actualiza `customer_risk_view` y bloquea nuevas emisiones de crédito.

---

## 14. Facturación Mensual / Statement

Derivado de `CU-07`, el servicio debe contemplar un proceso programado para:

1. consultar consumos `AUTHORIZED` del periodo anterior,
2. agrupar por tarjeta,
3. generar estado de cuenta,
4. marcar consumos como `SETTLED`,
5. invalidar límites en Redis.

Este proceso puede implementarse con:
- scheduler interno,
- job reactivo,
- o endpoint administrativo protegido si la arquitectura lo permite.

Aunque la colección `statements` no está formalmente listada en el esqueleto inicial, **el agente implementador debe evaluar su creación** si el diseño final la requiere.

---

## 15. Estructura de Paquetes Actual

```text
src/main/java/com/yanki/cards
├── CardsServiceApplication.java
├── config/
│   └── OpenAPIConfig.java
├── domain
│   ├── model/              # 8 entidades/enum puras (Card, Consumption, CustomerProfileView, CustomerRiskView, ProcessedEvent, CardType, CardSubtype, CardStatus, ConsumptionStatus, CustomerProfile, CustomerType)
│   ├── port/
│   │   ├── in/             # 15 puertos de entrada (UseCase interfaces)
│   │   └── out/            # 6 puertos de salida (persistence, cache, event publisher)
│   ├── service/            # 14 implementaciones de use case con RxJava 3
│   ├── event/              # 11 domain events + Topics.java
│   └── exception/          # BusinessException, ErrorCode (27 códigos)
├── infrastructure
│   ├── config/             # CacheProperties, KafkaConsumerConfig
│   ├── persistence/
│   │   ├── document/       # 5 @Document classes
│   │   ├── repository/     # 5 SpringData*Repository interfaces
│   │   ├── mapper/         # 5 Document ↔ Domain mappers
│   │   └── adapter/        # 5 adaptadores de puertos de persistencia
│   ├── cache/adapter/      # CardRedisCacheAdapter
│   └── messaging/
│       ├── adapter/        # CardKafkaEventAdapter (7 tipos de evento)
│       └── consumer/       # CustomerProfileConsumer, AccountClosedConsumer
└── interfaces/rest/
    ├── controller/         # CardController (13 endpoints)
    ├── dto/                # 13 DTOs (requests y responses)
    ├── mapper/             # CardRestMapper, ConsumptionRestMapper
    └── GlobalExceptionHandler.java
```

### Componentes implementados
- `CardController` — 13 endpoints REST reactivos
- 14 servicios de dominio — IssueCard, FindCards, GetCard, GetCardLimit, GetConsumptions, GetConsumptionDetail, RegisterConsumption, RefundConsumption, GetStatement, RegisterPayment, BlockCard, UnblockCard, CloseCard, UpdateCustomerProfile, HandleAccountClosed
- `CardKafkaEventAdapter` — Publicador de 7 tipos de eventos Kafka
- `CustomerProfileConsumer` — Consumidor de CustomerRegistered/Updated/Deleted
- `AccountClosedConsumer` — Consumidor de AccountClosed
- `CardRedisCacheAdapter` — Cache de tarjeta y límite con TTL configurable
- `GlobalExceptionHandler` — Manejo unificado de errores
- `ErrorCode` — 27 códigos de error
- `BusinessException` — Excepción de negocio

---

## 16. Estrategia de Implementación Autónoma

Orden recomendado para que un agente implemente el microservicio:

1. Crear modelo de dominio y enums.
2. Crear repositorios Mongo reactivos.
3. Crear DTOs request/response.
4. Implementar manejo de errores unificado.
5. Implementar emisión de tarjetas (`POST /api/cards`).
6. Implementar consulta/listado de tarjetas.
7. Implementar límites Redis.
8. Implementar consumos de crédito.
9. Implementar consumos débito con Kafka.
10. Implementar pago de tarjeta crédito.
11. Implementar bloqueo, desbloqueo y cierre.
12. Implementar consumidores Kafka para vistas locales y settled events.
13. Implementar top-up wallet.
14. Implementar statement y refund.
15. Agregar tests unitarios, integración y eventos.
16. Validar Checkstyle, JaCoCo y OpenAPI.

---

## 17. Estrategia de Testing

### Unit tests
- reglas de emisión crédito/débito
- validación de límite
- cambio de estados
- bloqueo por deuda vencida
- refund elegible/no elegible

### Integration tests
- controladores reactivos
- Mongo repositories
- serialización de DTOs
- GlobalExceptionHandler

### Event tests
- consumers Kafka idempotentes
- publicación correcta de eventos
- flujo `debit.payment.requested/settled`
- flujo `wallet.topup.requested/settled`

### Cobertura mínima esperada
- happy path
- casos exitosos alternativos
- errores esperados
- validación de códigos HTTP
- validación de efectos laterales en Mongo/Redis/Kafka

---

## 18. Variables de Entorno

| Variable | Default | Descripción |
|---|---|---|
| `SERVER_PORT` | `8085` | Puerto del servicio |
| `MONGODB_URI` | `mongodb://localhost:27017/yanki_cards` | MongoDB |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka brokers |
| `KAFKA_GROUP_ID` | `cards-service` | Consumer group |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Eureka |
| `CONFIG_SERVER_URL` | `http://localhost:8888` | Config server |
| `YANKI_CARD_STANDARD_LIMIT` | `5000` | Límite base |
| `YANKI_CARD_VIP_LIMIT_MULTIPLIER` | `2.0` | Multiplicador VIP |
| `YANKI_CARD_PYME_LIMIT_MULTIPLIER` | `3.0` | Multiplicador PYME |

---

## 19. Ejemplos de Uso

```bash
curl -X POST http://localhost:8085/api/cards \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CLI-1001",
    "type": "CREDIT",
    "subtype": "PERSONAL",
    "creditLimit": 5000
  }'
```

```bash
curl -X POST http://localhost:8085/api/cards/CARD-1001/consumptions \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 150,
    "merchantId": "MERCH-01",
    "description": "Compra supermercado"
  }'
```

```bash
curl -X PUT http://localhost:8085/api/cards/CARD-1001/block \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "STOLEN"
  }'
```

```bash
curl http://localhost:8085/api/cards/CARD-1001/limit
```

---

## 20. Ejecución

### Desarrollo
```powershell
mvn spring-boot:run
```

### Build y validación
```powershell
mvn clean verify
```

### Docker
```bash
docker build -t yanki/cards-service:1.0.0 .
docker run -p 8085:8085 yanki/cards-service:1.0.0
```

### Salud
```bash
curl http://localhost:8085/actuator/health
```

### OpenAPI / Swagger
- `http://localhost:8085/swagger-ui.html` — Interfaz interactiva de prueba de endpoints
- `http://localhost:8085/v3/api-docs` — Especificación OpenAPI 3.0 en JSON
- [`API/cards-service.openapi.yml`](API/cards-service.openapi.yml) — Especificación OpenAPI 3.2.1 completa en YAML con los 13 endpoints, 10 schemas, ejemplos de respuestas y errores

### Colección Postman
- [`postman/Yanki-Cards-Service-API.postman_collection.json`](postman/Yanki-Cards-Service-API.postman_collection.json) — Colección completa con 13 endpoints, tests automatizados, variables y respuestas de ejemplo
- [`postman/README.md`](postman/README.md) — Documentación detallada de la colección, flujo de uso y tabla de errores

**Documentación implementada:**
- Configuración OpenAPI con info del API, contacto y servidores (dev, staging, prod)
- 13 endpoints documentados con `@Operation`, `@Parameter` y `@ApiResponses`
- 10 DTOs documentados con `@Schema` (descripciones, ejemplos, valores permitidos)
- Tags organizados: Cards, Consumptions, Payments

---

## 21. Dependencias Externas y Mocks Requeridos para Pruebas

| Dependencia | Tipo | Motivo de mock |
|---|---|---|
| MongoDB | Persistencia | probar reglas sin infraestructura real si es unit test |
| Redis | Cache | validar cálculo/invalación |
| Kafka | Eventos | verificar publicación y consumo |
| customers-service | vista local/eventos | estado y perfil del cliente |
| credits-service | vista local/eventos | deuda vencida |
| accounts-service | liquidación débito | consumos débito y pagos |
| wallet-service | top-up | coordinación asincrónica |

Referirse también a:
- `cards-service/CASOS_PRUEBA/MOCKS.md`

---

## 22. Trazabilidad y Documentación

> **Regla de Oro de Trazabilidad Bidireccional:** Ver `../AGENTS.md`

### Casos de Uso Asociados

| CU | Nombre | Rol | SEC Vinculados |
|----|--------|-----|----------------|
| **CU-04** | Gestión de Tarjetas | Owner | SEC014, SEC015, SEC016, SEC017, SEC018, SEC019, SEC020 |
| **CU-07** | Consumos en Tarjetas de Crédito | Owner | SEC021, SEC022, SEC023, SEC024, SEC025, SEC026 |

### Diagramas de Secuencia

| SEC | Descripción |
|-----|-------------|
| SEC014 | Emisión tarjeta de crédito personal |
| SEC015 | Emisión tarjeta de crédito empresarial |
| SEC016 | Emisión tarjeta de débito |
| SEC017 | Bloqueo de tarjeta |
| SEC018 | Desbloqueo de tarjeta |
| SEC019 | Cierre de tarjeta |
| SEC020 | Consulta límite disponible |
| SEC021 | Consumo con tarjeta de crédito |
| SEC022 | Consumo con tarjeta de débito |
| SEC023 | Pago de tarjeta de crédito |
| SEC024 | Reverso/devolución de consumo |
| SEC025 | Top-up wallet con tarjeta débito |
| SEC026 | Bloqueo por deuda vencida |

### Casos de Prueba QA

| CP | Descripción | CU | SEC |
|----|-------------|----|-----|
| CP-024 | Emisión de tarjeta de crédito válida | CU-04 | SEC014 |
| CP-025 | Emisión de tarjeta de débito asociada a cuenta | CU-04 | SEC016 |
| CP-026 | Consumo con tarjeta de crédito dentro del límite | CU-07 | SEC021 |
| CP-027 | Rechazo de consumo por exceder límite | CU-07 | SEC021 |
| CP-028 | Pago con tarjeta de débito exitoso | CU-04 | SEC022 |
| CP-029 | Pago con tarjeta de débito rechazado por deuda vencida | CU-04 | SEC026 |

### Casos de Prueba Locales

| Archivo | Descripción |
|---------|-------------|
| `CASOS_PRUEBA/CP-001-emision-tarjeta-credito-personal-exitosa.md` | Happy path emisión crédito personal |
| `CASOS_PRUEBA/CP-002-emision-tarjeta-debito-exitosa.md` | Happy path emisión débito |
| `CASOS_PRUEBA/CP-003-emision-tarjeta-credito-rechazada-por-deuda-vencida.md` | Error: deuda vencida bloquea emisión |

### Referencias Cruzadas

| Archivo | Descripción |
|---------|-------------|
| `../docs/CASOS_USO` | Caso de uso principal: gestión de tarjetas |
| `../docs/CASOS_USO` | Caso de uso: consumos en tarjetas |
| `../DIAGRAMAS_SECUENCIAS/SEC014_emision_tarjeta_credito_personal.md` | Diagrama emisión crédito personal |
| `../DIAGRAMAS_SECUENCIAS/SEC016_emision_tarjeta_debito.md` | Diagrama emisión débito |
| `../DIAGRAMAS_SECUENCIAS/SEC021_consumo_tarjeta_credito.md` | Diagrama consumo crédito |
| `../DIAGRAMAS_SECUENCIAS/SEC025_topup_wallet_tripartito.md` | Diagrama top-up wallet |
| `../docs/CASOS_PRUEBA` | Matriz de trazabilidad completa |
| `../docs/CASOS_PRUEBA` | Trazabilidad por microservicio |
| `../system-prompt-auditoria-hexagonal-yanki.md` | Estándar de arquitectura hexagonal |

---

## 23. Referencias

- `README.md` raíz del proyecto
- `CONVENTIONS.md`
- `docs/EVENTOS.md`
- `../docs/CASOS_USO`
- `../docs/CASOS_USO`
- `CASOS_PRUEBA/CASOS_PRUEBA_QA.md`
- `cards-service/CASOS_PRUEBA/README.md`
- `cards-service/CASOS_PRUEBA/MOCKS.md`
- `cards-service/CASOS_PRUEBA/CP-001-emision-tarjeta-credito-personal-exitosa.md`
- `cards-service/CASOS_PRUEBA/CP-002-emision-tarjeta-debito-exitosa.md`
- `cards-service/CASOS_PRUEBA/CP-003-emision-tarjeta-credito-rechazada-por-deuda-vencida.md`
- `api/cards-service.yaml` — Contrato OpenAPI 3.0 completo con schemas, endpoints y errores
