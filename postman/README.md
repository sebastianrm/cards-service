# Colección Postman — Yanki Cards Service API

## Descripción

Colección completa de la API REST del microservicio **cards-service** de la plataforma Yanki, versión Postman Collection v2.1.

Cubre los **13 endpoints** implementados siguiendo la Arquitectura Hexagonal, organizados en 4 carpetas:

| Carpeta | Endpoints | Descripción |
|---------|-----------|-------------|
| **1. Cards** | 9 | Emisión, consulta, bloqueo, desbloqueo y cierre de tarjetas |
| **2. Consumptions** | 5 | Registro, consulta y reembolso de consumos |
| **3. Payments** | 1 | Registro de pagos de tarjetas de crédito |
| **4. Error Responses Reference** | 1 | Referencia de errores y validación del schema `ApiError` |

## Archivo

- [`Yanki-Cards-Service-API.postman_collection.json`](Yanki-Cards-Service-API.postman_collection.json) — Colección importable en Postman.

## Cómo importar

1. Abrir Postman.
2. **Import** → Seleccionar el archivo `Yanki-Cards-Service-API.postman_collection.json`.
3. La colección se cargará con las variables pre-configuradas.

## Variables de Colección

| Variable | Valor por defecto | Descripción |
|----------|-------------------|-------------|
| `baseUrl` | `http://localhost:8083` | URL base del cards-service |
| `cardId` | `card-123` | ID de tarjeta de crédito (se actualiza al emitir) |
| `debitCardId` | `card-debit-001` | ID de tarjeta de débito (se actualiza al emitir) |
| `customerId` | `CLI-1001` | ID del cliente |
| `consumptionId` | `cons-456` | ID de consumo (se actualiza al registrar) |
| `accountId` | `ACC-5001` | ID de cuenta bancaria |
| `merchantId` | `merchant-001` | ID del comercio |

> **Nota:** Las variables `cardId`, `debitCardId` y `consumptionId` se actualizan automáticamente después de ejecutar los requests de emisión y registro, gracias a los tests `pm.collectionVariables.set()`.

## Flujo de uso recomendado

Siga el orden numerado de los requests para mantener la consistencia de los datos:

1. **1.2** — Emitir tarjeta de crédito (crea `cardId`)
2. **1.3** — Emitir tarjeta de débito (crea `debitCardId`)
3. **1.4** — Probar rechazo por deuda vencida (error esperado, CP-003)
4. **1.5** — Consultar detalle de la tarjeta creada
5. **1.6** — Consultar límite disponible
6. **2.1** — Registrar consumo (crea `consumptionId`)
7. **2.2** — Listar consumos
8. **2.3** — Consultar detalle del consumo
9. **2.4** — Reembolsar consumo
10. **2.5** — Estado de cuenta mensual
11. **3.1** — Registrar pago
12. **1.7** — Bloquear tarjeta
13. **1.8** — Desbloquear tarjeta
14. **1.9** — Cerrar tarjeta

## Tests automatizados

Cada request incluye tests automatizados (`pm.test`) que validan:

- **Código de estado HTTP** esperado (200, 201, 204, 400, 403, 404, 409, 422).
- **Schema de respuesta**: presencia y tipo de los campos del DTO correspondiente.
- **Códigos de error**: `code`, `message`, `status`, `error`, `path`, `timestamp` según `ApiError`.
- **Valores específicos**: `status` de la tarjeta (ACTIVE, BLOCKED), `type` (CREDIT, DEBIT), etc.

## Respuestas de ejemplo

Cada request incluye respuestas de ejemplo pre-definidas:

- **Happy Path**: Respuesta exitosa con el body JSON esperado.
- **Error esperado**: Respuestas de error con el schema `ApiError` validado contra `GlobalExceptionHandler.java` y `ErrorCode.java`.

### Schema ApiError

Todos los errores devuelven esta estructura:

```json
{
  "timestamp": "2026-11-14T18:00:00Z",
  "status": 404,
  "error": "Not Found",
  "code": "CARD_NOT_FOUND",
  "message": "La tarjeta no existe",
  "path": "/api/cards/card-nonexistent"
}
```

## Tabla completa de errores

