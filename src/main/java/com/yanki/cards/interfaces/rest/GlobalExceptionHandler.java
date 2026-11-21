package com.yanki.cards.interfaces.rest;

import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.interfaces.rest.dto.ApiError;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para los controladores REST.
 * <p>
 * Convierte las excepciones de negocio y técnicas en respuestas HTTP
 * con el formato {@link ApiError} y el código de estado adecuado.
 * </p>
 * 
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
@Slf4j
@Component
@Order(-1)
public class GlobalExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        if (ex instanceof BusinessException businessEx) {
            log.warn("Business exception en {} {}: [{}] {} - cliente: {}",
                    method, path, businessEx.getErrorCode().name(), businessEx.getMessage(),
                    extractCustomerId(exchange));
            return buildErrorResponse(response, path, businessEx.getErrorCode().getHttpStatus(),
                HttpStatus.valueOf(businessEx.getErrorCode().getHttpStatus()).getReasonPhrase(),
                businessEx.getErrorCode().name(), businessEx.getMessage());
        } else if (ex instanceof IllegalArgumentException illegalArgEx) {
            log.warn("Illegal argument exception en {} {}: {}",
                    method, path, illegalArgEx.getMessage());
            return buildErrorResponse(response, path, 400, "Bad Request",
                "INVALID_ARGUMENT", illegalArgEx.getMessage());
        } else if (ex instanceof WebExchangeBindException bindEx) {
            log.warn("Validation failed en {} {}: {}",
                    method, path, bindEx.getMessage());
            List<String> details = bindEx.getAllErrors().stream()
                .map(error -> error instanceof FieldError fieldError
                    ? fieldError.getField() + ": " + error.getDefaultMessage()
                    : error.getDefaultMessage())
                .collect(Collectors.toList());
            return buildErrorResponse(response, path, 400, "Bad Request",
                "VALIDATION_ERROR", "Error de validación en la solicitud", details);
        } else if (ex instanceof IllegalStateException illegalStateEx) {
            log.warn("Illegal state exception en {} {}: {}",
                    method, path, illegalStateEx.getMessage());
            return buildErrorResponse(response, path, 409, "Conflict",
                "INVALID_STATE", illegalStateEx.getMessage());
        } else {
            log.error("Unexpected exception en {} {}: {} {}",
                    method, path, ex.getClass().getSimpleName(), ex.getMessage(), ex);
            return buildErrorResponse(response, path, 500, "Internal Server Error",
                "INTERNAL_ERROR", "Ha ocurrido un error interno del servidor");
        }
    }

    private String extractCustomerId(ServerWebExchange exchange) {
        try {
            String path = exchange.getRequest().getURI().getPath();
            String[] parts = path.split("/");
            if (parts.length >= 4) {
                return parts[3];
            }
        } catch (Exception e) {
            log.debug("No se pudo extraer customerId de la request: {}", e.getMessage());
        }
        return "desconocido";
    }

    private Mono<Void> buildErrorResponse(ServerHttpResponse response, String path, int status,
                                          String error, String code, String message) {
        return buildErrorResponse(response, path, status, error, code, message, null);
    }

    private Mono<Void> buildErrorResponse(ServerHttpResponse response, String path, int status,
                                          String error, String code, String message, List<String> details) {
        if (response.isCommitted()) {
            return Mono.error(new RuntimeException("Response already committed"));
        }

        response.setStatusCode(HttpStatus.valueOf(status));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiError apiError = new ApiError(status, error, code, message, path, details);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(apiError);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Failed to write error response", e);
            return response.setComplete();
        }
    }
}
