# CP-003 - Rechazo de emisión de tarjeta de crédito por deuda vencida

## Objetivo
Validar que el microservicio `cards-service` rechaza la emisión de una tarjeta de crédito cuando el cliente presenta deuda vencida en la vista local de riesgo.

## Tipo de caso
- Error esperado

## Precondiciones
- Existe un cliente con `customerId` válido.
- La vista local `customer_profile_view` contiene al cliente con estado `ACTIVE`.
- La vista local `customer_risk_view` indica `hasOverdueDebt = true`.
- MongoDB y Kafka están simulados mediante mocks para verificar que no se ejecuten operaciones no permitidas.

## Datos de entrada
### Request
`POST /api/cards`

```json
{
  "customerId": "CLI-2001",
  "type": "CREDIT",
  "subtype": "PERSONAL",
  "creditLimit": 3000
}
```

## Mocks requeridos
- Mock de `customer_profile_view` con cliente activo.
- Mock de `customer_risk_view` con deuda vencida.
- Mock de MongoDB para verificar que no se persiste ninguna tarjeta.
- Mock de Kafka para verificar que no se publica `yanki.card.opened.v1`.

## Pasos
1. Enviar solicitud de emisión de tarjeta de crédito.
2. Validar que el servicio consulta la vista de perfil local.
3. Validar que el servicio consulta la vista de riesgo local.
4. Detectar que el cliente tiene deuda vencida.
5. Rechazar la operación antes de persistir o publicar eventos.

## Resultado esperado
- Respuesta HTTP `403 Forbidden`.
- Mensaje de error indicando que el cliente no es elegible por deuda vencida.
- No se crea ninguna tarjeta en MongoDB.
- No se genera caché en Redis.
- No se publica el evento `yanki.card.opened.v1`.

## Validaciones adicionales
- Verificar que no se invoquen operaciones de escritura en persistencia.
- Verificar que no existan efectos secundarios en sistemas externos mockeados.
