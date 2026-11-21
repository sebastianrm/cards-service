package com.yanki.cards.domain.service;

import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.exception.ErrorCode;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.model.CustomerProfileView;
import com.yanki.cards.domain.model.CustomerRiskView;
import com.yanki.cards.domain.port.out.CardCachePort;
import com.yanki.cards.domain.port.out.CardEventPublisherPort;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import com.yanki.cards.domain.port.out.CustomerProfileViewPersistencePort;
import com.yanki.cards.domain.port.out.CustomerRiskViewPersistencePort;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del caso de uso de emisión de tarjetas.
 * <p>
 * Cubre los casos de prueba CP-001, CP-002 y CP-003 documentados,
 * así como escenarios adicionales de error y validación.
 * </p>
 */
class IssueCardServiceImplTest {

    private CardPersistencePort cardPersistencePort;
    private CustomerProfileViewPersistencePort customerProfileViewPort;
    private CustomerRiskViewPersistencePort customerRiskViewPort;
    private CardEventPublisherPort eventPublisherPort;
    private CardCachePort cardCachePort;
    private IssueCardServiceImpl issueCardService;

    @BeforeEach
    void setUp() {
        cardPersistencePort = mock(CardPersistencePort.class);
        customerProfileViewPort = mock(CustomerProfileViewPersistencePort.class);
        customerRiskViewPort = mock(CustomerRiskViewPersistencePort.class);
        eventPublisherPort = mock(CardEventPublisherPort.class);
        cardCachePort = mock(CardCachePort.class);
        when(eventPublisherPort.publishCardOpened(any())).thenReturn(Completable.complete());
        issueCardService = new IssueCardServiceImpl(cardPersistencePort, customerProfileViewPort, customerRiskViewPort, eventPublisherPort, cardCachePort);
    }

    // ==================== CP-001: Emisión exitosa de tarjeta de crédito personal ====================

    @Test
    @DisplayName("CP-001: Emisión exitosa de tarjeta de crédito personal - Happy Path")
    void shouldIssueCreditCardSuccessfully() {
        // Arrange
        String customerId = "CLI-1001";
        CustomerProfileView profile = CustomerProfileView.builder()
                .customerId(customerId)
                .status("ACTIVE")
                .build();
        CustomerRiskView risk = CustomerRiskView.builder()
                .customerId(customerId)
                .hasOverdueDebt(false)
                .build();

        when(customerProfileViewPort.findByCustomerId(customerId)).thenReturn(Maybe.just(profile));
        when(customerRiskViewPort.findByCustomerId(customerId)).thenReturn(Maybe.just(risk));
        when(cardPersistencePort.existsByCustomerIdAndTypeAndSubtypeAndStatus(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Single.just(false));
        when(cardPersistencePort.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            return Single.just(card);
        });

        // Act
        TestObserver<Card> testObserver = issueCardService
                .execute(customerId, "CREDIT", "PERSONAL", new BigDecimal("5000"), null)
                .test();

        // Assert
        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(card -> {
            assertThat(card.getCustomerId()).isEqualTo(customerId);
            assertThat(card.getType()).isEqualTo(CardType.CREDIT);
            assertThat(card.getStatus()).isEqualTo(CardStatus.ISSUED);
            assertThat(card.getCreditLimit()).isEqualTo(new BigDecimal("5000"));
            assertThat(card.getAvailableLimit()).isEqualTo(new BigDecimal("5000"));
            assertThat(card.getLinkedAccountId()).isNull();
            assertThat(card.getIssuedAt()).isNotNull();
            assertThat(card.getCreatedAt()).isNotNull();
            return true;
        });
    }

    // ==================== CP-002: Emisión exitosa de tarjeta de débito ====================

    @Test
    @DisplayName("CP-002: Emisión exitosa de tarjeta de débito - Happy Path")
    void shouldIssueDebitCardSuccessfully() {
        // Arrange
        String customerId = "CLI-1001";
        String accountId = "ACC-5001";
        CustomerProfileView profile = CustomerProfileView.builder()
                .customerId(customerId)
                .status("ACTIVE")
                .build();

        when(customerProfileViewPort.findByCustomerId(customerId)).thenReturn(Maybe.just(profile));
        when(customerRiskViewPort.findByCustomerId(customerId)).thenReturn(Maybe.empty());
        when(cardPersistencePort.existsByLinkedAccountIdAndType(anyString(), anyString()))
                .thenReturn(Single.just(false));
        when(cardPersistencePort.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            return Single.just(card);
        });

        // Act
        TestObserver<Card> testObserver = issueCardService
                .execute(customerId, "DEBIT", null, null, accountId)
                .test();

