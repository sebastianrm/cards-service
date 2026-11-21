package com.yanki.cards.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO para la solicitud de pago de tarjeta de crédito.
 */
@Schema(
        name = "CreatePaymentRequest",
        description = "Solicitud para registrar un pago en una tarjeta de crédito",
        example = "{\"amount\":500.00,\"sourceAccountId\":\"acc-789\"}"
)
public record CreatePaymentRequest(
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        @Schema(description = "Monto del pago a aplicar a la tarjeta (debe ser mayor a cero)", example = "500.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal amount,

        @Schema(description = "Identificador de la cuenta bancaria origen del pago (opcional)", example = "acc-789", nullable = true)
        String sourceAccountId
) {}
