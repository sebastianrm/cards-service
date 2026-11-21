package com.yanki.cards.domain.service;

import com.yanki.cards.domain.event.CustomerRegisteredEvent;
import com.yanki.cards.domain.model.CustomerProfileView;
import com.yanki.cards.domain.port.out.CustomerProfileViewPersistencePort;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del servicio {@code UpdateCustomerProfileServiceImpl}.
 * <p>
 * Valida que el servicio rechace eventos Kafka con campos obligatorios null o vacíos,
 * evitando persistir perfiles incompletos en la base de datos.
 * </p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
class UpdateCustomerProfileServiceImplTest {

    private CustomerProfileViewPersistencePort persistencePort;
    private UpdateCustomerProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        persistencePort = mock(CustomerProfileViewPersistencePort.class);
        service = new UpdateCustomerProfileServiceImpl(persistencePort);
    }

    // ==================== Escenario 1: Evento completo y válido ====================

    @Nested
    @DisplayName("Escenario 1: Evento con todos los campos obligatorios")
    class ValidEvent {

        @Test
        @DisplayName("Debe actualizar el perfil cuando el evento tiene customerId, customerType y status")
        void shouldUpdateProfileWhenEventHasAllRequiredFields() {
            // Arrange
            CustomerRegisteredEvent event = CustomerRegisteredEvent.builder()
                    .customerId("6afa69cb5b2ecd70db08f5e6")
                    .customerType("PERSONAL")
                    .fullName("Juan Perez")
                    .status("ACTIVE")
                    .occurredAt(LocalDateTime.now())
                    .build();

            when(persistencePort.save(any(CustomerProfileView.class)))
                    .thenReturn(Single.just(new CustomerProfileView()));

            // Act
            TestObserver<Void> testObserver = service.execute(event).test();

            // Assert
            testObserver.assertComplete();
            verify(persistencePort).save(any(CustomerProfileView.class));
        }

        @Test
        @DisplayName("Debe actualizar el perfil con customerType CORPORATE y status ACTIVE")
        void shouldUpdateProfileWithCorporateType() {
            // Arrange
            CustomerRegisteredEvent event = CustomerRegisteredEvent.builder()
                    .customerId("6afa69cb5b2ecd70db08f5e6")
                    .customerType("CORPORATE")
                    .status("ACTIVE")
                    .build();

            when(persistencePort.save(any(CustomerProfileView.class)))
                    .thenReturn(Single.just(new CustomerProfileView()));

            // Act
            TestObserver<Void> testObserver = service.execute(event).test();

            // Assert
            testObserver.assertComplete();
            verify(persistencePort).save(any(CustomerProfileView.class));
        }
    }

    // ==================== Escenario 2: Evento con customerId null o vacío ====================

    @Nested
    @DisplayName("Escenario 2: Evento con customerId null o vacío")
    class MissingCustomerId {

        @Test
        @DisplayName("Debe rechazar el evento cuando customerId es null")
        void shouldRejectEventWhenCustomerIdIsNull() {
            // Arrange
            CustomerRegisteredEvent event = CustomerRegisteredEvent.builder()
                    .customerId(null)
                    .customerType("PERSONAL")
                    .status("ACTIVE")
                    .build();

            // Act
            TestObserver<Void> testObserver = service.execute(event).test();

            // Assert
            testObserver.assertError(IllegalArgumentException.class);
            testObserver.assertError(e -> e.getMessage().contains("Campo customerId obligatorio"));
            verify(persistencePort, never()).save(any(CustomerProfileView.class));
        }

        @Test
        @DisplayName("Debe rechazar el evento cuando customerId está vacío")
        void shouldRejectEventWhenCustomerIdIsBlank() {
            // Arrange
            CustomerRegisteredEvent event = CustomerRegisteredEvent.builder()
                    .customerId("   ")
                    .customerType("PERSONAL")
                    .status("ACTIVE")
                    .build();

            // Act
            TestObserver<Void> testObserver = service.execute(event).test();

            // Assert
            testObserver.assertError(IllegalArgumentException.class);
            testObserver.assertError(e -> e.getMessage().contains("Campo customerId obligatorio"));
            verify(persistencePort, never()).save(any(CustomerProfileView.class));
        }
    }

    // ==================== Escenario 3: Evento con customerType null o vacío ====================

    @Nested
    @DisplayName("Escenario 3: Evento con customerType null o vacío")
    class MissingCustomerType {

        @Test
        @DisplayName("Debe rechazar el evento cuando customerType es null")
        void shouldRejectEventWhenCustomerTypeIsNull() {
            // Arrange
            CustomerRegisteredEvent event = CustomerRegisteredEvent.builder()
                    .customerId("6afa69cb5b2ecd70db08f5e6")
                    .customerType(null)
                    .status("ACTIVE")
                    .build();

            // Act
            TestObserver<Void> testObserver = service.execute(event).test();

            // Assert
            testObserver.assertError(IllegalArgumentException.class);
            testObserver.assertError(e -> e.getMessage().contains("Campo customerType obligatorio"));
            verify(persistencePort, never()).save(any(CustomerProfileView.class));
        }

        @Test
        @DisplayName("Debe rechazar el evento cuando customerType está vacío")
        void shouldRejectEventWhenCustomerTypeIsBlank() {
            // Arrange
            CustomerRegisteredEvent event = CustomerRegisteredEvent.builder()
                    .customerId("6afa69cb5b2ecd70db08f5e6")
                    .customerType("")
                    .status("ACTIVE")
                    .build();

            // Act
            TestObserver<Void> testObserver = service.execute(event).test();

            // Assert
            testObserver.assertError(IllegalArgumentException.class);
            testObserver.assertError(e -> e.getMessage().contains("Campo customerType obligatorio"));
            verify(persistencePort, never()).save(any(CustomerProfileView.class));
        }
    }

    // ==================== Escenario 4: Evento con status null o vacío ====================

    @Nested
    @DisplayName("Escenario 4: Evento con status null o vacío")
    class MissingStatus {

        @Test
        @DisplayName("Debe rechazar el evento cuando status es null")
        void shouldRejectEventWhenStatusIsNull() {
            // Arrange — Este es el escenario real que ocurre cuando customers-service
            // publica el evento y cards-service lo deserializa con campos null
            CustomerRegisteredEvent event = CustomerRegisteredEvent.builder()
                    .customerId("6afa69cb5b2ecd70db08f5e6")
                    .customerType("PERSONAL")
                    .status(null)
                    .build();

            // Act
            TestObserver<Void> testObserver = service.execute(event).test();

            // Assert
            testObserver.assertError(IllegalArgumentException.class);
            testObserver.assertError(e -> e.getMessage().contains("Campo status obligatorio"));
            verify(persistencePort, never()).save(any(CustomerProfileView.class));
        }

        @Test
        @DisplayName("Debe rechazar el evento cuando status está vacío")
        void shouldRejectEventWhenStatusIsBlank() {
            // Arrange
            CustomerRegisteredEvent event = CustomerRegisteredEvent.builder()
                    .customerId("6afa69cb5b2ecd70db08f5e6")
                    .customerType("PERSONAL")
                    .status("  ")
                    .build();

            // Act
            TestObserver<Void> testObserver = service.execute(event).test();

            // Assert
            testObserver.assertError(IllegalArgumentException.class);
            testObserver.assertError(e -> e.getMessage().contains("Campo status obligatorio"));
            verify(persistencePort, never()).save(any(CustomerProfileView.class));
        }
    }

    // ==================== Escenario 5: Evento completamente vacío (esquema real de customers-service) ====================

    @Nested
    @DisplayName("Escenario 5: Evento completamente vacío (esquema real de customers-service)")
    class EmptyEvent {

        @Test
        @DisplayName("Debe rechazar el evento cuando todos los campos son null excepto customerId")
        void shouldRejectEventWhenOnlyCustomerIdIsPresent() {
            // Arrange — Simula el evento tal como llega cuando customers-service publica
            // y Jackson deserializa solo customerId (campo coincidente)
            CustomerRegisteredEvent event = CustomerRegisteredEvent.builder()
                    .customerId("6afa69cb5b2ecd70db08f5e6")
                    .customerType(null)
                    .fullName(null)
                    .status(null)
                    .occurredAt(null)
                    .build();

            // Act
            TestObserver<Void> testObserver = service.execute(event).test();

            // Assert — Se rechaza por customerType null (primera validación fallida después de customerId)
            testObserver.assertError(IllegalArgumentException.class);
            testObserver.assertError(e -> e.getMessage().contains("Campo customerType obligatorio"));
            verify(persistencePort, never()).save(any(CustomerProfileView.class));
        }

        @Test
        @DisplayName("Debe rechazar el evento cuando el evento es completamente null")
        void shouldRejectEventWhenEventIsNull() {
            // Act
            TestObserver<Void> testObserver = service.execute(null).test();

            // Assert
            testObserver.assertError(IllegalArgumentException.class);
            testObserver.assertError(e -> e.getMessage().contains("Evento nulo recibido de Kafka"));
            verify(persistencePort, never()).save(any(CustomerProfileView.class));
        }
    }

    // ==================== Escenario 6: Error de persistencia ====================

    @Nested
    @DisplayName("Escenario 6: Error de persistencia")
    class PersistenceError {

        @Test
        @DisplayName("Debe propagar el error cuando la persistencia falla")
        void shouldPropagateErrorWhenPersistenceFails() {
            // Arrange
            CustomerRegisteredEvent event = CustomerRegisteredEvent.builder()
                    .customerId("6afa69cb5b2ecd70db08f5e6")
                    .customerType("PERSONAL")
                    .status("ACTIVE")
                    .build();

            when(persistencePort.save(any(CustomerProfileView.class)))
                    .thenReturn(Single.error(new RuntimeException("Error de conexión a MongoDB")));

            // Act
            TestObserver<Void> testObserver = service.execute(event).test();

            // Assert
            testObserver.assertError(RuntimeException.class);
        }
    }
}
