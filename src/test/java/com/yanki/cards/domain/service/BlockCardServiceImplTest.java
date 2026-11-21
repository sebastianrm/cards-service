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
 * Pruebas unitarias del caso de uso de bloqueo de tarjetas.
 */
class BlockCardServiceImplTest {

    private CardPersistencePort cardPersistencePort;
    private CardEventPublisherPort eventPublisherPort;
    private CardCachePort cardCachePort;
    private BlockCardServiceImpl blockCardService;

    @BeforeEach
    void setUp() {
        cardPersistencePort = mock(CardPersistencePort.class);
        eventPublisherPort = mock(CardEventPublisherPort.class);
        cardCachePort = mock(CardCachePort.class);
        when(eventPublisherPort.publishCardBlocked(any())).thenReturn(Completable.complete());
        blockCardService = new BlockCardServiceImpl(cardPersistencePort, eventPublisherPort, cardCachePort);
    }

    @Test
    @DisplayName("Bloqueo exitoso de tarjeta activa")
    void shouldBlockActiveCardSuccessfully() {
        Card card = Card.builder()
                .id("CARD-001")
                .customerId("CLI-1001")
                .type(CardType.CREDIT)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(cardPersistencePort.save(any(Card.class))).thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

        TestObserver<Card> testObserver = blockCardService.execute("CARD-001", "Tarjeta perdida").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(c -> {
            assertThat(c.getStatus()).isEqualTo(CardStatus.BLOCKED);
            assertThat(c.getBlockReason()).isEqualTo("Tarjeta perdida");
            assertThat(c.getBlockedAt()).isNotNull();
            return true;
        });
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta no existe")
    void shouldRejectWhenCardNotFound() {
        when(cardPersistencePort.findById("CARD-NOT-FOUND")).thenReturn(Maybe.empty());

        TestObserver<Card> testObserver = blockCardService.execute("CARD-NOT-FOUND", null).test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("tarjeta no existe"));
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta ya está cerrada")
    void shouldRejectWhenCardAlreadyClosed() {
        Card card = Card.builder().id("CARD-001").status(CardStatus.CLOSED).build();
        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));

        TestObserver<Card> testObserver = blockCardService.execute("CARD-001", null).test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("cerrada"));
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta ya está bloqueada")
    void shouldRejectWhenCardAlreadyBlocked() {
        Card card = Card.builder().id("CARD-001").status(CardStatus.BLOCKED).build();
        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));

        TestObserver<Card> testObserver = blockCardService.execute("CARD-001", null).test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("bloqueada"));
    }
}
