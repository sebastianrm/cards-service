package com.yanki.cards.infrastructure.persistence.adapter;

import com.yanki.cards.domain.model.Consumption;
import com.yanki.cards.domain.model.ConsumptionStatus;
import com.yanki.cards.infrastructure.persistence.document.ConsumptionDocument;
import com.yanki.cards.infrastructure.persistence.repository.SpringDataConsumptionRepository;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import java.math.BigDecimal;
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
 * Pruebas de integracion del adaptador MongoDB de consumos con mocks de repositorio Spring Data.
 */
class ConsumptionMongoAdapterTest {

    @Mock
    private SpringDataConsumptionRepository repository;

    private ConsumptionMongoAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new ConsumptionMongoAdapter(repository);
    }

    @Test
    @DisplayName("findById - Retorna el consumo cuando existe")
    void shouldFindConsumptionById() {
        ConsumptionDocument document = ConsumptionDocument.builder()
                .id("CONS-001").cardId("CARD-001").amount(new BigDecimal("150.50")).status("AUTHORIZED").build();
        when(repository.findById("CONS-001")).thenReturn(Mono.just(document));

        TestObserver<Consumption> testObserver = adapter.findById("CONS-001").test();
        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertThat(testObserver.values()).hasSize(1);
        assertThat(testObserver.values().get(0).getId()).isEqualTo("CONS-001");
    }

    @Test
    @DisplayName("findByIdAndCardId - Retorna el consumo cuando existe")
    void shouldFindConsumptionByIdAndCardId() {
        ConsumptionDocument document = ConsumptionDocument.builder()
                .id("CONS-001").cardId("CARD-001").amount(new BigDecimal("150.50")).build();
        when(repository.findByIdAndCardId("CONS-001", "CARD-001")).thenReturn(Mono.just(document));

        TestObserver<Consumption> testObserver = adapter.findByIdAndCardId("CONS-001", "CARD-001").test();
        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertThat(testObserver.values()).hasSize(1);
    }

    @Test
    @DisplayName("findByIdAndCardId - Retorna vacio cuando no existe")
    void shouldReturnEmptyWhenConsumptionNotFound() {
        when(repository.findByIdAndCardId("CONS-NOT-FOUND", "CARD-001")).thenReturn(Mono.empty());

        TestObserver<Consumption> testObserver = adapter.findByIdAndCardId("CONS-NOT-FOUND", "CARD-001").test();
        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertThat(testObserver.values()).isEmpty();
    }

    @Test
    @DisplayName("save - Guarda y retorna el consumo")
    void shouldSaveConsumption() {
        Consumption consumption = Consumption.builder().id("CONS-001").cardId("CARD-001")
                .amount(new BigDecimal("150.50")).status(ConsumptionStatus.AUTHORIZED).build();
        ConsumptionDocument document = ConsumptionDocument.builder()
                .id("CONS-001").cardId("CARD-001").amount(new BigDecimal("150.50")).status("AUTHORIZED").build();
        when(repository.save(any(ConsumptionDocument.class))).thenReturn(Mono.just(document));

        TestObserver<Consumption> testObserver = adapter.save(consumption).test();
        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertThat(testObserver.values()).hasSize(1);
    }

    @Test
    @DisplayName("findByCardId - Retorna consumos por tarjeta")
    void shouldFindConsumptionsByCardId() {
        ConsumptionDocument doc1 = ConsumptionDocument.builder().id("CONS-001").cardId("CARD-001").build();
        ConsumptionDocument doc2 = ConsumptionDocument.builder().id("CONS-002").cardId("CARD-001").build();
        when(repository.findByCardId("CARD-001")).thenReturn(Flux.just(doc1, doc2));

        TestSubscriber<Consumption> testSubscriber = adapter.findByCardId("CARD-001").test();
        try { testSubscriber.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertThat(testSubscriber.values()).hasSize(2);
    }

    @Test
    @DisplayName("sumAmountByCardIdAndStatus - Retorna la suma de montos")
    void shouldSumAmountByCardIdAndStatus() {
        when(repository.sumAmountByCardIdAndStatus("CARD-001", "AUTHORIZED"))
                .thenReturn(Mono.just(new BigDecimal("2500")));

        TestObserver<BigDecimal> testObserver = adapter.sumAmountByCardIdAndStatus("CARD-001", "AUTHORIZED").test();
        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertThat(testObserver.values()).hasSize(1);
        assertThat(testObserver.values().get(0)).isEqualTo(new BigDecimal("2500"));
    }

    @Test
    @DisplayName("sumAmountByCardIdAndStatus - Retorna cero cuando no hay consumos")
    void shouldReturnZeroWhenNoConsumptions() {
        when(repository.sumAmountByCardIdAndStatus("CARD-001", "AUTHORIZED")).thenReturn(Mono.empty());

        TestObserver<BigDecimal> testObserver = adapter.sumAmountByCardIdAndStatus("CARD-001", "AUTHORIZED").test();
        try { testObserver.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertThat(testObserver.values()).hasSize(1);
        assertThat(testObserver.values().get(0)).isEqualTo(BigDecimal.ZERO);
    }
}
