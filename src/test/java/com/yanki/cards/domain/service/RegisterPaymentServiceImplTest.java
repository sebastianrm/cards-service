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
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del caso de uso de registro de pagos.
 */
class RegisterPaymentServiceImplTest {

    private CardPersistencePort cardPersistencePort;
    private CardEventPublisherPort eventPublisherPort;
    private CardCachePort cardCachePort;
    private RegisterPaymentServiceImpl registerPaymentService;

    @BeforeEach
    void setUp() {
        cardPersistencePort = mock(CardPersistencePort.class);
        eventPublisherPort = mock(CardEventPublisherPort.class);
        cardCachePort = mock(CardCachePort.class);
        when(eventPublisherPort.publishPaymentRegistered(any())).thenReturn(Completable.complete());
        registerPaymentService = new RegisterPaymentServiceImpl(cardPersistencePort, eventPublisherPort, cardCachePort);
    }

    @Test
    @DisplayName("Registro exitoso de pago en tarjeta de crédito")
    void shouldRegisterPaymentSuccessfully() {
        Card card = Card.builder()
                .id("CARD-001")
                .type(CardType.CREDIT)
                .status(CardStatus.ACTIVE)
                .creditLimit(new BigDecimal("10000"))
                .availableLimit(new BigDecimal("7000"))
                .build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(cardPersistencePort.save(any(Card.class))).thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

        TestObserver<Card> testObserver = registerPaymentService
                .execute("CARD-001", new BigDecimal("500")).test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(c -> {
            assertThat(c.getAvailableLimit().compareTo(new BigDecimal("7500"))).isZero();
            return true;
        });
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta no existe")
    void shouldRejectWhenCardNotFound() {
        when(cardPersistencePort.findById("CARD-NOT-FOUND")).thenReturn(Maybe.empty());

        TestObserver<Card> testObserver = registerPaymentService
                .execute("CARD-NOT-FOUND", new BigDecimal("500")).test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("tarjeta no existe"));
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta es de débito")
    void shouldRejectWhenCardIsDebit() {
        Card card = Card.builder().id("CARD-001").type(CardType.DEBIT).status(CardStatus.ACTIVE).build();
        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));

        TestObserver<Card> testObserver = registerPaymentService
                .execute("CARD-001", new BigDecimal("500")).test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("Tipo de tarjeta"));
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta está cerrada")
    void shouldRejectWhenCardClosed() {
        Card card = Card.builder().id("CARD-001").type(CardType.CREDIT).status(CardStatus.CLOSED).build();
        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));

        TestObserver<Card> testObserver = registerPaymentService
                .execute("CARD-001", new BigDecimal("500")).test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("cerrada"));
    }

    @Test
    @DisplayName("Rechazo cuando el monto es nulo")
    void shouldRejectWhenAmountIsNull() {
        TestObserver<Card> testObserver = registerPaymentService
                .execute("CARD-001", null).test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("monto"));
    }

    @Test
    @DisplayName("Rechazo cuando el monto es cero")
    void shouldRejectWhenAmountIsZero() {
        TestObserver<Card> testObserver = registerPaymentService
                .execute("CARD-001", BigDecimal.ZERO).test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
    }
}
