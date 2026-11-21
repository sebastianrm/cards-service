package com.yanki.cards.infrastructure.cache.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.port.out.CardCachePort;
import com.yanki.cards.infrastructure.config.CacheProperties;
import io.reactivex.rxjava3.core.Maybe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida que implementa la caché de tarjetas sobre Redis Reactivo.
 *
 * <p>Implementa {@link CardCachePort} usando {@link ReactiveStringRedisTemplate} para
 * almacenar y recuperar tarjetas y limites en caché. Las claves siguen el patrón
 * {@code yanki:cards:card:<cardId>} y {@code yanki:cards:limit:<cardId>}.</p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardRedisCacheAdapter implements CardCachePort {

    private static final String CARD_KEY_SUFFIX = "card:";
    private static final String LIMIT_KEY_SUFFIX = "limit:";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final CacheProperties cacheProperties;
    private final ObjectMapper objectMapper;

    @Override
    public Maybe<Card> getCard(String cardId) {
        String cacheKey = cacheProperties.getKeyPrefix() + CARD_KEY_SUFFIX + cardId;
        return Maybe.fromPublisher(
                redisTemplate.opsForValue().get(cacheKey)
        ).map(json -> {
            try {
                return objectMapper.readValue(json, Card.class);
            } catch (JsonProcessingException e) {
                log.warn("Error deserializando tarjeta desde caché: {}", e.getMessage());
                return null;
            }
        });
    }

    @Override
    public void setCard(Card card) {
        String cacheKey = cacheProperties.getKeyPrefix() + CARD_KEY_SUFFIX + card.getId();
        try {
            String json = objectMapper.writeValueAsString(card);
            redisTemplate.opsForValue()
                    .set(cacheKey, json, cacheProperties.getCardTtl())
                    .subscribe();
        } catch (JsonProcessingException e) {
            log.warn("Error serializando tarjeta para caché: {}", e.getMessage());
        }
    }

    @Override
    public void invalidateCard(String cardId) {
        String cacheKey = cacheProperties.getKeyPrefix() + CARD_KEY_SUFFIX + cardId;
        redisTemplate.delete(cacheKey).subscribe();
    }

    @Override
    public Maybe<String> getCardLimit(String cardId) {
        String cacheKey = cacheProperties.getKeyPrefix() + LIMIT_KEY_SUFFIX + cardId;
        return Maybe.fromPublisher(
                redisTemplate.opsForValue().get(cacheKey)
        );
    }

    @Override
    public void setCardLimit(String cardId, String limitJson) {
        String cacheKey = cacheProperties.getKeyPrefix() + LIMIT_KEY_SUFFIX + cardId;
        redisTemplate.opsForValue()
                .set(cacheKey, limitJson, cacheProperties.getLimitTtl())
                .subscribe();
    }

    @Override
    public void invalidateCardLimit(String cardId) {
        String cacheKey = cacheProperties.getKeyPrefix() + LIMIT_KEY_SUFFIX + cardId;
        redisTemplate.delete(cacheKey).subscribe();
    }
}
