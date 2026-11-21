package com.yanki.cards.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/**
 * DTO de respuesta de error unificado para el servicio de tarjetas.
 * <p>
 * Sigue el contrato de error especificado con los campos obligatorios:
 * timestamp, status, error, code, message, path y details.
 * </p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "ApiError",
        description = "Respuesta de error estandarizada del API con código de negocio, mensaje y detalles",
        example = "{\"timestamp\":\"2026-11-14T18:00:00Z\",\"status\":404,\"error\":\"Not Found\",\"code\":\"CARD_NOT_FOUND\",\"message\":\"La tarjeta no existe\",\"path\":\"/api/cards/abc123\"}"
)
public class ApiError {

    @Schema(
            description = "Marca de tiempo del error en formato ISO-8601",
            example = "2026-11-14T18:00:00Z",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final Instant timestamp;

    @Schema(
            description = "Código HTTP de la respuesta de error",
            example = "404",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final int status;

    @Schema(
            description = "Razón breve del error en inglés (estándar HTTP)",
            example = "Not Found",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String error;

    @Schema(
            description = "Código de error de negocio específico del dominio de tarjetas",
            example = "CARD_NOT_FOUND",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String code;

    @Schema(
            description = "Mensaje descriptivo del error en español para el usuario final",
            example = "La tarjeta no existe",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String message;

    @Schema(
            description = "Ruta HTTP donde se produjo el error",
            example = "/api/cards/abc123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String path;

    @Schema(
            description = "Lista opcional de detalles adicionales sobre el error (validaciones, restricciones)",
            example = "[\"El monto debe ser mayor a cero\"]"
    )
    private final List<String> details;
    
    public ApiError(int status, String error, String code, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.path = path;
        this.details = null;
    }
    
    public ApiError(int status, String error, String code, String message, String path, List<String> details) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.path = path;
        this.details = details;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public int getStatus() {
        return status;
    }
    
    public String getError() {
        return error;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getPath() {
        return path;
    }
    
    public List<String> getDetails() {
        return details;
    }
}
