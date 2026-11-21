package com.yanki.cards.domain.service;

import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.model.Consumption;
import com.yanki.cards.domain.model.ConsumptionStatus;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del caso de uso de reembolso de consumos.
 */
class RefundConsumptionServiceImplTest {

    private CardPersistencePort cardPersistencePort;
    private ConsumptionPersistencePort consumptionPersistencePort;
    private CardEventPublisherPort eventPublisherPort;
    private CardCachePort cardCachePort;
    private RefundConsumptionServiceImpl refundConsumptionService;

    @BeforeEach
    void setUp() {
        cardPersistencePort = mock(CardPersistencePort.class);
        consumptionPersistencePort = mock(ConsumptionPersistencePort.class);
        eventPublisherPort = mock(CardEventPublisherPort.class);
        cardCachePort = mock(CardCachePort.class);
        when(eventPublisherPort.publishConsumptionRefunded(any())).thenReturn(Completable.complete());
        refundConsumptionService = new RefundConsumptionServiceImpl(cardPersistencePort, consumptionPersistencePort, eventPublisherPort, cardCachePort);
    }

    @Test
    @DisplayName("Reembolso exitoso de consumo autorizado")
    void shouldRefundAuthorizedConsumptionSuccessfully() {
        Card card = Card.builder().id("CARD-001").type(CardType.CREDIT).status(CardStatus.ACTIVE).build();
        Consumption consumption = Consumption.builder()
                .id("CONS-001")
                .cardId("CARD-001")
                .amount(new BigDecimal("150.50"))
                .status(ConsumptionStatus.AUTHORIZED)
                .build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(consumptionPersistencePort.findByIdAndCardId("CONS-001", "CARD-001")).thenReturn(Maybe.just(consumption));
        when(consumptionPersistencePort.save(any(Consumption.class)))
                .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

        TestObserver<Consumption> testObserver = refundConsumptionService
                .execute("CARD-001", "CONS-001").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(c -> {
            assertThat(c.getStatus()).isEqualTo(ConsumptionStatus.REFUNDED);
            return true;
        });
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta no existe")
    void shouldRejectWhenCardNotFound() {
        when(cardPersistencePort.findById("CARD-NOT-FOUND")).thenReturn(Maybe.empty());

        TestObserver<Consumption> testObserver = refundConsumptionService
                .execute("CARD-NOT-FOUND", "CONS-001").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("tarjeta no existe"));
    }

    @Test
    @DisplayName("Rechazo cuando el consumo no existe")
    void shouldRejectWhenConsumptionNotFound() {
        Card card = Card.builder().id("CARD-001").type(CardType.CREDIT).status(CardStatus.ACTIVE).build();
        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(consumptionPersistencePort.findByIdAndCardId("CONS-NOT-FOUND", "CARD-001")).thenReturn(Maybe.empty());

        TestObserver<Consumption> testObserver = refundConsumptionService
                .execute("CARD-001", "CONS-NOT-FOUND").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("consumo no existe"));
    }

    @Test
    @DisplayName("Rechazo cuando el consumo ya fue reembolsado")
    void shouldRejectWhenConsumptionAlreadyRefunded() {
        Card card = Card.builder().id("CARD-001").type(CardType.CREDIT).status(CardStatus.ACTIVE).build();
        Consumption consumption = Consumption.builder()
                .id("CONS-001").cardId("CARD-001").status(ConsumptionStatus.REFUNDED).build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(consumptionPersistencePort.findByIdAndCardId("CONS-001", "CARD-001")).thenReturn(Maybe.just(consumption));

        TestObserver<Consumption> testObserver = refundConsumptionService
                .execute("CARD-001", "CONS-001").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("reembolsado"));
    }

    @Test
    @DisplayName("Rechazo cuando el consumo está liquidado (no reembolsable)")
    void shouldRejectWhenConsumptionSettled() {
        Card card = Card.builder().id("CARD-001").type(CardType.CREDIT).status(CardStatus.ACTIVE).build();
        Consumption consumption = Consumption.builder()
                .id("CONS-001").cardId("CARD-001").status(ConsumptionStatus.SETTLED).build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(consumptionPersistencePort.findByIdAndCardId("CONS-001", "CARD-001")).thenReturn(Maybe.just(consumption));

        TestObserver<Consumption> testObserver = refundConsumptionService
                .execute("CARD-001", "CONS-001").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("reembolsable"));
    }
}
