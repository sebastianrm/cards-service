package com.yanki.cards.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para las operaciones de consulta de tarjetas.
 * <p>
 * Expone los datos relevantes de la tarjeta al cliente REST
 * sin exponer detalles internos del dominio.
 * </p>
 */
@Schema(
        name = "CardResponse",
        description = "Datos completos de una tarjeta de crédito o débito",
        example = "{\"id\":\"card-123\",\"customerId\":\"cust-456\",\"type\":\"CREDIT\",\"subtype\":\"PERSONAL\",\"status\":\"ACTIVE\",\"cardNumber\":\"****1234\",\"expiryDate\":\"2028-12-31T23:59:59\",\"creditLimit\":10000.00,\"availableLimit\":8500.00,\"linkedAccountId\":\"acc-789\",\"issuedAt\":\"2026-01-15T10:00:00\",\"activatedAt\":\"2026-01-16T08:00:00\",\"createdAt\":\"2026-01-15T10:00:00\"}"
)
public record CardResponse(
        @Schema(description = "Identificador único de la tarjeta", example = "card-123", requiredMode = Schema.RequiredMode.REQUIRED)
        String id,

        @Schema(description = "Identificador del cliente propietario de la tarjeta", example = "cust-456", requiredMode = Schema.RequiredMode.REQUIRED)
        String customerId,

        @Schema(description = "Tipo de tarjeta: CREDIT (crédito) o DEBIT (débito)", example = "CREDIT", allowableValues = {"CREDIT", "DEBIT"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String type,

        @Schema(description = "Subtipo de tarjeta: PERSONAL, CORPORATE, VIRTUAL, FISICA", example = "PERSONAL", allowableValues = {"PERSONAL", "CORPORATE", "VIRTUAL", "FISICA"})
        String subtype,

        @Schema(description = "Estado actual de la tarjeta: ACTIVE, BLOCKED, CLOSED, EXPIRED", example = "ACTIVE", allowableValues = {"ACTIVE", "BLOCKED", "CLOSED", "EXPIRED"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String status,

        @Schema(description = "Número de tarjeta enmascarado (solo últimos 4 dígitos visibles)", example = "****1234")
        String cardNumber,

        @Schema(description = "Fecha de vencimiento de la tarjeta en formato ISO-8601", example = "2028-12-31T23:59:59")
        LocalDateTime expiryDate,

        @Schema(description = "Límite de crédito total asignado a la tarjeta (solo para tarjetas de crédito)", example = "10000.00", nullable = true)
        BigDecimal creditLimit,

        @Schema(description = "Límite disponible actual (crédito total menos consumos autorizados)", example = "8500.00")
        BigDecimal availableLimit,

        @Schema(description = "Identificador de la cuenta bancaria vinculada (obligatorio para tarjetas de débito)", example = "acc-789", nullable = true)
        String linkedAccountId,

        @Schema(description = "Fecha de emisión de la tarjeta en formato ISO-8601", example = "2026-01-15T10:00:00")
        LocalDateTime issuedAt,

        @Schema(description = "Fecha de activación de la tarjeta en formato ISO-8601", example = "2026-01-16T08:00:00", nullable = true)
        LocalDateTime activatedAt,

        @Schema(description = "Fecha de creación del registro en formato ISO-8601", example = "2026-01-15T10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime createdAt
) {
}
