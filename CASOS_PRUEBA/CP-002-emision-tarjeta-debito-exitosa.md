# CP-002 - Emisión exitosa de tarjeta de débito

## Objetivo
Validar que el microservicio `cards-service` emite correctamente una tarjeta de débito cuando la cuenta del cliente está abierta y activa, y no posee una tarjeta de débito asociada a la misma.

## Tipo de caso
- Caso exitoso

## Precondiciones
- Existe una cuenta con `accountId` válida y estado `OPEN`.
- La cuenta pertenece al cliente con `customerId` correspondiente.
- La cuenta no tiene ya una tarjeta de débito vinculada.
- MongoDB y Kafka están disponibles o simulados mediante mocks.

## Datos de entrada
### Request
`POST /api/cards`

```json
{
  "customerId": "CLI-1001",
  "type": "DEBIT",
  "accountId": "ACC-5001"
}
```

## Mocks requeridos
- Mock de validación de cuenta en cards-service.
- Mock de persistencia en MongoDB para la tarjeta emitida.
- Mock de Kafka para publicación `yanki.card.opened.v1`.

## Pasos
1. Enviar solicitud de emisión de tarjeta de débito vinculada a cuenta.
2. Validar que la cuenta existe y está abierta.
3. Validar que no existe otra tarjeta de débito para la cuenta.
4. Registrar la tarjeta en MongoDB con estado `ACTIVE`.
5. Publicar evento `yanki.card.opened.v1`.

## Resultado esperado
- Respuesta HTTP `201 Created`.
- Respuesta con `cardId`, `cardNumber` enmascarado y `expiryDate`.
- La tarjeta queda registrada con:
  - `type = DEBIT`
  - `linkedAccountId = accountId`
  - `status = ACTIVE`
- Kafka recibe evento de emisión de tarjeta.

## Errores no esperados
- No debe devolverse `422` ni errores de validación.
- No debe fallar la creación ni publicación del evento.
