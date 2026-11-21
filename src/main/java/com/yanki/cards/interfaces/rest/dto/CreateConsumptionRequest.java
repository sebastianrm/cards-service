package com.yanki.cards.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO para la solicitud de registro de un consumo.
 */
@Schema(
        name = "CreateConsumptionRequest",
        description = "Solicitud para registrar un nuevo consumo en una tarjeta",
        example = "{\"amount\":150.50,\"merchantId\":\"merchant-001\",\"description\":\"Compra en supermercado\"}"
)
public record CreateConsumptionRequest(
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        @Schema(description = "Monto del consumo (debe ser mayor a cero)", example = "150.50", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal amount,

        @NotNull(message = "El merchantId es obligatorio")
        @Schema(description = "Identificador del comercio donde se realiza el consumo", example = "merchant-001", requiredMode = Schema.RequiredMode.REQUIRED)
        String merchantId,

        @Schema(description = "Descripción opcional del consumo o concepto de la transacción", example = "Compra en supermercado", nullable = true)
        String description
) {}
