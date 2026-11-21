package com.yanki.cards.domain.exception;

/**
 * Excepción de negocio lanzada cuando se viola una regla de negocio.
 * <p>
 * Cada instancia está asociada a un {@link ErrorCode} que determina
 * el código HTTP y el mensaje de la respuesta de error.
 * </p>
 * 
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
public class BusinessException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public BusinessException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage() + ": " + detail);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
