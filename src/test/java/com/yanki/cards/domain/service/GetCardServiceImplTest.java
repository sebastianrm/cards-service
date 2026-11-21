package com.yanki.cards.domain.service;

import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.port.out.CardCachePort;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
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
 * Pruebas unitarias del caso de uso de consulta de tarjeta por ID.
 */
class GetCardServiceImplTest {

    private CardPersistencePort cardPersistencePort;
    private CardCachePort cardCachePort;
    private GetCardServiceImpl getCardService;

    @BeforeEach
    void setUp() {
        cardPersistencePort = mock(CardPersistencePort.class);
        cardCachePort = mock(CardCachePort.class);
        when(cardCachePort.getCard(any())).thenReturn(Maybe.empty());
        getCardService = new GetCardServiceImpl(cardPersistencePort, cardCachePort);
    }

    @Test
    @DisplayName("Consulta exitosa de tarjeta por ID")
    void shouldGetCardByIdSuccessfully() {
        Card card = Card.builder()
                .id("CARD-001")
                .customerId("CLI-1001")
                .type(CardType.CREDIT)
                .status(CardStatus.ACTIVE)
                .creditLimit(new BigDecimal("10000"))
                .build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));

        TestObserver<Card> testObserver = getCardService.execute("CARD-001").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(c -> {
            assertThat(c.getId()).isEqualTo("CARD-001");
            assertThat(c.getCustomerId()).isEqualTo("CLI-1001");
            return true;
        });
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta no existe")
    void shouldRejectWhenCardNotFound() {
        when(cardPersistencePort.findById("CARD-NOT-FOUND")).thenReturn(Maybe.empty());

        TestObserver<Card> testObserver = getCardService.execute("CARD-NOT-FOUND").test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("tarjeta no existe"));
    }
}
