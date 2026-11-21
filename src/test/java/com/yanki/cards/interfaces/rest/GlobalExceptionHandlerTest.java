package com.yanki.cards.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.core.MethodParameter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas del manejador global de excepciones.
 * <p>
 * Verifica que cada tipo de excepcion se convierte en la respuesta HTTP
 * correcta con el codigo de estado y el formato ApiError esperado.
 * </p>
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(new ObjectMapper());
    }

    @Test
    @DisplayName("BusinessException - Retorna respuesta 404 con codigo de error")
    void shouldHandleBusinessException() {
        BusinessException ex = new BusinessException(ErrorCode.CARD_NOT_FOUND);
        ServerWebExchange exchange = MockServerWebExchange
                .builder(MockServerHttpRequest.get("/api/cards/CARD-001")).build();

        Mono<Void> result = handler.handle(exchange, ex);

        StepVerifier.create(result).expectComplete().verify();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(404);
        assertThat(exchange.getResponse().getHeaders().getContentType())
                .isEqualTo(org.springframework.http.MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("BusinessException - Retorna respuesta 403 para tarjeta bloqueada")
    void shouldHandleCardBlockedException() {
        BusinessException ex = new BusinessException(ErrorCode.CARD_BLOCKED);
        ServerWebExchange exchange = MockServerWebExchange
                .builder(MockServerHttpRequest.post("/api/cards/CARD-001/consumptions")).build();

        Mono<Void> result = handler.handle(exchange, ex);

        StepVerifier.create(result).expectComplete().verify();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(403);
    }

    @Test
    @DisplayName("BusinessException - Retorna respuesta 409 para conflicto")
    void shouldHandleConflictException() {
        BusinessException ex = new BusinessException(ErrorCode.PERSONAL_CREDIT_CARD_ALREADY_EXISTS);
        ServerWebExchange exchange = MockServerWebExchange
                .builder(MockServerHttpRequest.post("/api/cards")).build();

        Mono<Void> result = handler.handle(exchange, ex);

        StepVerifier.create(result).expectComplete().verify();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(409);
    }

    @Test
    @DisplayName("IllegalArgumentException - Retorna respuesta 400")
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Monto invalido");
        ServerWebExchange exchange = MockServerWebExchange
                .builder(MockServerHttpRequest.post("/api/cards/CARD-001/consumptions")).build();

        Mono<Void> result = handler.handle(exchange, ex);

        StepVerifier.create(result).expectComplete().verify();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("IllegalStateException - Retorna respuesta 409")
    void shouldHandleIllegalStateException() {
        IllegalStateException ex = new IllegalStateException("Estado invalido");
        ServerWebExchange exchange = MockServerWebExchange
                .builder(MockServerHttpRequest.put("/api/cards/CARD-001/block")).build();

        Mono<Void> result = handler.handle(exchange, ex);

        StepVerifier.create(result).expectComplete().verify();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(409);
    }

    @Test
    @DisplayName("Excepcion inesperada - Retorna respuesta 500")
    void shouldHandleUnexpectedException() {
        RuntimeException ex = new RuntimeException("Error inesperado");
        ServerWebExchange exchange = MockServerWebExchange
                .builder(MockServerHttpRequest.get("/api/cards")).build();

        Mono<Void> result = handler.handle(exchange, ex);

        StepVerifier.create(result).expectComplete().verify();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(500);
    }

    @Test
    @DisplayName("WebExchangeBindException - Retorna respuesta 400 con detalles de validacion")
    void shouldHandleWebExchangeBindException() throws NoSuchMethodException {
        MethodParameter parameter = new MethodParameter(
                com.yanki.cards.interfaces.rest.CardController.class
                        .getDeclaredMethod("issueCard", com.yanki.cards.interfaces.rest.dto.CreateCardRequest.class),
                0);
        org.springframework.validation.BeanPropertyBindingResult bindingResult =
                new org.springframework.validation.BeanPropertyBindingResult(
                        new Object(), "createCardRequest");
        bindingResult.addError(new FieldError(
                "createCardRequest", "customerId", "El identificador del cliente es obligatorio"));
        WebExchangeBindException ex = new WebExchangeBindException(parameter, bindingResult);

        ServerWebExchange exchange = MockServerWebExchange
                .builder(MockServerHttpRequest.post("/api/cards")).build();

        Mono<Void> result = handler.handle(exchange, ex);

        StepVerifier.create(result).expectComplete().verify();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(400);
        assertThat(exchange.getResponse().getHeaders().getContentType())
                .isEqualTo(org.springframework.http.MediaType.APPLICATION_JSON);
    }

}
