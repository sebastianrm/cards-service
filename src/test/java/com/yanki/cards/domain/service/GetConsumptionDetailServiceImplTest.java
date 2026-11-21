package com.yanki.cards.domain.service;

import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.model.Consumption;
import com.yanki.cards.domain.model.ConsumptionStatus;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import com.yanki.cards.domain.port.out.ConsumptionPersistencePort;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.observers.TestObserver;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del caso de uso de consulta de detalle de un consumo.
 */
class GetConsumptionDetailServiceImplTest {

    private CardPersistencePort cardPersistencePort;
    private ConsumptionPersistencePort consumptionPersistencePort;
    private GetConsumptionDetailServiceImpl getConsumptionDetailService;

    @BeforeEach
    void setUp() {
        cardPersistencePort = mock(CardPersistencePort.class);
        consumptionPersistencePort = mock(ConsumptionPersistencePort.class);
        getConsumptionDetailService = new GetConsumptionDetailServiceImpl(cardPersistencePort, consumptionPersistencePort);
    }

    @Test
    @DisplayName("Consulta exitosa del detalle de un consumo")
    void shouldGetConsumptionDetailSuccessfully() {
        Card card = Card.builder().id("CARD-001").type(CardType.CREDIT).status(CardStatus.ACTIVE).build();
        Consumption consumption = Consumption.builder()
                .id("CONS-001")
                .cardId("CARD-001")
                .customerId("CLI-1001")
                .amount(new BigDecimal("150.50"))
                .merchantId("MERCHANT-001")
                .status(ConsumptionStatus.AUTHORIZED)
                .build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(consumptionPersistencePort.findByIdAndCardId("CONS-001", "CARD-001")).thenReturn(Maybe.just(consumption));

        TestObserver<Consumption> testObserver = getConsumptionDetailService
                .execute("CARD-001", "CONS-001").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(c -> {
            assertThat(c.getId()).isEqualTo("CONS-001");
            assertThat(c.getAmount()).isEqualTo(new BigDecimal("150.50"));
            return true;
        });
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta no existe")
    void shouldRejectWhenCardNotFound() {
        when(cardPersistencePort.findById("CARD-NOT-FOUND")).thenReturn(Maybe.empty());

        TestObserver<Consumption> testObserver = getConsumptionDetailService
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

        TestObserver<Consumption> testObserver = getConsumptionDetailService
                .execute("CARD-001", "CONS-NOT-FOUND").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("consumo no existe"));
    }
}
