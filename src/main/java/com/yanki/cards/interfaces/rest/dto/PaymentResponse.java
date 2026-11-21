package com.yanki.cards.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de pago registrado.
 */
@Schema(
        name = "PaymentResponse",
        description = "Respuesta confirmatoria de un pago registrado en una tarjeta de crédito",
        example = "{\"cardId\":\"card-123\",\"amount\":500.00,\"newAvailableLimit\":9000.00,\"paidAt\":\"2026-11-14T15:30:00\"}"
)
public record PaymentResponse(
        @Schema(description = "Identificador de la tarjeta donde se aplicó el pago", example = "card-123", requiredMode = Schema.RequiredMode.REQUIRED)
        String cardId,

        @Schema(description = "Monto del pago aplicado", example = "500.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal amount,

        @Schema(description = "Nuevo límite disponible después de aplicar el pago", example = "9000.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal newAvailableLimit,

        @Schema(description = "Fecha y hora del pago en formato ISO-8601", example = "2026-11-14T15:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime paidAt
) {}
