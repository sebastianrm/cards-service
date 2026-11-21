package com.yanki.cards.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO de solicitud para emitir una nueva tarjeta.
 * <p>
 * Contiene los datos minimos requeridos para crear una tarjeta
 * de credito o debito asociada a un cliente.
 * </p>
 */
@Schema(
        name = "CreateCardRequest",
        description = "Solicitud para emitir una nueva tarjeta de crédito o débito",
        example = "{\"customerId\":\"cust-456\",\"type\":\"CREDIT\",\"subtype\":\"PERSONAL\",\"creditLimit\":10000.00,\"linkedAccountId\":null}"
)
public record CreateCardRequest(
        @NotBlank(message = "El identificador del cliente es obligatorio")
        @Schema(description = "Identificador único del cliente propietario de la tarjeta", example = "cust-456", requiredMode = Schema.RequiredMode.REQUIRED)
        String customerId,

        @NotBlank(message = "El tipo de tarjeta es obligatorio")
        @Schema(
                description = "Tipo de tarjeta a emitir: CREDIT (crédito) o DEBIT (débito)",
                example = "CREDIT",
                allowableValues = {"CREDIT", "DEBIT"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String type,

        @Schema(
                description = "Subtipo de tarjeta: PERSONAL, CORPORATE, VIRTUAL, FISICA",
                example = "PERSONAL",
                allowableValues = {"PERSONAL", "CORPORATE", "VIRTUAL", "FISICA"},
                nullable = true
        )
        String subtype,

        @Schema(description = "Límite de crédito inicial para tarjetas de crédito (obligatorio para CREDIT, ignorado para DEBIT)", example = "10000.00", nullable = true)
        BigDecimal creditLimit,

        @JsonProperty("accountId")
        @Schema(description = "Identificador de la cuenta bancaria vinculada (obligatorio para tarjetas de débito)", example = "acc-789", nullable = true)
        String linkedAccountId
) {
}
