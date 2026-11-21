package com.yanki.cards.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * DTO para la respuesta de consulta de límite disponible.
 */
@Schema(
        name = "LimitResponse",
        description = "Respuesta con el detalle del límite de crédito de una tarjeta",
        example = "{\"cardId\":\"card-123\",\"creditLimit\":10000.00,\"availableLimit\":8500.00,\"consumedAmount\":1500.00}"
)
public record LimitResponse(
        @Schema(description = "Identificador de la tarjeta", example = "card-123", requiredMode = Schema.RequiredMode.REQUIRED)
        String cardId,

        @Schema(description = "Límite de crédito total asignado a la tarjeta", example = "10000.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal creditLimit,

        @Schema(description = "Límite disponible actual (crédito total menos consumos autorizados)", example = "8500.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal availableLimit,

        @Schema(description = "Monto total consumos autorizados pendientes de pago", example = "1500.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal consumedAmount
) {}
