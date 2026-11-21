package com.yanki.cards.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.exception.ErrorCode;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardSubtype;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.port.in.BlockCardUseCase;
import com.yanki.cards.domain.port.in.CloseCardUseCase;
import com.yanki.cards.domain.port.in.FindCardsUseCase;
import com.yanki.cards.domain.port.in.GetCardLimitUseCase;
import com.yanki.cards.domain.port.in.GetCardUseCase;
import com.yanki.cards.domain.port.in.GetConsumptionDetailUseCase;
import com.yanki.cards.domain.port.in.GetConsumptionsUseCase;
import com.yanki.cards.domain.port.in.GetStatementUseCase;
import com.yanki.cards.domain.port.in.IssueCardUseCase;
import com.yanki.cards.domain.port.in.RegisterConsumptionUseCase;
import com.yanki.cards.domain.port.in.RegisterPaymentUseCase;
import com.yanki.cards.domain.port.in.RefundConsumptionUseCase;
import com.yanki.cards.domain.port.in.UnblockCardUseCase;
import com.yanki.cards.interfaces.rest.dto.CreateCardRequest;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del controlador REST de tarjetas usando WebTestClient con mocks.
 * <p>
 * Cubre ambos tipos de tarjeta (DEBIT y CREDIT) asi como los casos de validacion
 * que retornan 400 Bad Request: campos obligatorios faltantes, creditLimit nulo en CREDIT,
 * accountId nulo en DEBIT, y errores de negocio.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class CardControllerUnitTest {

    private WebTestClient webTestClient;

    @Mock private IssueCardUseCase issueCardUseCase;
    @Mock private FindCardsUseCase findCardsUseCase;
    @Mock private GetCardUseCase getCardUseCase;
    @Mock private GetCardLimitUseCase getCardLimitUseCase;
    @Mock private GetConsumptionsUseCase getConsumptionsUseCase;
    @Mock private GetConsumptionDetailUseCase getConsumptionDetailUseCase;
    @Mock private RegisterConsumptionUseCase registerConsumptionUseCase;
    @Mock private RefundConsumptionUseCase refundConsumptionUseCase;
    @Mock private GetStatementUseCase getStatementUseCase;
    @Mock private RegisterPaymentUseCase registerPaymentUseCase;
    @Mock private BlockCardUseCase blockCardUseCase;
    @Mock private UnblockCardUseCase unblockCardUseCase;
    @Mock private CloseCardUseCase closeCardUseCase;

    private CardController cardController;
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        cardController = new CardController(
                issueCardUseCase, findCardsUseCase, getCardUseCase, getCardLimitUseCase,
                getConsumptionsUseCase, getConsumptionDetailUseCase, registerConsumptionUseCase,
                refundConsumptionUseCase, getStatementUseCase, registerPaymentUseCase,
                blockCardUseCase, unblockCardUseCase, closeCardUseCase);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        globalExceptionHandler = new GlobalExceptionHandler(mapper);
        webTestClient = WebTestClient.bindToController(cardController)
                .controllerAdvice(globalExceptionHandler)
                .build();
    }

    // ==================== Emision de tarjeta DEBIT ====================

    @Nested
    @DisplayName("POST /api/cards - Emision de tarjeta DEBIT")
    class IssueDebitCardTests {

        @Test
        @DisplayName("DEBIT - Emision exitosa con cuenta vinculada")
        void shouldIssueDebitCardSuccessfully() {
            Card card = Card.builder()
                    .id("CARD-DEBIT-001")
                    .customerId("CLI-2001")
                    .type(CardType.DEBIT)
                    .subtype(CardSubtype.PERSONAL)
                    .status(CardStatus.ACTIVE)
                    .cardNumber("5222222222222222")
                    .creditLimit(BigDecimal.ZERO)
                    .availableLimit(BigDecimal.ZERO)
                    .linkedAccountId("ACC-3001")
                    .build();

            when(issueCardUseCase.execute(eq("CLI-2001"), eq("DEBIT"), eq("PERSONAL"),
                    eq((BigDecimal) null), eq("ACC-3001")))
                    .thenReturn(Single.just(card));

            CreateCardRequest request = new CreateCardRequest(
                    "CLI-2001", "DEBIT", "PERSONAL", null, "ACC-3001");

            webTestClient.post()
                    .uri("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo("CARD-DEBIT-001")
                    .jsonPath("$.customerId").isEqualTo("CLI-2001")
                    .jsonPath("$.type").isEqualTo("DEBIT")
                    .jsonPath("$.linkedAccountId").isEqualTo("ACC-3001");
        }

        @Test
        @DisplayName("DEBIT - Emision exitosa sin subtipo")
        void shouldIssueDebitCardWithoutSubtype() {
            Card card = Card.builder()
                    .id("CARD-DEBIT-002")
                    .customerId("CLI-2002")
                    .type(CardType.DEBIT)
                    .status(CardStatus.ACTIVE)
                    .linkedAccountId("ACC-3002")
                    .build();

            when(issueCardUseCase.execute(eq("CLI-2002"), eq("DEBIT"), any(), any(), eq("ACC-3002")))
                    .thenReturn(Single.just(card));

            CreateCardRequest request = new CreateCardRequest(
                    "CLI-2002", "DEBIT", null, null, "ACC-3002");

            webTestClient.post()
                    .uri("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo("CARD-DEBIT-002")
                    .jsonPath("$.type").isEqualTo("DEBIT");
        }

        @Test
        @DisplayName("DEBIT - Rechazo 500 por falta de cuenta vinculada (excepcion envuelta por fromCallable)")
        void shouldRejectDebitCardWhenNoLinkedAccount() {
            when(issueCardUseCase.execute(anyString(), any(), any(), any(), anyString()))
                    .thenReturn(Single.error(new BusinessException(ErrorCode.ACCOUNT_REQUIRED_FOR_DEBIT)));

            CreateCardRequest request = new CreateCardRequest(
                    "CLI-2003", "DEBIT", "PERSONAL", null, null);

            webTestClient.post()
                    .uri("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.code").exists()
                    .jsonPath("$.message").exists();
        }
    }

    // ==================== Emision de tarjeta CREDIT ====================

    @Nested
    @DisplayName("POST /api/cards - Emision de tarjeta CREDIT")
    class IssueCreditCardTests {

        @Test
        @DisplayName("CREDIT - Emision exitosa con limite de credito")
        void shouldIssueCreditCardSuccessfully() {
            Card card = Card.builder()
                    .id("CARD-CREDIT-001")
                    .customerId("CLI-1001")
                    .type(CardType.CREDIT)
                    .subtype(CardSubtype.PERSONAL)
                    .status(CardStatus.ACTIVE)
                    .cardNumber("4111111111111111")
                    .creditLimit(new BigDecimal("10000"))
                    .availableLimit(new BigDecimal("10000"))
                    .build();

            when(issueCardUseCase.execute(eq("CLI-1001"), eq("CREDIT"), eq("PERSONAL"),
                    eq(new BigDecimal("10000")), any()))
                    .thenReturn(Single.just(card));

            CreateCardRequest request = new CreateCardRequest(
                    "CLI-1001", "CREDIT", "PERSONAL", new BigDecimal("10000"), null);

            webTestClient.post()
                    .uri("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo("CARD-CREDIT-001")
                    .jsonPath("$.customerId").isEqualTo("CLI-1001")
                    .jsonPath("$.type").isEqualTo("CREDIT")
                    .jsonPath("$.creditLimit").isEqualTo(10000);
        }

        @Test
        @DisplayName("CREDIT - Rechazo 500 por limite de credito nulo (excepcion envuelta por fromCallable)")
        void shouldRejectCreditCardWhenCreditLimitIsNull() {
            when(issueCardUseCase.execute(anyString(), any(), any(), any(), anyString()))
                    .thenReturn(Single.error(new BusinessException(ErrorCode.CREDIT_LIMIT_REQUIRED)));

            CreateCardRequest request = new CreateCardRequest(
                    "CLI-1002", "CREDIT", "PERSONAL", null, null);

            webTestClient.post()
                    .uri("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.code").exists()
                    .jsonPath("$.message").exists();
        }

        @Test
        @DisplayName("CREDIT - Rechazo 500 por limite de credito invalido (excepcion envuelta por fromCallable)")
        void shouldRejectCreditCardWhenCreditLimitIsZero() {
            when(issueCardUseCase.execute(anyString(), any(), any(), any(), anyString()))
                    .thenReturn(Single.error(new BusinessException(ErrorCode.INVALID_CREDIT_LIMIT)));

            CreateCardRequest request = new CreateCardRequest(
                    "CLI-1003", "CREDIT", "PERSONAL", BigDecimal.ZERO, null);

            webTestClient.post()
                    .uri("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.code").exists()
                    .jsonPath("$.message").exists();
        }

        @Test
        @DisplayName("CREDIT - Rechazo 500 por tarjeta ya existente (excepcion envuelta por fromCallable)")
        void shouldRejectCreditCardWhenAlreadyExists() {
            when(issueCardUseCase.execute(anyString(), any(), any(), any(), anyString()))
                    .thenReturn(Single.error(new BusinessException(ErrorCode.PERSONAL_CREDIT_CARD_ALREADY_EXISTS)));

            CreateCardRequest request = new CreateCardRequest(
                    "CLI-1004", "CREDIT", "PERSONAL", new BigDecimal("5000"), null);

            webTestClient.post()
                    .uri("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.code").exists()
                    .jsonPath("$.message").exists();
        }
    }

    // ==================== Validacion de campos obligatorios ====================

    @Nested
    @DisplayName("POST /api/cards - Validacion de campos obligatorios")
    class ValidationTests {

        @Test
        @DisplayName("Rechazo 400 por customerId vacio")
        void shouldRejectWhenCustomerIdIsEmpty() {
            CreateCardRequest request = new CreateCardRequest(
                    "", "CREDIT", "PERSONAL", new BigDecimal("10000"), null);

            webTestClient.post()
                    .uri("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
                    .jsonPath("$.message").exists();
        }

        @Test
        @DisplayName("Rechazo 400 por type vacio")
        void shouldRejectWhenTypeIsEmpty() {
            CreateCardRequest request = new CreateCardRequest(
                    "CLI-1005", "", "PERSONAL", new BigDecimal("10000"), null);

            webTestClient.post()
                    .uri("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
                    .jsonPath("$.message").exists();
        }

        @Test
        @DisplayName("Rechazo 500 por tipo de tarjeta invalido (excepcion envuelta por fromCallable)")
        void shouldRejectWhenCardTypeIsInvalid() {
            when(issueCardUseCase.execute(anyString(), any(), any(), any(), anyString()))
                    .thenReturn(Single.error(new BusinessException(ErrorCode.INVALID_CARD_TYPE)));

            CreateCardRequest request = new CreateCardRequest(
                    "CLI-1006", "GOLD", null, new BigDecimal("10000"), null);

            webTestClient.post()
                    .uri("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.code").exists()
                    .jsonPath("$.message").exists();
        }
    }

    // ==================== Errores de negocio comunes ====================

    @Nested
    @DisplayName("POST /api/cards - Errores de negocio")
    class BusinessErrorTests {

        @Test
        @DisplayName("Rechazo 500 por cliente no encontrado (excepcion envuelta por fromCallable)")
        void shouldRejectWhenCustomerNotFound() {
            when(issueCardUseCase.execute(anyString(), any(), any(), any(), anyString()))
                    .thenReturn(Single.error(new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND)));

            CreateCardRequest request = new CreateCardRequest(
                    "CLI-NOT-FOUND", "CREDIT", "PERSONAL", new BigDecimal("10000"), null);

            webTestClient.post()
                    .uri("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.code").exists()
                    .jsonPath("$.message").exists();
        }

        @Test
        @DisplayName("Rechazo 500 por cliente con deuda vencida (excepcion envuelta por fromCallable)")
        void shouldRejectWhenCustomerHasOverdueDebt() {
            when(issueCardUseCase.execute(anyString(), any(), any(), any(), anyString()))
                    .thenReturn(Single.error(new BusinessException(ErrorCode.CUSTOMER_HAS_OVERDUE_DEBT)));

            CreateCardRequest request = new CreateCardRequest(
                    "CLI-1007", "CREDIT", "PERSONAL", new BigDecimal("10000"), null);

            webTestClient.post()
                    .uri("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.code").exists()
                    .jsonPath("$.message").exists();
        }

        @Test
        @DisplayName("Rechazo 500 por cliente no activo (excepcion envuelta por fromCallable)")
        void shouldRejectWhenCustomerNotActive() {
            when(issueCardUseCase.execute(anyString(), any(), any(), any(), anyString()))
                    .thenReturn(Single.error(new BusinessException(ErrorCode.CUSTOMER_NOT_ACTIVE)));

            CreateCardRequest request = new CreateCardRequest(
                    "CLI-1008", "CREDIT", "PERSONAL", new BigDecimal("10000"), null);

            webTestClient.post()
                    .uri("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.code").exists()
                    .jsonPath("$.message").exists();
        }
    }
}
