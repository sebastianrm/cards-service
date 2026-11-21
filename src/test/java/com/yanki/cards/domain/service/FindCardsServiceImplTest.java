package com.yanki.cards.domain.service;

import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del caso de uso de consulta de tarjetas.
 */
class FindCardsServiceImplTest {

    private CardPersistencePort cardPersistencePort;
    private FindCardsServiceImpl findCardsService;

    @BeforeEach
    void setUp() {
        cardPersistencePort = mock(CardPersistencePort.class);
        findCardsService = new FindCardsServiceImpl(cardPersistencePort);
    }

    @Test
    @DisplayName("Consulta exitosa de todas las tarjetas")
    void shouldFindAllCardsSuccessfully() {
        Card card1 = Card.builder()
                .id("CARD-001")
                .customerId("CLI-1001")
                .type(CardType.CREDIT)
                .status(CardStatus.ACTIVE)
                .build();
        Card card2 = Card.builder()
                .id("CARD-002")
                .customerId("CLI-1002")
                .type(CardType.DEBIT)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardPersistencePort.findAll()).thenReturn(Flowable.just(card1, card2));

        TestSubscriber<Card> testSubscriber = findCardsService.findAll().test();

        try { testSubscriber.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(2);
    }

    @Test
    @DisplayName("Resultado vacío cuando no hay tarjetas")
    void shouldReturnEmptyWhenNoCards() {
        when(cardPersistencePort.findAll()).thenReturn(Flowable.empty());

        TestSubscriber<Card> testSubscriber = findCardsService.findAll().test();

        try { testSubscriber.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(0);
    }

    @Test
    @DisplayName("Consulta exitosa de tarjetas por cliente")
    void shouldFindCardsByCustomerIdSuccessfully() {
        Card card1 = Card.builder()
                .id("CARD-001")
                .customerId("CLI-1001")
                .type(CardType.CREDIT)
                .status(CardStatus.ACTIVE)
                .build();
        Card card2 = Card.builder()
                .id("CARD-002")
                .customerId("CLI-1001")
                .type(CardType.DEBIT)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardPersistencePort.findByCustomerId("CLI-1001")).thenReturn(Flowable.just(card1, card2));

        TestSubscriber<Card> testSubscriber = findCardsService.findByCustomerId("CLI-1001").test();

        try { testSubscriber.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValues(card1, card2);
    }

    @Test
    @DisplayName("Resultado vacío cuando el cliente no tiene tarjetas")
    void shouldReturnEmptyWhenCustomerHasNoCards() {
        when(cardPersistencePort.findByCustomerId("CLI-EMPTY")).thenReturn(Flowable.empty());

        TestSubscriber<Card> testSubscriber = findCardsService.findByCustomerId("CLI-EMPTY").test();

        try { testSubscriber.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(0);
    }
}
