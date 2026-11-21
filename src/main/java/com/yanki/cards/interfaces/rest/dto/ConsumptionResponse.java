package com.yanki.cards.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de un consumo.
 */
@Schema(
        name = "ConsumptionResponse",
        description = "Datos de un consumo registrado en una tarjeta",
        example = "{\"id\":\"cons-123\",\"cardId\":\"card-456\",\"customerId\":\"cust-789\",\"amount\":150.50,\"merchantId\":\"merchant-001\",\"description\":\"Compra en supermercado\",\"status\":\"AUTHORIZED\",\"paymentId\":\"pay-001\",\"occurredAt\":\"2026-11-14T12:00:00\",\"settledAt\":\"2026-11-15T00:00:00\",\"createdAt\":\"2026-11-14T12:00:00\",\"updatedAt\":\"2026-11-15T00:00:00\"}"
)
public record ConsumptionResponse(
        @Schema(description = "Identificador único del consumo", example = "cons-123", requiredMode = Schema.RequiredMode.REQUIRED)
        String id,

        @Schema(description = "Identificador de la tarjeta donde se registró el consumo", example = "card-456", requiredMode = Schema.RequiredMode.REQUIRED)
        String cardId,

        @Schema(description = "Identificador del cliente propietario de la tarjeta", example = "cust-789", requiredMode = Schema.RequiredMode.REQUIRED)
        String customerId,

        @Schema(description = "Monto del consumo en moneda local", example = "150.50", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal amount,

        @Schema(description = "Identificador del comercio donde se realizó el consumo", example = "merchant-001", requiredMode = Schema.RequiredMode.REQUIRED)
        String merchantId,

        @Schema(description = "Descripción del consumo o concepto de la transacción", example = "Compra en supermercado", nullable = true)
        String description,

        @Schema(
                description = "Estado del consumo: AUTHORIZED, SETTLED, REFUNDED, REJECTED",
                example = "AUTHORIZED",
                allowableValues = {"AUTHORIZED", "SETTLED", "REFUNDED", "REJECTED"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String status,

        @Schema(description = "Identificador del pago asociado (para consumos de débito con pago asíncrono)", example = "pay-001", nullable = true)
        String paymentId,

        @Schema(description = "Fecha y hora en que ocurrió el consumo en formato ISO-8601", example = "2026-11-14T12:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime occurredAt,

        @Schema(description = "Fecha y hora de liquidación del consumo en formato ISO-8601", example = "2026-11-15T00:00:00", nullable = true)
        LocalDateTime settledAt,

        @Schema(description = "Fecha de creación del registro en formato ISO-8601", example = "2026-11-14T12:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime createdAt,

        @Schema(description = "Fecha de última actualización en formato ISO-8601", example = "2026-11-15T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime updatedAt
) {}
