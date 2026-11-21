package com.yanki.cards.domain.service;

import com.yanki.cards.domain.event.CustomerRegisteredEvent;
import com.yanki.cards.domain.model.CustomerProfileView;
import com.yanki.cards.domain.model.CustomerType;
import com.yanki.cards.domain.port.in.UpdateCustomerProfileUseCase;
import com.yanki.cards.domain.port.out.CustomerProfileViewPersistencePort;
import io.reactivex.rxjava3.core.Completable;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso encargado de actualizar la vista local del perfil del cliente
 * a partir de eventos Kafka recibidos de customers-service.
 *
 * <p>Mantiene la proyeccion customer_profile_view sincronizada con el estado
 * del cliente en el servicio origen.</p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateCustomerProfileServiceImpl implements UpdateCustomerProfileUseCase {

    private final CustomerProfileViewPersistencePort customerProfileViewPort;

    @Override
    public Completable execute(CustomerRegisteredEvent event) {
        try {
            validateEvent(event);
        } catch (IllegalArgumentException e) {
            log.error("Evento incompleto rechazado para cliente {}: {}",
                    event != null && event.getCustomerId() != null ? event.getCustomerId() : "DESCONOCIDO", e.getMessage());
            return Completable.error(e);
        }

        log.debug("Actualizando perfil del cliente {} desde evento Kafka - tipo: {}, estado: {}",
                event.getCustomerId(), event.getCustomerType(), event.getStatus());

        try {
            CustomerType parsedType = parseCustomerType(event.getCustomerType());
            if (parsedType == null) {
                log.warn("Tipo de cliente invalido: {} para cliente {} - usando null",
                        event.getCustomerType(), event.getCustomerId());
            }
            CustomerProfileView profile = CustomerProfileView.builder()
                    .customerId(event.getCustomerId())
                    .customerType(parsedType)
                    .status(event.getStatus())
                    .updatedAt(LocalDateTime.now())
                    .build();

            return customerProfileViewPort.save(profile)
                    .ignoreElement()
                    .doOnComplete(() ->
                            log.info("Perfil del cliente {} actualizado exitosamente desde evento Kafka - tipo: {}, estado: {}",
                                    event.getCustomerId(), parsedType, event.getStatus()))
                    .doOnError(error ->
                            log.error("Error al persistir perfil del cliente {}: {}",
                                    event.getCustomerId(), error.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al actualizar perfil del cliente {}: {}",
                    event.getCustomerId(), e.getMessage(), e);
            throw new RuntimeException("Error al actualizar perfil: " + e.getMessage(), e);
        }
    }

    /**
     * Valida que el evento contenga los campos obligatorios para actualizar el perfil del cliente.
     *
     * <p>Si algun campo obligatorio es nulo o vacio, se rechaza el evento para evitar
     * persistir perfiles incompletos en la base de datos.</p>
     *
     * @param event evento recibido de Kafka
     * @throws IllegalArgumentException si algun campo obligatorio falta o esta vacio
     */
    private void validateEvent(CustomerRegisteredEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Evento nulo recibido de Kafka");
        }
        if (event.getCustomerId() == null || event.getCustomerId().isBlank()) {
            throw new IllegalArgumentException("Campo customerId obligatorio: valor es null o vacio");
        }
        if (event.getCustomerType() == null || event.getCustomerType().isBlank()) {
            throw new IllegalArgumentException(
                    "Campo customerType obligatorio para cliente " + event.getCustomerId() + ": valor es null o vacio");
        }
        if (event.getStatus() == null || event.getStatus().isBlank()) {
            throw new IllegalArgumentException(
                    "Campo status obligatorio para cliente " + event.getCustomerId() + ": valor es null o vacio");
        }
    }

    private CustomerType parseCustomerType(String type) {
        if (type == null) {
            log.debug("Tipo de cliente nulo");
            return null;
        }
        try {
            return CustomerType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.debug("Tipo de cliente no reconocido: {}", type);
            return null;
        }
    }
}
