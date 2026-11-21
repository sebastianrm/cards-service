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
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del caso de uso de registro de consumos.
 */
class RegisterConsumptionServiceImplTest {

    private CardPersistencePort cardPersistencePort;
    private ConsumptionPersistencePort consumptionPersistencePort;
    private CardEventPublisherPort eventPublisherPort;
    private CardCachePort cardCachePort;
    private RegisterConsumptionServiceImpl registerConsumptionService;

    @BeforeEach
    void setUp() {
        cardPersistencePort = mock(CardPersistencePort.class);
        consumptionPersistencePort = mock(ConsumptionPersistencePort.class);
        eventPublisherPort = mock(CardEventPublisherPort.class);
        cardCachePort = mock(CardCachePort.class);
        when(eventPublisherPort.publishConsumptionRegistered(any())).thenReturn(Completable.complete());
        registerConsumptionService = new RegisterConsumptionServiceImpl(cardPersistencePort, consumptionPersistencePort, eventPublisherPort, cardCachePort);
    }

    @Test
    @DisplayName("Registro exitoso de consumo en tarjeta de crédito con límite suficiente")
    void shouldRegisterCreditConsumptionSuccessfully() {
        // Arrange
        Card card = Card.builder()
                .id("CARD-001")
                .customerId("CLI-1001")
                .type(CardType.CREDIT)
                .status(CardStatus.ACTIVE)
                .creditLimit(new BigDecimal("10000"))
                .availableLimit(new BigDecimal("10000"))
                .build();

        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(consumptionPersistencePort.sumAmountByCardIdAndStatus("CARD-001", "AUTHORIZED"))
                .thenReturn(Single.just(new BigDecimal("2000")));
        when(consumptionPersistencePort.save(any(Consumption.class)))
                .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

        // Act
        TestObserver<Consumption> testObserver = registerConsumptionService
                .execute("CARD-001", new BigDecimal("150.50"), "MERCHANT-001", "Compra supermercado")
                .test();

        // Assert
        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(consumption -> {
            assertThat(consumption.getCardId()).isEqualTo("CARD-001");
            assertThat(consumption.getCustomerId()).isEqualTo("CLI-1001");
            assertThat(consumption.getAmount()).isEqualTo(new BigDecimal("150.50"));
            assertThat(consumption.getMerchantId()).isEqualTo("MERCHANT-001");
            assertThat(consumption.getStatus()).isEqualTo(ConsumptionStatus.AUTHORIZED);
            return true;
        });
    }

    @Test
    @DisplayName("Registro exitoso de consumo en tarjeta de débito")
    void shouldRegisterDebitConsumptionSuccessfully() {
        // Arrange
        Card card = Card.builder()
                .id("CARD-002")
                .customerId("CLI-1001")
                .type(CardType.DEBIT)
                .status(CardStatus.ACTIVE)
                .linkedAccountId("ACC-5001")
                .build();

        when(cardPersistencePort.findById("CARD-002")).thenReturn(Maybe.just(card));
        when(consumptionPersistencePort.save(any(Consumption.class)))
                .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

        // Act
        TestObserver<Consumption> testObserver = registerConsumptionService
                .execute("CARD-002", new BigDecimal("50.00"), "MERCHANT-002", "Compra farmacia")
                .test();

        // Assert
        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(consumption -> {
            assertThat(consumption.getCardId()).isEqualTo("CARD-002");
            assertThat(consumption.getStatus()).isEqualTo(ConsumptionStatus.AUTHORIZED);
            return true;
        });
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta no existe")
    void shouldRejectWhenCardNotFound() {
        when(cardPersistencePort.findById("CARD-NOT-FOUND")).thenReturn(Maybe.empty());

        TestObserver<Consumption> testObserver = registerConsumptionService
                .execute("CARD-NOT-FOUND", new BigDecimal("100"), "MERCHANT-001", null)
                .test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("tarjeta no existe"));
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta está bloqueada")
    void shouldRejectWhenCardBlocked() {
        Card card = Card.builder()
                .id("CARD-001")
                .type(CardType.CREDIT)
                .status(CardStatus.BLOCKED)
                .creditLimit(new BigDecimal("10000"))
                .build();
        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));

        TestObserver<Consumption> testObserver = registerConsumptionService
                .execute("CARD-001", new BigDecimal("100"), "MERCHANT-001", null)
                .test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("bloqueada"));
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta está cerrada")
    void shouldRejectWhenCardClosed() {
        Card card = Card.builder()
                .id("CARD-001")
                .type(CardType.CREDIT)
                .status(CardStatus.CLOSED)
                .creditLimit(new BigDecimal("10000"))
                .build();
        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));

        TestObserver<Consumption> testObserver = registerConsumptionService
                .execute("CARD-001", new BigDecimal("100"), "MERCHANT-001", null)
                .test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("cerrada"));
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta está vencida")
    void shouldRejectWhenCardExpired() {
        Card card = Card.builder()
                .id("CARD-001")
                .type(CardType.CREDIT)
                .status(CardStatus.ACTIVE)
                .creditLimit(new BigDecimal("10000"))
                .expiryDate(LocalDateTime.now().minusDays(1))
                .build();
        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));

        TestObserver<Consumption> testObserver = registerConsumptionService
                .execute("CARD-001", new BigDecimal("100"), "MERCHANT-001", null)
                .test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("vencida"));
    }

    @Test
    @DisplayName("Rechazo cuando el límite de crédito es insuficiente")
    void shouldRejectWhenInsufficientCreditLimit() {
        Card card = Card.builder()
                .id("CARD-001")
                .type(CardType.CREDIT)
                .status(CardStatus.ACTIVE)
                .creditLimit(new BigDecimal("1000"))
                .build();
        when(cardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(card));
        when(consumptionPersistencePort.sumAmountByCardIdAndStatus("CARD-001", "AUTHORIZED"))
                .thenReturn(Single.just(new BigDecimal("950")));

        TestObserver<Consumption> testObserver = registerConsumptionService
                .execute("CARD-001", new BigDecimal("100"), "MERCHANT-001", null)
                .test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("Límite de crédito insuficiente"));
    }

    @Test
    @DisplayName("Rechazo cuando el monto es nulo")
    void shouldRejectWhenAmountIsNull() {
        TestObserver<Consumption> testObserver = registerConsumptionService
                .execute("CARD-001", null, "MERCHANT-001", null)
                .test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("monto"));
    }

    @Test
    @DisplayName("Rechazo cuando el monto es cero o negativo")
    void shouldRejectWhenAmountIsZeroOrNegative() {
        TestObserver<Consumption> testObserver = registerConsumptionService
                .execute("CARD-001", BigDecimal.ZERO, "MERCHANT-001", null)
                .test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
    }
}