| Código de error | HTTP Status | Mensaje |
|----------------|-------------|---------|
| `CUSTOMER_NOT_FOUND` | 404 | El cliente no existe en el sistema |
| `CUSTOMER_NOT_ACTIVE` | 409 | El cliente no tiene estado activo |
| `CUSTOMER_HAS_OVERDUE_DEBT` | 403 | El cliente tiene deuda vencida |
| `ACCOUNT_NOT_FOUND` | 404 | La cuenta no existe |
| `ACCOUNT_CUSTOMER_MISMATCH` | 422 | La cuenta no pertenece al cliente indicado |
| `ACCOUNT_REQUIRED_FOR_DEBIT` | 400 | La tarjeta de débito requiere una cuenta vinculada |
| `PERSONAL_CREDIT_CARD_ALREADY_EXISTS` | 409 | Ya existe una tarjeta de crédito personal para este cliente |
| `DEBIT_CARD_ALREADY_EXISTS_FOR_ACCOUNT` | 409 | Ya existe una tarjeta de débito para esta cuenta |
| `INVALID_CARD_TYPE` | 422 | Tipo de tarjeta no válido |
| `INVALID_CREDIT_LIMIT` | 400 | El límite de crédito debe ser mayor a cero |
| `CARD_NOT_FOUND` | 404 | La tarjeta no existe |
| `CARD_NOT_ACTIVE` | 403 | La tarjeta no está activa |
| `CARD_BLOCKED` | 403 | La tarjeta está bloqueada |
| `CARD_EXPIRED` | 403 | La tarjeta está vencida |
| `CARD_ALREADY_CLOSED` | 409 | La tarjeta ya está cerrada |
| `CARD_ALREADY_BLOCKED` | 409 | La tarjeta ya está bloqueada |
| `CARD_NOT_BLOCKED` | 409 | La tarjeta no está bloqueada |
| `OUTSTANDING_BALANCE` | 409 | La tarjeta tiene saldo pendiente |
| `LIMIT_NOT_APPLICABLE` | 422 | La consulta de límite no aplica para este tipo de tarjeta |
| `CONSUMPTION_NOT_FOUND` | 404 | El consumo no existe |
| `CONSUMPTION_ALREADY_REFUNDED` | 409 | El consumo ya fue reembolsado |
| `CONSUMPTION_NOT_REFUNDABLE` | 409 | El consumo no es reembolsable en su estado actual |
| `INSUFFICIENT_CREDIT_LIMIT` | 400 | Límite de crédito insuficiente para el consumo |
| `INVALID_AMOUNT` | 400 | El monto debe ser mayor a cero |
| `INVALID_FILTERS` | 400 | Los filtros de consulta no son válidos |
| `DEBIT_PAYMENT_REJECTED` | 400 | El pago con débito fue rechazado |
| `STATEMENT_NOT_FOUND` | 404 | No se encontró estado de cuenta para el periodo indicado |
| `INTERNAL_ERROR` | 500 | Error interno del servidor |

## Referencias a Casos de Prueba

| Request | Caso de Prueba | Descripción |
|---------|---------------|-------------|
| 1.2 Emitir tarjeta crédito | [CP-001](../CASOS_PRUEBA/CP-001-emision-tarjeta-credito-personal-exitosa.md) | Happy path: emisión exitosa |
| 1.3 Emitir tarjeta débito | [CP-002](../CASOS_PRUEBA/CP-002-emision-tarjeta-debito-exitosa.md) | Happy path: emisión exitosa |
| 1.4 Rechazo por deuda vencida | [CP-003](../CASOS_PRUEBA/CP-003-emision-tarjeta-credito-rechazada-por-deuda-vencida.md) | Error esperado: 403 Forbidden |

## Validación contra código fuente

Esta colección se generó validando exhaustivamente:

- **`CardController.java`**: Cada endpoint, path parameter, query parameter, body y response status code coincide con la implementación.
- **`GlobalExceptionHandler.java`**: Los errores esperados concuerdan con el mapeo `BusinessException` → HTTP status + `ErrorCode`.
- **`ErrorCode.java`**: Los 28 códigos de error están documentados con su mensaje y HTTP status exactos.
- **DTOs**: Los body de request y response coinciden con los records Java (`CreateCardRequest`, `CardResponse`, `CreateConsumptionRequest`, `ConsumptionResponse`, `BlockCardRequest`, `CreatePaymentRequest`, `PaymentResponse`, `LimitResponse`, `StatementResponse`, `ApiError`).

## Referencias

- **Controller:** [`CardController.java`](../src/main/java/com/yanki/cards/interfaces/rest/CardController.java)
- **Exception Handler:** [`GlobalExceptionHandler.java`](../src/main/java/com/yanki/cards/interfaces/rest/GlobalExceptionHandler.java)
- **Error Codes:** [`ErrorCode.java`](../src/main/java/com/yanki/cards/domain/exception/ErrorCode.java)
- **DTOs:** [`interfaces/rest/dto/`](../src/main/java/com/yanki/cards/interfaces/rest/dto/)
- **Casos de Prueba:** [`CASOS_PRUEBA/`](../CASOS_PRUEBA/)
