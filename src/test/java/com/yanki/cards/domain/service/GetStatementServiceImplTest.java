package com.yanki.cards.domain.service;

import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.model.Consumption;
import com.yanki.cards.domain.model.ConsumptionStatus;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import com.yanki.cards.domain.port.out.ConsumptionPersistencePort;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del caso de uso de consulta de estado de cuenta mensual.
 */
class GetStatementServiceImplTest {

    private CardPersistencePort cardPersistencePort;
    private ConsumptionPersistencePort consumptionPersistencePort;
    private GetStatementServiceImpl getStatementService;

    @BeforeEach
    void setUp() {
        cardPersistencePort = mock(CardPersistencePort.class);
        consumptionPersistencePort = mock(ConsumptionPersistencePort.class);
        getStatementService = new GetStatementServiceImpl(cardPersistencePort, consumptionPersistencePort);
    }

    @Test
    @DisplayName("Consulta exitosa del estado de cuenta mensual")
    void shouldGetStatementSuccessfully() {
        Card card = Card.builder()
                .id("CARD-001")
                .type(CardType.CREDIT)
                .status(CardStatus.ACTIVE)
                .creditLimit(new BigDecimal("10000"))
                .availableLimit(new BigDecimal("7500"))
                .build();

        Consumption c1 = Consumption.builder().id("CONS-001").cardId("CARD-001").amount(new BigDecimal("100")).status(ConsumptionStatus.AUTHORIZED).build();
        Consumption c2 = Consumption.builder().id("CONS-002").cardId("CARD-001").amount(new BigDecimal("150")).status(ConsumptionStatus.AUTHORIZED).build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(consumptionPersistencePort.findByCardIdAndStatus("CARD-001", "AUTHORIZED"))
                .thenReturn(Flowable.just(c1, c2));

        TestSubscriber<GetStatementServiceImpl.StatementResult> testSubscriber = getStatementService
                .execute("CARD-001", "2026-11").test();

        try { testSubscriber.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValue(result -> {
            assertThat(result.cardId()).isEqualTo("CARD-001");
            assertThat(result.totalAmount()).isEqualTo(new BigDecimal("250"));
            assertThat(result.consumptions()).hasSize(2);
            assertThat(result.availableLimit()).isEqualTo(new BigDecimal("7500"));
            return true;
        });
    }

    @Test
    @DisplayName("Estado de cuenta vacío cuando no hay consumos")
    void shouldGetEmptyStatementWhenNoConsumptions() {
        Card card = Card.builder()
                .id("CARD-001")
                .type(CardType.CREDIT)
                .status(CardStatus.ACTIVE)
                .availableLimit(new BigDecimal("10000"))
                .build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(consumptionPersistencePort.findByCardIdAndStatus("CARD-001", "AUTHORIZED"))
                .thenReturn(Flowable.empty());

        TestSubscriber<GetStatementServiceImpl.StatementResult> testSubscriber = getStatementService
                .execute("CARD-001", "2026-11").test();

        try { testSubscriber.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValue(result -> {
            assertThat(result.totalAmount()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.consumptions()).isEmpty();
            return true;
        });
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta no existe")
    void shouldRejectWhenCardNotFound() {
        when(cardPersistencePort.findById("CARD-NOT-FOUND")).thenReturn(Maybe.empty());

        TestSubscriber<GetStatementServiceImpl.StatementResult> testSubscriber = getStatementService
                .execute("CARD-NOT-FOUND", "2026-11").test();

        try { testSubscriber.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testSubscriber.assertError(BusinessException.class);
        testSubscriber.assertError(e -> e.getMessage().contains("tarjeta no existe"));
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta es de débito")
    void shouldRejectWhenCardIsDebit() {
        Card card = Card.builder().id("CARD-001").type(CardType.DEBIT).status(CardStatus.ACTIVE).build();
        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));

        TestSubscriber<GetStatementServiceImpl.StatementResult> testSubscriber = getStatementService
                .execute("CARD-001", "2026-11").test();

        try { testSubscriber.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testSubscriber.assertError(BusinessException.class);
        testSubscriber.assertError(e -> e.getMessage().contains("Tipo de tarjeta"));
    }

    @Test
    @DisplayName("Rechazo cuando el formato del mes es inválido")
    void shouldRejectWhenMonthFormatInvalid() {
        TestSubscriber<GetStatementServiceImpl.StatementResult> testSubscriber = getStatementService
                .execute("CARD-001", "invalid").test();

        try { testSubscriber.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testSubscriber.assertError(BusinessException.class);
        testSubscriber.assertError(e -> e.getMessage().contains("filtros"));
    }

    @Test
    @DisplayName("Rechazo cuando el mes es nulo")
    void shouldRejectWhenMonthIsNull() {
        TestSubscriber<GetStatementServiceImpl.StatementResult> testSubscriber = getStatementService
                .execute("CARD-001", null).test();

        try { testSubscriber.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testSubscriber.assertError(BusinessException.class);
        testSubscriber.assertError(e -> e.getMessage().contains("filtros"));
    }
}
