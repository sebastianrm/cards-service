package com.yanki.cards.domain.service;

import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.port.out.CardCachePort;
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
 * Pruebas unitarias del caso de uso de consulta de límite disponible.
 */
class GetCardLimitServiceImplTest {

    private CardPersistencePort cardPersistencePort;
    private ConsumptionPersistencePort consumptionPersistencePort;
    private CardCachePort cardCachePort;
    private GetCardLimitServiceImpl getCardLimitService;

    @BeforeEach
    void setUp() {
        cardPersistencePort = mock(CardPersistencePort.class);
        consumptionPersistencePort = mock(ConsumptionPersistencePort.class);
        cardCachePort = mock(CardCachePort.class);
        when(cardCachePort.getCardLimit(any())).thenReturn(Maybe.empty());
        getCardLimitService = new GetCardLimitServiceImpl(cardPersistencePort, consumptionPersistencePort, cardCachePort);
    }

    @Test
    @DisplayName("Consulta exitosa del límite disponible de tarjeta de crédito")
    void shouldGetCardLimitSuccessfully() {
        Card card = Card.builder()
                .id("CARD-001")
                .type(CardType.CREDIT)
                .status(CardStatus.ACTIVE)
                .creditLimit(new BigDecimal("10000"))
                .build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(consumptionPersistencePort.sumAmountByCardIdAndStatus("CARD-001", "AUTHORIZED"))
                .thenReturn(Single.just(new BigDecimal("2500")));

        TestObserver<BigDecimal> testObserver = getCardLimitService.execute("CARD-001").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(limit -> limit.compareTo(new BigDecimal("7500")) == 0);
    }

    @Test
    @DisplayName("Límite disponible es igual al crédito total cuando no hay consumos")
    void shouldReturnFullLimitWhenNoConsumptions() {
        Card card = Card.builder()
                .id("CARD-001")
                .type(CardType.CREDIT)
                .status(CardStatus.ACTIVE)
                .creditLimit(new BigDecimal("10000"))
                .build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(consumptionPersistencePort.sumAmountByCardIdAndStatus("CARD-001", "AUTHORIZED"))
                .thenReturn(Single.just(BigDecimal.ZERO));

        TestObserver<BigDecimal> testObserver = getCardLimitService.execute("CARD-001").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(limit -> limit.compareTo(new BigDecimal("10000")) == 0);
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta no existe")
    void shouldRejectWhenCardNotFound() {
        when(cardPersistencePort.findById("CARD-NOT-FOUND")).thenReturn(Maybe.empty());

        TestObserver<BigDecimal> testObserver = getCardLimitService.execute("CARD-NOT-FOUND").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("tarjeta no existe"));
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta es de débito")
    void shouldRejectWhenCardIsDebit() {
        Card card = Card.builder().id("CARD-001").type(CardType.DEBIT).status(CardStatus.ACTIVE).build();
        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));

        TestObserver<BigDecimal> testObserver = getCardLimitService.execute("CARD-001").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("no aplica"));
    }
}
