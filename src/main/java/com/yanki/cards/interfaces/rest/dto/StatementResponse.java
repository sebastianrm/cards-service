package com.yanki.cards.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para la respuesta del estado de cuenta mensual.
 */
@Schema(
        name = "StatementResponse",
        description = "Estado de cuenta mensual con el resumen de consumos y saldos de una tarjeta de crédito",
        example = "{\"cardId\":\"card-123\",\"month\":\"2026-11-01\",\"consumptions\":[],\"totalAmount\":1500.00,\"availableLimit\":8500.00}"
)
public record StatementResponse(
        @Schema(description = "Identificador de la tarjeta", example = "card-123", requiredMode = Schema.RequiredMode.REQUIRED)
        String cardId,

        @Schema(description = "Mes del estado de cuenta en formato YYYY-MM (ej: 2026-11)", example = "2026-11-01", nullable = true)
        LocalDate month,

        @Schema(description = "Lista de consumos registrados en el periodo del estado de cuenta")
        List<ConsumptionResponse> consumptions,

        @Schema(description = "Monto total de consumos en el periodo", example = "1500.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal totalAmount,

        @Schema(description = "Límite disponible actual de la tarjeta al cierre del periodo", example = "8500.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal availableLimit
) {}
