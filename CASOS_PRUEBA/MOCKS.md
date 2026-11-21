# Mocks para cards-service

Este archivo describe los mocks necesarios para simular la comunicación con sistemas externos durante las pruebas del microservicio de tarjetas (cards-service).

## Propósito de los mocks

- Evitar dependencia real con servicios externos durante las pruebas.
- Simular respuestas esperadas o errores en llamadas a APIs externas o sistemas terceros.
- Asegurar que las pruebas validan solo la lógica interna del microservicio.

## Ejemplos de mocks necesarios

- Mock para servicio de autenticación/autorización.
- Mock para microservicio de clientes, para validar datos del cliente.
- Mock para microservicio de cuentas, para verificar saldo y límites.
- Mock para sistema de notificaciones, para comprobar envío de alertas.

Cada mock debe cubrir tanto respuestas exitosas como casos de error (timeout, error 500, datos inválidos).

## Implementación propuesta

- Usar frameworks de mocking disponibles en el entorno de pruebas.
- Configurar mocks en los archivos de pruebas o en un módulo común para ser reutilizados.
- Documentar cada mock con los casos que cubre y las respuestas simuladas.
