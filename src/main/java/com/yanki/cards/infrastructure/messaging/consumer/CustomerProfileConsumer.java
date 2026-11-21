package com.yanki.cards.infrastructure.messaging.consumer;

import com.yanki.cards.domain.event.CustomerRegisteredEvent;
import com.yanki.cards.domain.event.Topics;
import com.yanki.cards.domain.model.ProcessedEvent;
import com.yanki.cards.domain.port.in.UpdateCustomerProfileUseCase;
import com.yanki.cards.domain.port.out.ProcessedEventPersistencePort;
import io.reactivex.rxjava3.core.Completable;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor Kafka idempotente para eventos de registro/actualizacion de cliente.
 *
 * <p>Antes de procesar cada evento, verifica en la coleccion local
 * processed_events si ya fue procesado, garantizando idempotencia
 * conforme a la Regla de Oro de la plataforma Yanki.</p>
 *
 * <p>Actualiza la vista local customer_profile_view para mantener
 * la proyeccion sincronizada con customers-service.</p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerProfileConsumer {

    private final UpdateCustomerProfileUseCase updateCustomerProfileUseCase;
    private final ProcessedEventPersistencePort processedEventPort;

    @KafkaListener(topics = Topics.CUSTOMER_REGISTERED, groupId = "${spring.kafka.consumer.group-id:cards-service}",
            containerFactory = "customerRegisteredKafkaListenerContainerFactory")
    public void onCustomerRegistered(CustomerRegisteredEvent event) {
        processCustomerEvent(Topics.CUSTOMER_REGISTERED, event);
    }

    @KafkaListener(topics = Topics.CUSTOMER_UPDATED, groupId = "${spring.kafka.consumer.group-id:cards-service}",
            containerFactory = "customerRegisteredKafkaListenerContainerFactory")
    public void onCustomerUpdated(CustomerRegisteredEvent event) {
        processCustomerEvent(Topics.CUSTOMER_UPDATED, event);
    }

    private void processCustomerEvent(String topic, CustomerRegisteredEvent event) {
        // Log de diagnostico: clase y campos del evento recibido
        log.info("[KAFKA-CONSUMER] Evento {} recibido - clase: {}", topic,
                event != null ? event.getClass().getName() : "null");
        if (event != null) {
            log.debug("[KAFKA-CONSUMER] CustomerRegisteredEvent campos recibidos -> customerId={}, customerType={}, fullName={}, status={}, occurredAt={}",
                    event.getCustomerId(), event.getCustomerType(), event.getFullName(), event.getStatus(), event.getOccurredAt());
        }

        // Validacion temprana de campos obligatorios antes de verificar idempotencia
        if (event == null || event.getCustomerId() == null || event.getCustomerId().isBlank()) {
            log.error("[KAFKA-CONSUMER] Evento {} rechazado: customerId es null o vacio", topic);
            return;
        }

        String idempotencyKey = topic + ":" + event.getCustomerId() + ":" + event.getOccurredAt();

        log.info("[KAFKA-CONSUMER] Evento {} recibido para cliente {}", topic, event.getCustomerId());

        processedEventPort.existsByTopicAndEntityId(topic, event.getCustomerId())
                .flatMapCompletable(exists -> {
            if (exists) {
                log.info("Evento {} ya procesado para cliente {}, ignorando", topic, event.getCustomerId());
                return Completable.complete();
            }
            return updateCustomerProfileUseCase.execute(event)
                    .andThen(saveProcessedEvent(idempotencyKey, topic, event.getCustomerId(), event.getOccurredAt()));
        }).subscribe(
                () -> log.info("Evento {} procesado para cliente {}", topic, event.getCustomerId()),
                error -> {
                    // Si el error es por evento incompleto, no se marca como procesado
                    if (error.getMessage() != null && error.getMessage().contains("obligatorio")) {
                        log.error("Evento {} rechazado para cliente {}: {} (no se marcare como procesado)",
                                topic, event.getCustomerId(), error.getMessage());
                    } else {
                        log.error("Error procesando {}: {}", topic, error.getMessage());
                    }
                }
        );
    }

    private Completable saveProcessedEvent(String id, String topic, String entityId, LocalDateTime occurredAt) {
        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .id(id)
                .topic(topic)
                .entityId(entityId)
                .occurredAt(occurredAt)
                .processedAt(LocalDateTime.now())
                .build();
        return processedEventPort.save(processedEvent).ignoreElement();
    }
}
