package com.yanki.cards.domain.service;

import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.port.out.CardCachePort;
import com.yanki.cards.domain.port.out.CardEventPublisherPort;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import com.yanki.cards.domain.port.out.ConsumptionPersistencePort;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del caso de uso de cierre de tarjetas.
 */
class CloseCardServiceImplTest {

    private CardPersistencePort cardPersistencePort;
    private ConsumptionPersistencePort consumptionPersistencePort;
    private CardEventPublisherPort eventPublisherPort;
    private CardCachePort cardCachePort;
    private CloseCardServiceImpl closeCardService;

    @BeforeEach
    void setUp() {
        cardPersistencePort = mock(CardPersistencePort.class);
        consumptionPersistencePort = mock(ConsumptionPersistencePort.class);
        eventPublisherPort = mock(CardEventPublisherPort.class);
        cardCachePort = mock(CardCachePort.class);
        when(eventPublisherPort.publishCardClosed(any())).thenReturn(Completable.complete());
        closeCardService = new CloseCardServiceImpl(cardPersistencePort, consumptionPersistencePort, eventPublisherPort, cardCachePort);
    }

    @Test
    @DisplayName("Cierre exitoso de tarjeta de crédito sin saldo pendiente")
    void shouldCloseCreditCardWithNoOutstandingBalance() {
        Card card = Card.builder()
                .id("CARD-001")
                .type(CardType.CREDIT)
                .status(CardStatus.ACTIVE)
                .creditLimit(new BigDecimal("10000"))
                .build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(consumptionPersistencePort.sumAmountByCardIdAndStatus("CARD-001", "AUTHORIZED"))
                .thenReturn(Single.just(BigDecimal.ZERO));
        when(cardPersistencePort.save(any(Card.class))).thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

        TestObserver<Void> testObserver = closeCardService.execute("CARD-001").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    @DisplayName("Cierre exitoso de tarjeta de débito")
    void shouldCloseDebitCardSuccessfully() {
        Card card = Card.builder()
                .id("CARD-002")
                .type(CardType.DEBIT)
                .status(CardStatus.ACTIVE)
                .linkedAccountId("ACC-5001")
                .build();

        when(cardPersistencePort.findById("CARD-002")).thenReturn(Maybe.just(card));
        when(cardPersistencePort.save(any(Card.class))).thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

        TestObserver<Void> testObserver = closeCardService.execute("CARD-002").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta no existe")
    void shouldRejectWhenCardNotFound() {
        when(cardPersistencePort.findById("CARD-NOT-FOUND")).thenReturn(Maybe.empty());

        TestObserver<Void> testObserver = closeCardService.execute("CARD-NOT-FOUND").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("tarjeta no existe"));
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta ya está cerrada")
    void shouldRejectWhenCardAlreadyClosed() {
        Card card = Card.builder().id("CARD-001").status(CardStatus.CLOSED).build();
        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));

        TestObserver<Void> testObserver = closeCardService.execute("CARD-001").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("cerrada"));
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta de crédito tiene saldo pendiente")
    void shouldRejectCreditCardWithOutstandingBalance() {
        Card card = Card.builder()
                .id("CARD-001")
                .type(CardType.CREDIT)
                .status(CardStatus.ACTIVE)
                .creditLimit(new BigDecimal("10000"))
                .build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(consumptionPersistencePort.sumAmountByCardIdAndStatus("CARD-001", "AUTHORIZED"))
                .thenReturn(Single.just(new BigDecimal("500")));

        TestObserver<Void> testObserver = closeCardService.execute("CARD-001").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("saldo pendiente"));
    }
}
