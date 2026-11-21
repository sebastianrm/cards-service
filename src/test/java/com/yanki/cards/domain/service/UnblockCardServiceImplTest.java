package com.yanki.cards.domain.service;

import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.port.out.CardCachePort;
import com.yanki.cards.domain.port.out.CardEventPublisherPort;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del caso de uso de desbloqueo de tarjetas.
 */
class UnblockCardServiceImplTest {

    private CardPersistencePort cardPersistencePort;
    private CardEventPublisherPort eventPublisherPort;
    private CardCachePort cardCachePort;
    private UnblockCardServiceImpl unblockCardService;

    @BeforeEach
    void setUp() {
        cardPersistencePort = mock(CardPersistencePort.class);
        eventPublisherPort = mock(CardEventPublisherPort.class);
        cardCachePort = mock(CardCachePort.class);
        when(eventPublisherPort.publishCardUnblocked(any())).thenReturn(Completable.complete());
        unblockCardService = new UnblockCardServiceImpl(cardPersistencePort, eventPublisherPort, cardCachePort);
    }

    @Test
    @DisplayName("Desbloqueo exitoso de tarjeta bloqueada")
    void shouldUnblockBlockedCardSuccessfully() {
        Card card = Card.builder()
                .id("CARD-001")
                .customerId("CLI-1001")
                .type(CardType.CREDIT)
                .status(CardStatus.BLOCKED)
                .blockReason("Tarjeta perdida")
                .build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(cardPersistencePort.save(any(Card.class))).thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

        TestObserver<Card> testObserver = unblockCardService.execute("CARD-001").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(c -> {
            assertThat(c.getStatus()).isEqualTo(CardStatus.ACTIVE);
            assertThat(c.getBlockReason()).isNull();
            assertThat(c.getBlockedAt()).isNull();
            return true;
        });
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta no existe")
    void shouldRejectWhenCardNotFound() {
        when(cardPersistencePort.findById("CARD-NOT-FOUND")).thenReturn(Maybe.empty());

        TestObserver<Card> testObserver = unblockCardService.execute("CARD-NOT-FOUND").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("tarjeta no existe"));
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta ya está cerrada")
    void shouldRejectWhenCardAlreadyClosed() {
        Card card = Card.builder().id("CARD-001").status(CardStatus.CLOSED).build();
        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));

        TestObserver<Card> testObserver = unblockCardService.execute("CARD-001").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("cerrada"));
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta no está bloqueada")
    void shouldRejectWhenCardNotBlocked() {
        Card card = Card.builder().id("CARD-001").status(CardStatus.ACTIVE).build();
        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));

        TestObserver<Card> testObserver = unblockCardService.execute("CARD-001").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("no está bloqueada"));
    }
}
