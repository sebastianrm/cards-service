package com.yanki.cards.domain.exception;

/**
 * Enumeración de códigos de error de negocio del servicio de tarjetas.
 * <p>
 * Cada código representa un escenario de error específico con su mensaje
 * asociado y el código HTTP correspondiente.
 * </p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
public enum ErrorCode {

    // Errores de cliente (4xx)
    CUSTOMER_NOT_FOUND("El cliente no existe en el sistema", 404),
    CUSTOMER_NOT_ACTIVE("El cliente no tiene estado activo", 409),
    CUSTOMER_HAS_OVERDUE_DEBT("El cliente tiene deuda vencida", 403),

    // Errores de cuenta (4xx)
    ACCOUNT_NOT_FOUND("La cuenta no existe", 404),
    ACCOUNT_CUSTOMER_MISMATCH("La cuenta no pertenece al cliente indicado", 422),
    ACCOUNT_REQUIRED_FOR_DEBIT("La tarjeta de débito requiere una cuenta vinculada", 400),

    // Errores de tarjeta (4xx)
    PERSONAL_CREDIT_CARD_ALREADY_EXISTS("Ya existe una tarjeta de crédito personal para este cliente", 409),
    DEBIT_CARD_ALREADY_EXISTS_FOR_ACCOUNT("Ya existe una tarjeta de débito para esta cuenta", 409),
    INVALID_CARD_TYPE("Tipo de tarjeta no válido", 422),
    INVALID_CREDIT_LIMIT("El límite de crédito debe ser mayor a cero", 400),
    CREDIT_LIMIT_REQUIRED("El límite de crédito es obligatorio para tarjetas de crédito", 400),
    CARD_NOT_FOUND("La tarjeta no existe", 404),
    CARD_NOT_ACTIVE("La tarjeta no está activa", 403),
    CARD_BLOCKED("La tarjeta está bloqueada", 403),
    CARD_EXPIRED("La tarjeta está vencida", 403),
    CARD_ALREADY_CLOSED("La tarjeta ya está cerrada", 409),
    CARD_ALREADY_BLOCKED("La tarjeta ya está bloqueada", 409),
    CARD_NOT_BLOCKED("La tarjeta no está bloqueada", 409),
    OUTSTANDING_BALANCE("La tarjeta tiene saldo pendiente", 409),
    LIMIT_NOT_APPLICABLE("La consulta de límite no aplica para este tipo de tarjeta", 422),

    // Errores de consumo (4xx)
    CONSUMPTION_NOT_FOUND("El consumo no existe", 404),
    CONSUMPTION_ALREADY_REFUNDED("El consumo ya fue reembolsado", 409),
    CONSUMPTION_NOT_REFUNDABLE("El consumo no es reembolsable en su estado actual", 409),
    INSUFFICIENT_CREDIT_LIMIT("Límite de crédito insuficiente para el consumo", 400),
    INVALID_AMOUNT("El monto debe ser mayor a cero", 400),
    INVALID_FILTERS("Los filtros de consulta no son válidos", 400),

    // Errores de pago (4xx)
    DEBIT_PAYMENT_REJECTED("El pago con débito fue rechazado", 400),

    // Errores de statement (4xx)
    STATEMENT_NOT_FOUND("No se encontró estado de cuenta para el periodo indicado", 404),

    // Errores internos (5xx)
    INTERNAL_ERROR("Error interno del servidor", 500);

    private final String message;
    private final int httpStatus;

    ErrorCode(String message, int httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
