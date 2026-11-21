package com.yanki.cards.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para la solicitud de bloqueo de tarjeta.
 */
@Schema(
        name = "BlockCardRequest",
        description = "Solicitud para bloquear una tarjeta activa con un motivo opcional",
        example = "{\"reason\":\"Tarjeta reportada como perdida\"}"
)
public record BlockCardRequest(
        @Schema(
                description = "Motivo del bloqueo de la tarjeta (opcional)",
                example = "Tarjeta reportada como perdida",
                nullable = true
        )
        String reason
) {}
