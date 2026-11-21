package com.yanki.cards.domain.service;

import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.port.in.FindCardsUseCase;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import io.reactivex.rxjava3.core.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso encargado de consultar tarjetas del sistema.
 * <p>
 * Permite listar todas las tarjetas o filtrar por cliente.
 * Depende exclusivamente del puerto de persistencia de tarjetas.
 * </p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FindCardsServiceImpl implements FindCardsUseCase {

    private final CardPersistencePort cardPersistencePort;

    @Override
    public Flowable<Card> findAll() {
        log.debug("Consultando todas las tarjetas del sistema");
        return cardPersistencePort.findAll()
                .doOnComplete(() -> log.info("Consulta de todas las tarjetas completada"))
                .doOnError(error ->
                        log.error("Error al consultar todas las tarjetas: {}", error.getMessage()));
    }

    @Override
    public Flowable<Card> findByCustomerId(String customerId) {
        log.debug("Consultando tarjetas del cliente: {}", customerId);
        return cardPersistencePort.findByCustomerId(customerId)
                .doOnComplete(() ->
                        log.info("Tarjetas del cliente {} consultadas exitosamente", customerId))
                .doOnError(error ->
                        log.error("Error al consultar tarjetas del cliente {}: {}",
                                customerId, error.getMessage()));
    }
}
