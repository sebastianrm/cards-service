package com.yanki.cards.infrastructure.persistence.adapter;

import com.yanki.cards.domain.model.CustomerRiskView;
import com.yanki.cards.domain.port.out.CustomerRiskViewPersistencePort;
import com.yanki.cards.infrastructure.persistence.mapper.CustomerRiskViewDocumentMapper;
import com.yanki.cards.infrastructure.persistence.repository.SpringDataCustomerRiskViewRepository;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava3Adapter;

/**
 * Adaptador de salida que implementa la persistencia de vistas de riesgo de cliente sobre MongoDB.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerRiskViewMongoAdapter implements CustomerRiskViewPersistencePort {

    private final SpringDataCustomerRiskViewRepository repository;

    @Override
    public Maybe<CustomerRiskView> findByCustomerId(String customerId) {
        log.debug("Buscando vista de riesgo de cliente en MongoDB: {}", customerId);
        return RxJava3Adapter.monoToMaybe(
                        repository.findByCustomerId(customerId)
                                .map(CustomerRiskViewDocumentMapper::toDomain))
                .doOnSuccess(risk ->
                        log.debug("Vista de riesgo de cliente {}: {}", customerId,
                                risk != null ? "encontrada" : "no encontrada"))
                .doOnError(e ->
                        log.error("Error al buscar vista de riesgo de cliente {} en MongoDB: {}",
                                customerId, e.getMessage(), e));
    }

    @Override
    public Single<CustomerRiskView> save(CustomerRiskView risk) {
        log.debug("Guardando vista de riesgo de cliente en MongoDB: {}", risk.getCustomerId());
        return RxJava3Adapter.monoToSingle(
                        repository.save(CustomerRiskViewDocumentMapper.toDocument(risk))
                                .map(CustomerRiskViewDocumentMapper::toDomain))
                .doOnSuccess(saved ->
                        log.debug("Vista de riesgo de cliente {} guardada exitosamente",
                                risk.getCustomerId()))
                .doOnError(e ->
                        log.error("Error al guardar vista de riesgo de cliente {} en MongoDB: {}",
                                risk.getCustomerId(), e.getMessage(), e));
    }
}
