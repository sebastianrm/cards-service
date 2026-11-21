# CP-001 - Emisión exitosa de tarjeta de crédito personal

## Objetivo
Validar que el microservicio `cards-service` emite correctamente una tarjeta de crédito personal cuando el cliente está activo, no presenta deuda vencida y cumple las reglas de negocio.

## Tipo de caso
- Happy path
- Caso exitoso

## Precondiciones
- Existe un cliente con `customerId` válido.
- La vista local `customer_profile_view` contiene al cliente con estado `ACTIVE`.
- La vista local `customer_risk_view` indica `hasOverdueDebt = false`.
- El cliente no posee una tarjeta de crédito personal vigente.
- MongoDB, Redis y Kafka están disponibles o simulados mediante mocks.

## Datos de entrada
### Request
`POST /api/cards`

```json
{
  "customerId": "CLI-1001",
  "type": "CREDIT",
  "subtype": "PERSONAL",
  "creditLimit": 5000
}
```

## Mocks requeridos
- Mock de `customer_profile_view` con cliente activo.
- Mock de `customer_risk_view` sin deuda vencida.
- Mock de persistencia en MongoDB para guardar la tarjeta emitida.
- Mock de Redis para almacenar `yanki:cards:limit:{cardId}`.
- Mock de publicación Kafka para `yanki.card.opened.v1`.

## Pasos
1. Enviar solicitud de emisión de tarjeta de crédito personal.
2. Validar que el servicio consulta el perfil local del cliente.
3. Validar que el servicio consulta la vista de riesgo local.
4. Verificar que se aplican las reglas de negocio para tarjeta de crédito personal.
5. Verificar que se registra la tarjeta en MongoDB con estado `ISSUED`.
6. Verificar que se genera la caché del límite disponible en Redis.
7. Verificar que se publica el evento `yanki.card.opened.v1`.

## Resultado esperado
- Respuesta HTTP `201 Created`.
- Se retorna `cardId`, `cardNumber` enmascarado, `expiryDate` y `cvv`.
- La tarjeta queda registrada con:
  - `type = CREDIT`
  - `subtype = PERSONAL`
  - `status = ISSUED`
  - `availableLimit = creditLimit`
- Redis contiene la clave del límite disponible.
- Kafka recibe el evento de apertura de tarjeta.

## Errores no esperados
- No debe devolverse `409 Conflict`.
- No debe devolverse `403 Forbidden`.
- No debe fallar la persistencia ni la publicación del evento.
