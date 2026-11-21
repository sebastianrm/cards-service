package com.yanki.cards.infrastructure.persistence.adapter;

import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.infrastructure.persistence.document.CardDocument;
import com.yanki.cards.infrastructure.persistence.repository.SpringDataCardRepository;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas de integracion del adaptador MongoDB de tarjetas con mocks de repositorio Spring Data.
 */
class CardMongoAdapterTest {

    @Mock
    private SpringDataCardRepository repository;

    private CardMongoAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new CardMongoAdapter(repository);
    }

    @Test
    @DisplayName("findById - Retorna la tarjeta cuando existe")
    void shouldFindCardById() {
        CardDocument document = CardDocument.builder()
                .id("CARD-001").customerId("CLI-1001").type("CREDIT").status("ACTIVE")
                .cardNumber("4111111111111111").creditLimit(new BigDecimal("10000"))
                .build();
        when(repository.findById("CARD-001")).thenReturn(Mono.just(document));

        TestObserver<Card> testObserver = adapter.findById("CARD-001").test();
        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertThat(testObserver.values()).hasSize(1);
        assertThat(testObserver.values().get(0).getId()).isEqualTo("CARD-001");
        assertThat(testObserver.values().get(0).getType()).isEqualTo(CardType.CREDIT);
    }

    @Test
    @DisplayName("findById - Retorna vacio cuando no existe")
    void shouldReturnEmptyWhenCardNotFound() {
        when(repository.findById("CARD-NOT-FOUND")).thenReturn(Mono.empty());

        TestObserver<Card> testObserver = adapter.findById("CARD-NOT-FOUND").test();
        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertThat(testObserver.values()).isEmpty();
    }

    @Test
    @DisplayName("save - Guarda y retorna la tarjeta")
    void shouldSaveCard() {
        Card card = Card.builder().id("CARD-001").customerId("CLI-1001")
                .type(CardType.CREDIT).status(CardStatus.ACTIVE)
                .cardNumber("4111111111111111").creditLimit(new BigDecimal("10000")).build();
        CardDocument document = CardDocument.builder().id("CARD-001").customerId("CLI-1001")
                .type("CREDIT").status("ACTIVE").cardNumber("4111111111111111")
                .creditLimit(new BigDecimal("10000")).build();
        when(repository.save(any(CardDocument.class))).thenReturn(Mono.just(document));

        TestObserver<Card> testObserver = adapter.save(card).test();
        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertThat(testObserver.values()).hasSize(1);
    }

    @Test
    @DisplayName("findAll - Retorna todas las tarjetas")
    void shouldFindAllCards() {
        CardDocument doc1 = CardDocument.builder().id("CARD-001").type("CREDIT").status("ACTIVE").build();
        CardDocument doc2 = CardDocument.builder().id("CARD-002").type("DEBIT").status("ACTIVE").build();
        when(repository.findAll()).thenReturn(Flux.just(doc1, doc2));

        TestSubscriber<Card> testSubscriber = adapter.findAll().test();
        try { testSubscriber.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertThat(testSubscriber.values()).hasSize(2);
    }

    @Test
    @DisplayName("findByCustomerId - Retorna tarjetas por cliente")
    void shouldFindCardsByCustomerId() {
        CardDocument doc1 = CardDocument.builder().id("CARD-001").customerId("CLI-1001").build();
        CardDocument doc2 = CardDocument.builder().id("CARD-002").customerId("CLI-1001").build();
        when(repository.findByCustomerId("CLI-1001")).thenReturn(Flux.just(doc1, doc2));

        TestSubscriber<Card> testSubscriber = adapter.findByCustomerId("CLI-1001").test();
        try { testSubscriber.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertThat(testSubscriber.values()).hasSize(2);
    }

    @Test
    @DisplayName("existsByCustomerIdAndTypeAndSubtypeAndStatus - Retorna true")
    void shouldReturnTrueWhenCardExists() {
        when(repository.existsByCustomerIdAndTypeAndSubtypeAndStatus(
                "CLI-1001", "CREDIT", "PERSONAL", "ACTIVE")).thenReturn(Mono.just(true));

        TestObserver<Boolean> testObserver = adapter
                .existsByCustomerIdAndTypeAndSubtypeAndStatus("CLI-1001", "CREDIT", "PERSONAL", "ACTIVE").test();
        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertThat(testObserver.values()).contains(true);
    }

    @Test
    @DisplayName("existsByLinkedAccountIdAndType - Retorna true")
    void shouldReturnTrueWhenDebitCardExistsForAccount() {
        when(repository.existsByLinkedAccountIdAndType("ACC-001", "DEBIT")).thenReturn(Mono.just(true));

        TestObserver<Boolean> testObserver = adapter
                .existsByLinkedAccountIdAndType("ACC-001", "DEBIT").test();
        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertThat(testObserver.values()).contains(true);
    }
}