        // Assert
        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(card -> {
            assertThat(card.getCustomerId()).isEqualTo(customerId);
            assertThat(card.getType()).isEqualTo(CardType.DEBIT);
            assertThat(card.getStatus()).isEqualTo(CardStatus.ACTIVE);
            assertThat(card.getLinkedAccountId()).isEqualTo(accountId);
            return true;
        });
    }

    // ==================== CP-003: Rechazo por deuda vencida ====================

    @Test
    @DisplayName("CP-003: Rechazo de emisión de tarjeta de crédito por deuda vencida")
    void shouldRejectCreditCardWhenCustomerHasOverdueDebt() {
        // Arrange
        String customerId = "CLI-2001";
        CustomerProfileView profile = CustomerProfileView.builder()
                .customerId(customerId)
                .status("ACTIVE")
                .build();
        CustomerRiskView risk = CustomerRiskView.builder()
                .customerId(customerId)
                .hasOverdueDebt(true)
                .build();

        when(customerProfileViewPort.findByCustomerId(customerId)).thenReturn(Maybe.just(profile));
        when(customerRiskViewPort.findByCustomerId(customerId)).thenReturn(Maybe.just(risk));

        // Act
        TestObserver<Card> testObserver = issueCardService
                .execute(customerId, "CREDIT", "PERSONAL", new BigDecimal("3000"), null)
                .test();

        // Assert
        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("deuda vencida"));
        testObserver.assertNotComplete();
    }

    // ==================== Casos adicionales de error ====================

    @Test
    @DisplayName("Rechazo cuando el cliente no existe")
    void shouldRejectWhenCustomerNotFound() {
        when(customerProfileViewPort.findByCustomerId("CLI-NOT-FOUND")).thenReturn(Maybe.empty());

        TestObserver<Card> testObserver = issueCardService
                .execute("CLI-NOT-FOUND", "CREDIT", "PERSONAL", new BigDecimal("5000"), null)
                .test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("cliente no existe"));
    }

    @Test
    @DisplayName("Rechazo cuando el cliente no está activo")
    void shouldRejectWhenCustomerNotActive() {
        CustomerProfileView profile = CustomerProfileView.builder()
                .customerId("CLI-1001")
                .status("INACTIVE")
                .build();
        when(customerProfileViewPort.findByCustomerId("CLI-1001")).thenReturn(Maybe.just(profile));

        TestObserver<Card> testObserver = issueCardService
                .execute("CLI-1001", "CREDIT", "PERSONAL", new BigDecimal("5000"), null)
                .test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("estado activo"));
    }

    @Test
    @DisplayName("Rechazo cuando ya existe tarjeta de crédito personal")
    void shouldRejectWhenPersonalCreditCardAlreadyExists() {
        CustomerProfileView profile = CustomerProfileView.builder()
                .customerId("CLI-1001")
                .status("ACTIVE")
                .build();
        when(customerProfileViewPort.findByCustomerId("CLI-1001")).thenReturn(Maybe.just(profile));
        when(customerRiskViewPort.findByCustomerId("CLI-1001")).thenReturn(Maybe.empty());
        when(cardPersistencePort.existsByCustomerIdAndTypeAndSubtypeAndStatus(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Single.just(true));

        TestObserver<Card> testObserver = issueCardService
                .execute("CLI-1001", "CREDIT", "PERSONAL", new BigDecimal("5000"), null)
                .test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("tarjeta de crédito personal"));
    }

    @Test
    @DisplayName("Rechazo cuando la tarjeta de débito no tiene cuenta vinculada")
    void shouldRejectDebitCardWithoutLinkedAccount() {
        TestObserver<Card> testObserver = issueCardService
                .execute("CLI-1001", "DEBIT", null, null, null)
                .test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("cuenta vinculada"));
    }

    @Test
    @DisplayName("Rechazo cuando el tipo de tarjeta es inválido")
    void shouldRejectInvalidCardType() {
        TestObserver<Card> testObserver = issueCardService
                .execute("CLI-1001", "INVALID", null, new BigDecimal("5000"), null)
                .test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("Tipo de tarjeta no válido"));
    }

    @Test
    @DisplayName("Rechazo cuando ya existe tarjeta de débito para la cuenta")
    void shouldRejectWhenDebitCardAlreadyExistsForAccount() {
        CustomerProfileView profile = CustomerProfileView.builder()
                .customerId("CLI-1001")
                .status("ACTIVE")
                .build();
        when(customerProfileViewPort.findByCustomerId("CLI-1001")).thenReturn(Maybe.just(profile));
        when(customerRiskViewPort.findByCustomerId("CLI-1001")).thenReturn(Maybe.empty());
        when(cardPersistencePort.existsByLinkedAccountIdAndType("ACC-5001", "DEBIT"))
                .thenReturn(Single.just(true));

        TestObserver<Card> testObserver = issueCardService
                .execute("CLI-1001", "DEBIT", null, null, "ACC-5001")
                .test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertError(BusinessException.class);
        testObserver.assertError(e -> e.getMessage().contains("tarjeta de débito para esta cuenta"));
    }

    @Test
    @DisplayName("Tarjeta de crédito corporativa permite emisión cuando no existe duplicidad")
    void shouldAllowCorporateCreditCardWhenNoDuplicate() {
        CustomerProfileView profile = CustomerProfileView.builder()
                .customerId("CLI-1001")
                .status("ACTIVE")
                .build();
        when(customerProfileViewPort.findByCustomerId("CLI-1001")).thenReturn(Maybe.just(profile));
        when(customerRiskViewPort.findByCustomerId("CLI-1001")).thenReturn(Maybe.empty());
        when(cardPersistencePort.existsByCustomerIdAndTypeAndSubtypeAndStatus(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Single.just(false));
        when(cardPersistencePort.save(any(Card.class))).thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

        TestObserver<Card> testObserver = issueCardService
                .execute("CLI-1001", "CREDIT", "CORPORATE", new BigDecimal("10000"), null)
                .test();

        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }
}
