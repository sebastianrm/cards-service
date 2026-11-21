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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del caso de uso de consulta de consumos de una tarjeta.
 */
class GetConsumptionsServiceImplTest {

    private CardPersistencePort cardPersistencePort;
    private ConsumptionPersistencePort consumptionPersistencePort;
    private GetConsumptionsServiceImpl getConsumptionsService;

    @BeforeEach
    void setUp() {
        cardPersistencePort = mock(CardPersistencePort.class);
        consumptionPersistencePort = mock(ConsumptionPersistencePort.class);
        getConsumptionsService = new GetConsumptionsServiceImpl(cardPersistencePort, consumptionPersistencePort);
    }

    @Test
    @DisplayName("Consulta exitosa de consumos de una tarjeta")
    void shouldGetConsumptionsSuccessfully() {
        Card card = Card.builder()
                .id("CARD-001")
                .customerId("CLI-1001")
                .type(CardType.CREDIT)
                .status(CardStatus.ACTIVE)
                .build();

        Consumption c1 = Consumption.builder().id("CONS-001").cardId("CARD-001").amount(new BigDecimal("100")).status(ConsumptionStatus.AUTHORIZED).build();
        Consumption c2 = Consumption.builder().id("CONS-002").cardId("CARD-001").amount(new BigDecimal("200")).status(ConsumptionStatus.SETTLED).build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(consumptionPersistencePort.findByCardId("CARD-001")).thenReturn(Flowable.just(c1, c2));

        TestSubscriber<Consumption> testSubscriber = getConsumptionsService.execute("CARD-001").test();

        try { testSubscriber.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValues(c1, c2);
    }

    @Test
    @DisplayName("Consulta vacía cuando no hay consumos")
    void shouldReturnEmptyWhenNoConsumptions() {
        Card card = Card.builder().id("CARD-001").type(CardType.CREDIT).status(CardStatus.ACTIVE).build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(consumptionPersistencePort.findByCardId("CARD-001")).thenReturn(Flowable.empty());

        TestSubscriber<Consumption> testSubscriber = getConsumptionsService.execute("CARD-001").test();

        try { testSubscriber.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(0);
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta no existe")
    void shouldRejectWhenCardNotFound() {
        when(cardPersistencePort.findById("CARD-NOT-FOUND")).thenReturn(Maybe.empty());

        TestSubscriber<Consumption> testSubscriber = getConsumptionsService.execute("CARD-NOT-FOUND").test();

        try { testSubscriber.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testSubscriber.assertError(BusinessException.class);
        testSubscriber.assertError(e -> e.getMessage().contains("tarjeta no existe"));
    }
}
