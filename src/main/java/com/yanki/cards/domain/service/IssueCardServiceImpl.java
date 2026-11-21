package com.yanki.cards.domain.service;

import com.yanki.cards.domain.event.CardOpenedEvent;
import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.exception.ErrorCode;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardSubtype;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.model.CustomerProfileView;
import com.yanki.cards.domain.model.CustomerRiskView;
import com.yanki.cards.domain.port.in.IssueCardUseCase;
import com.yanki.cards.domain.port.out.CardCachePort;
import com.yanki.cards.domain.port.out.CardEventPublisherPort;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import com.yanki.cards.domain.port.out.CustomerProfileViewPersistencePort;
import com.yanki.cards.domain.port.out.CustomerRiskViewPersistencePort;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso encargado de emitir nuevas tarjetas de credito o debito.
 * <p>
 * Valida la elegibilidad del cliente, el riesgo de deuda vencida,
 * la duplicidad de tarjetas y calcula los limites aplicables.
 * Depende exclusivamente de puertos de salida, sin acoplamiento a infraestructura.
 * </p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssueCardServiceImpl implements IssueCardUseCase {

    private final CardPersistencePort cardPersistencePort;
    private final CustomerProfileViewPersistencePort customerProfileViewPort;
    private final CustomerRiskViewPersistencePort customerRiskViewPort;
    private final CardEventPublisherPort eventPublisherPort;
    private final CardCachePort cardCachePort;

    @Override
    public Single<Card> execute(
            String customerId, String type, String subtype,
            BigDecimal creditLimit, String linkedAccountId) {

        log.debug("Iniciando emision de tarjeta - cliente: {}, tipo: {}, subtipo: {}, limite: {}, cuenta: {}",
                customerId, type, subtype, creditLimit, linkedAccountId);

        try {
            CardType cardType = parseCardType(type);
            CardSubtype cardSubtype = parseCardSubtype(subtype);

            // Validar tipo de tarjeta
            if (cardType == null) {
                log.warn("Tipo de tarjeta invalido: {} para cliente {}", type, customerId);
                return Single.error(new BusinessException(ErrorCode.INVALID_CARD_TYPE));
            }

            // Validar debito requiere cuenta vinculada
            if (cardType == CardType.DEBIT && (linkedAccountId == null || linkedAccountId.isEmpty())) {
                log.warn("Tarjeta de debito sin cuenta vinculada para cliente {}", customerId);
                return Single.error(new BusinessException(ErrorCode.ACCOUNT_REQUIRED_FOR_DEBIT));
            }

            // Validar credito requiere limite de credito
            if (cardType == CardType.CREDIT && creditLimit == null) {
                log.warn("Tarjeta de credito sin limite de credito para cliente {}", customerId);
                return Single.error(new BusinessException(ErrorCode.CREDIT_LIMIT_REQUIRED));
            }

            // Validar que el limite de credito sea mayor a cero para tarjetas de credito
            if (cardType == CardType.CREDIT && creditLimit.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Limite de credito invalido: {} para cliente {}", creditLimit, customerId);
                return Single.error(new BusinessException(ErrorCode.INVALID_CREDIT_LIMIT));
            }

            log.debug("Tipo de tarjeta validado: {} para cliente {}", cardType.name(), customerId);

            // Validar existencia y estado del cliente
            return customerProfileViewPort.findByCustomerId(customerId)
                    .switchIfEmpty(Single.error(() -> {
                        log.warn("Cliente {} no encontrado en el sistema", customerId);
                        return new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND);
                    }))
                    .flatMap(profile -> {
                        if (!"ACTIVE".equalsIgnoreCase(profile.getStatus())) {
                            log.warn("Cliente {} no esta activo (estado: {}) - rechazando emision de tarjeta",
                                    customerId, profile.getStatus());
                            throw new BusinessException(ErrorCode.CUSTOMER_NOT_ACTIVE);
                        }
                        log.debug("Cliente {} verificado como activo", customerId);
                        return Single.just(profile);
                    })
                    // Validar riesgo deuda vencida
                    .flatMap(profile ->
                            customerRiskViewPort.findByCustomerId(customerId)
                                    .switchIfEmpty(Single.just(CustomerRiskView.builder()
                                            .customerId(customerId)
                                            .hasOverdueDebt(false)
                                            .build()))
                                    .flatMap(risk -> {
                                        if (risk.isHasOverdueDebt()) {
                                            log.warn("Cliente {} tiene deuda vencida - rechazando emision de tarjeta",
                                                    customerId);
                                            throw new BusinessException(ErrorCode.CUSTOMER_HAS_OVERDUE_DEBT);
                                        }
                                        log.debug("Cliente {} sin deuda vencida - riesgo aprobado", customerId);
                                        return Single.just(profile);
                                    })
                    )
                    // Validar duplicidad y construir tarjeta
                    .flatMap(profile ->
                            validateUniqueness(customerId, cardType, cardSubtype, linkedAccountId)
                                    .andThen(Single.just(profile)))
                    .flatMap(profile -> {
                        Card card = Card.builder()
                                .id(UUID.randomUUID().toString())
                                .customerId(customerId)
                                .type(cardType)
                                .subtype(cardSubtype)
                                .cardNumber("SCRAMBLED-" + UUID.randomUUID().toString().substring(0, 16))
                                .status(cardType == CardType.CREDIT ? CardStatus.ISSUED : CardStatus.ACTIVE)
                                .creditLimit(creditLimit != null ? creditLimit : BigDecimal.ZERO)
                                .availableLimit(cardType == CardType.CREDIT
                                        ? (creditLimit != null ? creditLimit : BigDecimal.ZERO)
                                        : BigDecimal.ZERO)
                                .linkedAccountId(linkedAccountId)
                                .issuedAt(LocalDateTime.now())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                        log.info("Construyendo tarjeta {} - tipo: {}, cliente: {}, limite: {}",
                                card.getId(), cardType.name(), customerId, card.getCreditLimit());

                        return cardPersistencePort.save(card)
                                .doOnSuccess(saved -> {
                                    log.info("Tarjeta emitida exitosamente: {} para cliente {} (tipo: {}, limite: {})",
                                            saved.getId(), customerId, cardType.name(), saved.getCreditLimit());
                                    cardCachePort.setCard(saved);
                                    publishCardOpened(saved);
                                })
                                .doOnError(error ->
                                        log.error("Error al persistir tarjeta para cliente {}: {}",
                                                customerId, error.getMessage()));
                    });
        } catch (BusinessException e) {
            log.error("Excepcion de negocio al emitir tarjeta para cliente {}: [{}] {}",
                    customerId, e.getErrorCode().name(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al emitir tarjeta para cliente {}: {}",
                    customerId, e.getMessage(), e);
            throw new RuntimeException("Error al emitir tarjeta: " + e.getMessage(), e);
        }
    }

    private void publishCardOpened(Card card) {
        CardOpenedEvent event = CardOpenedEvent.builder()
                .cardId(card.getId())
                .customerId(card.getCustomerId())
                .cardType(card.getType().name())
                .cardSubtype(card.getSubtype() != null ? card.getSubtype().name() : null)
                .creditLimit(card.getCreditLimit())
                .linkedAccountId(card.getLinkedAccountId())
                .occurredAt(LocalDateTime.now())
                .build();
        eventPublisherPort.publishCardOpened(event).subscribe(
                () -> log.debug("Evento CardOpened publicado para tarjeta {}", card.getId()),
                error -> log.error("Error publicando CardOpened: {}", error.getMessage())
        );
    }

    private Completable validateUniqueness(
            String customerId, CardType type, CardSubtype subtype, String linkedAccountId) {

        if (type == CardType.CREDIT) {
            String subtypeStr = subtype != null ? subtype.name() : "";
            // Verificar tanto ISSUED como BLOCKED: una tarjeta bloqueada sigue existiendo
            // y no se debe permitir crear otra mientras exista una activa (no CLOSED/EXPIRED)
            return cardPersistencePort.existsByCustomerIdAndTypeAndSubtypeAndStatus(
                            customerId, type.name(), subtypeStr, CardStatus.ISSUED.name())
                    .flatMap(iIssued ->
                            cardPersistencePort.existsByCustomerIdAndTypeAndSubtypeAndStatus(
                                    customerId, type.name(), subtypeStr, CardStatus.BLOCKED.name())
                                    .map(iBlocked -> iIssued || iBlocked))
                    .flatMapCompletable(exists -> {
                        if (exists && CardSubtype.PERSONAL.equals(subtype)) {
                            return Completable.error(new BusinessException(ErrorCode.PERSONAL_CREDIT_CARD_ALREADY_EXISTS));
                        }
                        return Completable.complete();
                    });
        } else if (type == CardType.DEBIT) {
            return cardPersistencePort.existsByLinkedAccountIdAndType(linkedAccountId, type.name())
                    .flatMapCompletable(exists -> {
                        if (exists) {
                            return Completable.error(new BusinessException(ErrorCode.DEBIT_CARD_ALREADY_EXISTS_FOR_ACCOUNT));
                        }
                        return Completable.complete();
                    });
        }
        return Completable.error(new BusinessException(ErrorCode.INVALID_CARD_TYPE));
    }

    private CardType parseCardType(String type) {
        if (type == null) {
            return null;
        }
        try {
            return CardType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private CardSubtype parseCardSubtype(String subtype) {
        if (subtype == null) {
            return null;
        }
        try {
            return CardSubtype.valueOf(subtype.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
