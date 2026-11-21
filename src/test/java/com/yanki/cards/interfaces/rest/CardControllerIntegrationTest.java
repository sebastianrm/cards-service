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
import com.yanki.cards.domain.model.Consumption;
import com.yanki.cards.domain.model.ConsumptionStatus;
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
import com.yanki.cards.interfaces.rest.dto.BlockCardRequest;
import com.yanki.cards.interfaces.rest.dto.CreateCardRequest;
import com.yanki.cards.interfaces.rest.dto.CreateConsumptionRequest;
import com.yanki.cards.interfaces.rest.dto.CreatePaymentRequest;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Pruebas de integracion del controlador REST de tarjetas usando WebTestClient con mocks.
 */
@ExtendWith(MockitoExtension.class)
class CardControllerIntegrationTest {

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

    // ==================== Tarjetas ====================

    @Test
    @DisplayName("POST /api/cards - Emision exitosa de tarjeta de credito - valida invocacion del puerto de entrada")
    void shouldIssueCreditCardSuccessfully() {
        Card card = Card.builder()
                .id("CARD-001")
                .customerId("CLI-1001")
                .type(CardType.CREDIT)
                .subtype(CardSubtype.PERSONAL)
                .status(CardStatus.ACTIVE)
                .cardNumber("4111111111111111")
                .creditLimit(new BigDecimal("10000"))
                .availableLimit(new BigDecimal("10000"))
                .build();

        when(issueCardUseCase.execute(eq("CLI-1001"), eq("CREDIT"), eq("PERSONAL"),
                eq(new BigDecimal("10000")), anyString()))
                .thenReturn(Single.just(card));

        CreateCardRequest request = new CreateCardRequest(
                "CLI-1001", "CREDIT", "PERSONAL", new BigDecimal("10000"), null);

        webTestClient.post()
                .uri("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectBody();
    }

    @Test
    @DisplayName("POST /api/cards - Rechazo por deuda vencida")
    void shouldRejectIssueCardWhenOverdueDebt() {
        when(issueCardUseCase.execute(anyString(), any(), any(), any(), anyString()))
                .thenReturn(Single.error(new BusinessException(ErrorCode.CUSTOMER_HAS_OVERDUE_DEBT)));

        CreateCardRequest request = new CreateCardRequest(
                "CLI-1001", "CREDIT", "PERSONAL", new BigDecimal("10000"), null);

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
    @DisplayName("GET /api/cards - Lista todas las tarjetas")
    void shouldGetAllCards() {
        Card card1 = Card.builder()
                .id("CARD-001").customerId("CLI-1001").type(CardType.CREDIT)
                .status(CardStatus.ACTIVE).build();
        Card card2 = Card.builder()
                .id("CARD-002").customerId("CLI-1002").type(CardType.DEBIT)
                .status(CardStatus.ACTIVE).build();

        when(findCardsUseCase.findAll()).thenReturn(Flowable.just(card1, card2));

        webTestClient.get()
                .uri("/api/cards")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("CARD-001")
                .jsonPath("$[1].id").isEqualTo("CARD-002")
                .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    @DisplayName("GET /api/cards - Lista vacia cuando no hay tarjetas")
    void shouldGetEmptyCardsList() {
        when(findCardsUseCase.findAll()).thenReturn(Flowable.empty());

        webTestClient.get()
                .uri("/api/cards")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    @DisplayName("GET /api/cards/{id} - Consulta exitosa de tarjeta por ID")
    void shouldGetCardById() {
        Card card = Card.builder()
                .id("CARD-001").customerId("CLI-1001").type(CardType.CREDIT)
                .status(CardStatus.ACTIVE).creditLimit(new BigDecimal("10000")).build();

        when(getCardUseCase.execute("CARD-001")).thenReturn(Single.just(card));

        webTestClient.get()
                .uri("/api/cards/CARD-001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("CARD-001")
                .jsonPath("$.customerId").isEqualTo("CLI-1001");
    }

    @Test
    @DisplayName("GET /api/cards/{id} - 404 cuando la tarjeta no existe")
    void shouldReturn404WhenCardNotFound() {
        when(getCardUseCase.execute("CARD-NOT-FOUND"))
                .thenReturn(Single.error(new BusinessException(ErrorCode.CARD_NOT_FOUND)));

        webTestClient.get()
                .uri("/api/cards/CARD-NOT-FOUND")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").exists();
    }

    @Test
    @DisplayName("GET /api/cards/{id}/limit - Consulta del limite disponible")
    void shouldGetCardLimit() {
        when(getCardLimitUseCase.execute("CARD-001"))
                .thenReturn(Single.just(new BigDecimal("7500")));

        webTestClient.get()
                .uri("/api/cards/CARD-001/limit")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.cardId").isEqualTo("CARD-001");
    }

    @Test
    @DisplayName("PUT /api/cards/{id}/block - Bloqueo exitoso de tarjeta")
    void shouldBlockCardSuccessfully() {
        Card card = Card.builder()
                .id("CARD-001").status(CardStatus.BLOCKED)
                .blockReason("Pérdida reportada").build();

        when(blockCardUseCase.execute(eq("CARD-001"), anyString()))
                .thenReturn(Single.just(card));

        BlockCardRequest request = new BlockCardRequest("Pérdida reportada");

        webTestClient.put()
                .uri("/api/cards/CARD-001/block")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("CARD-001")
                .jsonPath("$.status").isEqualTo("BLOCKED");
    }

    @Test
    @DisplayName("PUT /api/cards/{id}/unblock - Desbloqueo exitoso de tarjeta")
    void shouldUnblockCardSuccessfully() {
        Card card = Card.builder()
                .id("CARD-001").status(CardStatus.ACTIVE).build();

        when(unblockCardUseCase.execute("CARD-001")).thenReturn(Single.just(card));

        webTestClient.put()
                .uri("/api/cards/CARD-001/unblock")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("CARD-001")
                .jsonPath("$.status").isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("DELETE /api/cards/{id} - Cierre exitoso de tarjeta")
    void shouldCloseCardSuccessfully() {
        when(closeCardUseCase.execute("CARD-001")).thenReturn(Completable.complete());

        webTestClient.delete()
                .uri("/api/cards/CARD-001")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("DELETE /api/cards/{id} - Rechazo por saldo pendiente")
    void shouldRejectCloseCardWhenOutstandingBalance() {
        when(closeCardUseCase.execute("CARD-001"))
                .thenReturn(Completable.error(new BusinessException(ErrorCode.OUTSTANDING_BALANCE)));

        webTestClient.delete()
                .uri("/api/cards/CARD-001")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.code").exists();
    }

    // ==================== Consumos ====================

    @Test
    @DisplayName("POST /api/cards/{id}/consumptions - Registro exitoso de consumo")
    void shouldRegisterConsumptionSuccessfully() {
        Consumption consumption = Consumption.builder()
                .id("CONS-001").cardId("CARD-001").customerId("CLI-1001")
                .amount(new BigDecimal("150.50")).merchantId("MERCHANT-001")
                .status(ConsumptionStatus.AUTHORIZED).build();

        when(registerConsumptionUseCase.execute(eq("CARD-001"), any(BigDecimal.class),
                anyString(), anyString())).thenReturn(Single.just(consumption));

        CreateConsumptionRequest request = new CreateConsumptionRequest(
                new BigDecimal("150.50"), "MERCHANT-001", "Compra supermercado");

        webTestClient.post()
                .uri("/api/cards/CARD-001/consumptions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("CONS-001")
                .jsonPath("$.amount").isEqualTo(150.50)
                .jsonPath("$.status").isEqualTo("AUTHORIZED");
    }

    @Test
    @DisplayName("GET /api/cards/{id}/consumptions - Lista de consumos")
    void shouldGetConsumptions() {
        Consumption cons1 = Consumption.builder()
                .id("CONS-001").cardId("CARD-001").amount(new BigDecimal("100"))
                .status(ConsumptionStatus.AUTHORIZED).build();
        Consumption cons2 = Consumption.builder()
                .id("CONS-002").cardId("CARD-001").amount(new BigDecimal("200"))
                .status(ConsumptionStatus.SETTLED).build();

        when(getConsumptionsUseCase.execute("CARD-001")).thenReturn(Flowable.just(cons1, cons2));

        webTestClient.get()
                .uri("/api/cards/CARD-001/consumptions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("CONS-001")
                .jsonPath("$[1].id").isEqualTo("CONS-002")
                .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    @DisplayName("GET /api/cards/{id}/consumptions/{consumptionId} - Detalle de un consumo")
    void shouldGetConsumptionDetail() {
        Consumption consumption = Consumption.builder()
                .id("CONS-001").cardId("CARD-001").customerId("CLI-1001")
                .amount(new BigDecimal("150.50")).merchantId("MERCHANT-001")
                .status(ConsumptionStatus.AUTHORIZED).build();

        when(getConsumptionDetailUseCase.execute("CARD-001", "CONS-001"))
                .thenReturn(Single.just(consumption));

        webTestClient.get()
                .uri("/api/cards/CARD-001/consumptions/CONS-001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("CONS-001")
                .jsonPath("$.amount").isEqualTo(150.50);
    }

    @Test
    @DisplayName("POST /api/cards/{id}/consumptions/{consumptionId}/refund - Reembolso exitoso")
    void shouldRefundConsumptionSuccessfully() {
        Consumption consumption = Consumption.builder()
                .id("CONS-001").cardId("CARD-001")
                .status(ConsumptionStatus.REFUNDED).build();

        when(refundConsumptionUseCase.execute("CARD-001", "CONS-001"))
                .thenReturn(Single.just(consumption));

        webTestClient.post()
                .uri("/api/cards/CARD-001/consumptions/CONS-001/refund")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("CONS-001")
                .jsonPath("$.status").isEqualTo("REFUNDED");
    }

    @Test
    @DisplayName("GET /api/cards/{id}/statement - Estado de cuenta mensual")
    void shouldGetStatement() {
        GetStatementUseCase.StatementResult result = new GetStatementUseCase.StatementResult(
                "CARD-001", LocalDate.of(2026, 11, 1), List.of(), new BigDecimal("1500"), new BigDecimal("8500"));

        when(getStatementUseCase.execute(eq("CARD-001"), anyString()))
                .thenReturn(Flowable.just(result));

        webTestClient.get()
                .uri("/api/cards/CARD-001/statement?month=2026-11")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.cardId").isEqualTo("CARD-001")
                .jsonPath("$.totalAmount").isEqualTo(1500);
    }

    // ==================== Pagos ====================

    @Test
    @DisplayName("POST /api/cards/{id}/payments - Pago exitoso")
    void shouldRegisterPaymentSuccessfully() {
        Card card = Card.builder()
                .id("CARD-001").availableLimit(new BigDecimal("8500")).build();

        when(registerPaymentUseCase.execute(eq("CARD-001"), any(BigDecimal.class)))
                .thenReturn(Single.just(card));

        CreatePaymentRequest request = new CreatePaymentRequest(new BigDecimal("1500"), null);

        webTestClient.post()
                .uri("/api/cards/CARD-001/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.cardId").isEqualTo("CARD-001")
                .jsonPath("$.newAvailableLimit").isEqualTo(8500);
    }
}
