package com.yanki.cards.infrastructure.persistence.adapter;

import com.yanki.cards.domain.model.CustomerProfileView;
import com.yanki.cards.domain.port.out.CustomerProfileViewPersistencePort;
import com.yanki.cards.infrastructure.persistence.mapper.CustomerProfileViewDocumentMapper;
import com.yanki.cards.infrastructure.persistence.repository.SpringDataCustomerProfileViewRepository;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava3Adapter;

/**
 * Adaptador de salida que implementa la persistencia de vistas de perfil de cliente sobre MongoDB.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerProfileViewMongoAdapter implements CustomerProfileViewPersistencePort {

    private final SpringDataCustomerProfileViewRepository repository;

    @Override
    public Maybe<CustomerProfileView> findByCustomerId(String customerId) {
        log.debug("Buscando perfil de cliente en MongoDB: {}", customerId);
        return RxJava3Adapter.monoToMaybe(
                        repository.findByCustomerId(customerId)
                                .map(CustomerProfileViewDocumentMapper::toDomain))
                .doOnSuccess(profile ->
                        log.debug("Perfil de cliente {}: {}", customerId,
                                profile != null ? "encontrado" : "no encontrado"))
                .doOnError(e ->
                        log.error("Error al buscar perfil de cliente {} en MongoDB: {}",
                                customerId, e.getMessage(), e));
    }

    @Override
    public Single<CustomerProfileView> save(CustomerProfileView profile) {
        log.debug("Guardando perfil de cliente en MongoDB: {}", profile.getCustomerId());
        return RxJava3Adapter.monoToSingle(
                        repository.save(CustomerProfileViewDocumentMapper.toDocument(profile))
                                .map(CustomerProfileViewDocumentMapper::toDomain))
                .doOnSuccess(saved ->
                        log.debug("Perfil de cliente {} guardado exitosamente en MongoDB",
                                profile.getCustomerId()))
                .doOnError(e ->
                        log.error("Error al guardar perfil de cliente {} en MongoDB: {}",
                                profile.getCustomerId(), e.getMessage(), e));
    }
}
