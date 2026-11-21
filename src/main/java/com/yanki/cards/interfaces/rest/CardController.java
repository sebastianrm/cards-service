package com.yanki.cards.interfaces.rest;

import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.Consumption;
import com.yanki.cards.domain.port.in.BlockCardUseCase;
import com.yanki.cards.domain.port.in.CloseCardUseCase;
import com.yanki.cards.domain.port.in.FindCardsUseCase;
import com.yanki.cards.domain.port.in.GetCardLimitUseCase;
import com.yanki.cards.domain.port.in.GetCardUseCase;
import com.yanki.cards.domain.port.in.GetConsumptionDetailUseCase;
import com.yanki.cards.domain.port.in.GetConsumptionsUseCase;
import com.yanki.cards.domain.port.in.GetStatementUseCase;
import com.yanki.cards.domain.port.in.IssueCardUseCase;
import com.yanki.cards.domain.port.in.RegisterConsumptionUseCase;
import com.yanki.cards.domain.port.in.RegisterPaymentUseCase;
import com.yanki.cards.domain.port.in.RefundConsumptionUseCase;
import com.yanki.cards.domain.port.in.UnblockCardUseCase;
import com.yanki.cards.interfaces.rest.dto.ApiError;
import com.yanki.cards.interfaces.rest.dto.BlockCardRequest;
import com.yanki.cards.interfaces.rest.dto.CardResponse;
import com.yanki.cards.interfaces.rest.dto.ConsumptionResponse;
import com.yanki.cards.interfaces.rest.dto.CreateCardRequest;
import com.yanki.cards.interfaces.rest.dto.CreateConsumptionRequest;
import com.yanki.cards.interfaces.rest.dto.CreatePaymentRequest;
import com.yanki.cards.interfaces.rest.dto.LimitResponse;
import com.yanki.cards.interfaces.rest.dto.PaymentResponse;
import com.yanki.cards.interfaces.rest.dto.StatementResponse;
import com.yanki.cards.interfaces.rest.mapper.CardRestMapper;
import com.yanki.cards.interfaces.rest.mapper.ConsumptionRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST reactivo para operaciones sobre tarjetas.
 * <p>
 * Recibe peticiones HTTP, invoca los puertos de entrada del dominio
 * y convierte los tipos reactivos de RxJava 3 a Project Reactor.
 * </p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
@Slf4j
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Gestión de tarjetas de crédito y débito")
@Tag(name = "Consumptions", description = "Registro y consulta de consumos de tarjetas")
@Tag(name = "Payments", description = "Pagos y reembolsos de tarjetas de crédito")
public class CardController {

    private final IssueCardUseCase issueCardUseCase;
    private final FindCardsUseCase findCardsUseCase;
    private final GetCardUseCase getCardUseCase;
    private final GetCardLimitUseCase getCardLimitUseCase;
    private final GetConsumptionsUseCase getConsumptionsUseCase;
    private final GetConsumptionDetailUseCase getConsumptionDetailUseCase;
    private final RegisterConsumptionUseCase registerConsumptionUseCase;
    private final RefundConsumptionUseCase refundConsumptionUseCase;
    private final GetStatementUseCase getStatementUseCase;
    private final RegisterPaymentUseCase registerPaymentUseCase;
    private final BlockCardUseCase blockCardUseCase;
    private final UnblockCardUseCase unblockCardUseCase;
    private final CloseCardUseCase closeCardUseCase;

    // ==================== Tarjetas ====================

    /**
     * Lista todas las tarjetas registradas en el sistema.
     *
     * @return flujo de tarjetas
     */
    @GetMapping
    @Operation(
            summary = "Lista todas las tarjetas",
            description = "Devuelve la colección completa de tarjetas registradas en el sistema, "
                    + "incluyendo tarjetas de crédito y débito en todos los estados.",
            tags = {"Cards"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de tarjetas obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public Flux<CardResponse> getAllCards() {
        log.debug("GET /api/cards - Listando todas las tarjetas");
        return Flux.from(
                findCardsUseCase.findAll().map(CardRestMapper::toResponse))
                .doOnComplete(() -> log.debug("GET /api/cards - Lista de tarjetas completada"))
                .doOnError(error ->
                        log.error("GET /api/cards - Error al listar tarjetas: {}", error.getMessage()));
    }

    /**
     * Emite una nueva tarjeta de credito o debito para el cliente indicado.
     *
     * @param request datos de la solicitud de emision
     * @return tarjeta emitida
     */
    @PostMapping
    @Operation(
            summary = "Emite una nueva tarjeta",
            description = "Crea una nueva tarjeta de crédito o débito para el cliente especificado. "
                    + "Para tarjetas de crédito, se valida que el cliente no tenga deuda vencida y que no exista "
                    + "ya una tarjeta de crédito personal. Para tarjetas de débito, se requiere una cuenta bancaria vinculada. "
                    + "La tarjeta se crea con estado ACTIVE y fecha de vencimiento calculada automáticamente.",
            tags = {"Cards"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarjeta emitida exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida: tipo de tarjeta no válido o límite de crédito inválido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "El cliente tiene deuda vencida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "El cliente no existe en el sistema",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe una tarjeta de este tipo para el cliente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CardResponse> issueCard(
            @Parameter(description = "Datos de la solicitud de emisión de tarjeta", required = true)
            @Valid @RequestBody CreateCardRequest request) {
        log.info("POST /api/cards - Emitiendo tarjeta para cliente: {}, tipo: {}, subtipo: {}",
                request.customerId(), request.type(), request.subtype());
        return RxJava3Adapter.singleToMono(
                issueCardUseCase.execute(
                        request.customerId(),
                        request.type(),
                        request.subtype(),
                        request.creditLimit(),
                        request.linkedAccountId())
                        .map(CardRestMapper::toResponse))
                .doOnSuccess(response ->
                        log.info("POST /api/cards - Tarjeta emitida: {}", response.id()))
                .doOnError(error ->
                        log.error("POST /api/cards - Error al emitir tarjeta para cliente {}: {}",
                                request.customerId(), error.getMessage()));
    }

    /**
     * Obtiene el detalle de una tarjeta por su identificador.
     *
     * @param id identificador de la tarjeta
     * @return tarjeta encontrada
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtiene el detalle de una tarjeta",
            description = "Devuelve toda la información de una tarjeta específica por su identificador, "
                    + "incluyendo estado, límites, fechas y cuenta vinculada.",
            tags = {"Cards"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle de la tarjeta obtenido exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "404", description = "La tarjeta no existe",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public Mono<CardResponse> getCard(
            @Parameter(description = "Identificador único de la tarjeta", example = "card-123", required = true)
            @PathVariable("id") String id) {
        log.debug("GET /api/cards/{} - Consultando tarjeta", id);
        return RxJava3Adapter.singleToMono(getCardUseCase.execute(id).map(CardRestMapper::toResponse))
                .doOnSuccess(response -> log.debug("GET /api/cards/{} - Tarjeta encontrada", id))
                .doOnError(error ->
                        log.error("GET /api/cards/{} - Error: {}", id, error.getMessage()));
    }

    /**
     * Consulta el límite disponible de una tarjeta de credito.
     *
     * @param id identificador de la tarjeta
     * @return limite disponible
     */
    @GetMapping("/{id}/limit")
    @Operation(
            summary = "Consulta el límite disponible",
            description = "Devuelve el límite de crédito total, el monto consumos autorizados y el límite disponible "
                    + "actual de una tarjeta de crédito. No aplica para tarjetas de débito.",
            tags = {"Cards"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Límite obtenido exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LimitResponse.class))),
            @ApiResponse(responseCode = "404", description = "La tarjeta no existe",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "La consulta de límite no aplica para este tipo de tarjeta",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public Mono<LimitResponse> getCardLimit(
            @Parameter(description = "Identificador único de la tarjeta de crédito", example = "card-123", required = true)
            @PathVariable("id") String id) {
        return RxJava3Adapter.singleToMono(
                getCardLimitUseCase.execute(id)
                        .map(availableLimit ->
                                new LimitResponse(id, BigDecimal.ZERO, availableLimit, BigDecimal.ZERO)));
    }

    /**
     * Bloquea una tarjeta activa.
     *
     * @param id identificador de la tarjeta
     * @param request motivo del bloqueo
     * @return tarjeta actualizada
     */
    @PutMapping("/{id}/block")
    @Operation(
            summary = "Bloquea una tarjeta",
            description = "Bloquea una tarjeta activa impidiendo su uso para nuevas transacciones. "
                    + "Se puede especificar un motivo opcional para el bloqueo. "
                    + "La tarjeta debe estar en estado ACTIVE para poder ser bloqueada.",
            tags = {"Cards"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarjeta bloqueada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "403", description = "La tarjeta no está activa",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "La tarjeta no existe",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "La tarjeta ya está bloqueada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public Mono<CardResponse> blockCard(
            @Parameter(description = "Identificador único de la tarjeta", example = "card-123", required = true)
            @PathVariable("id") String id,
            @Parameter(description = "Motivo opcional del bloqueo", required = false)
            @Valid @RequestBody BlockCardRequest request) {
        log.info("PUT /api/cards/{}/block - Bloqueando tarjeta, motivo: {}", id, request.reason());
        return RxJava3Adapter.singleToMono(blockCardUseCase.execute(id, request.reason()).map(CardRestMapper::toResponse))
                .doOnSuccess(response -> log.info("PUT /api/cards/{}/block - Tarjeta bloqueada", id))
                .doOnError(error ->
                        log.error("PUT /api/cards/{}/block - Error: {}", id, error.getMessage()));
    }

    /**
     * Desbloquea una tarjeta bloqueada.
     *
     * @param id identificador de la tarjeta
     * @return tarjeta actualizada
     */
    @PutMapping("/{id}/unblock")
    @Operation(
            summary = "Desbloquea una tarjeta",
            description = "Restaura el estado ACTIVE de una tarjeta previamente bloqueada, "
                    + "permitiendo nuevamente su uso para transacciones. "
                    + "La tarjeta debe estar en estado BLOCKED para poder ser desbloqueada.",
            tags = {"Cards"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarjeta desbloqueada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "404", description = "La tarjeta no existe",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "La tarjeta no está bloqueada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public Mono<CardResponse> unblockCard(
            @Parameter(description = "Identificador único de la tarjeta", example = "card-123", required = true)
            @PathVariable("id") String id) {
        log.info("PUT /api/cards/{}/unblock - Desbloqueando tarjeta", id);
        return RxJava3Adapter.singleToMono(unblockCardUseCase.execute(id).map(CardRestMapper::toResponse))
                .doOnSuccess(response -> log.info("PUT /api/cards/{}/unblock - Tarjeta desbloqueada", id))
                .doOnError(error ->
                        log.error("PUT /api/cards/{}/unblock - Error: {}", id, error.getMessage()));
    }

    /**
     * Cierra una tarjeta de credito o debito.
     *
     * @param id identificador de la tarjeta
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Cierra una tarjeta",
            description = "Cierra permanentemente una tarjeta de crédito o débito. "
                    + "Para tarjetas de crédito, se valida que no haya saldo pendiente. "
                    + "La tarjeta debe estar en estado ACTIVE o BLOCKED para poder ser cerrada. "
                    + "Una vez cerrada, la tarjeta no puede ser reactivada.",
            tags = {"Cards"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tarjeta cerrada exitosamente (sin contenido)"),
            @ApiResponse(responseCode = "403", description = "La tarjeta no está activa",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "La tarjeta no existe",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "La tarjeta ya está cerrada o tiene saldo pendiente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> closeCard(
            @Parameter(description = "Identificador único de la tarjeta", example = "card-123", required = true)
            @PathVariable("id") String id) {
        log.info("DELETE /api/cards/{} - Cerrando tarjeta", id);
        return RxJava3Adapter.completableToMono(
                closeCardUseCase.execute(id)
                        .doOnComplete(() -> log.info("DELETE /api/cards/{} - Tarjeta cerrada", id))
                        .doOnError(error ->
                                log.error("DELETE /api/cards/{} - Error: {}", id, error.getMessage())));
    }

    // ==================== Consumos ====================

    /**
     * Lista los consumos de una tarjeta.
     *
     * @param id identificador de la tarjeta
     * @return flujo de consumos
     */
    @GetMapping("/{id}/consumptions")
    @Operation(
            summary = "Lista los consumos de una tarjeta",
            description = "Devuelve la colección completa de consumos registrados para una tarjeta específica, "
                    + "incluyendo consumos autorizados, liquidados y reembolsados.",
            tags = {"Consumptions"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de consumos obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConsumptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "La tarjeta no existe",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public Flux<ConsumptionResponse> getConsumptions(
            @Parameter(description = "Identificador único de la tarjeta", example = "card-123", required = true)
            @PathVariable("id") String id) {
        log.debug("GET /api/cards/{}/consumptions - Listando consumos", id);
        return Flux.from(getConsumptionsUseCase.execute(id).map(ConsumptionRestMapper::toResponse))
                .doOnComplete(() -> log.debug("GET /api/cards/{}/consumptions - Lista completada", id))
                .doOnError(error ->
                        log.error("GET /api/cards/{}/consumptions - Error: {}", id, error.getMessage()));
    }

    /**
     * Obtiene el detalle de un consumo específico.
     *
     * @param id identificador de la tarjeta
     * @param consumptionId identificador del consumo
     * @return consumo encontrado
     */
    @GetMapping("/{id}/consumptions/{consumptionId}")
    @Operation(
            summary = "Obtiene el detalle de un consumo",
            description = "Devuelve la información completa de un consumo específico identificado por su ID "
                    + "y la tarjeta asociada, incluyendo monto, comercio, estado y fechas.",
            tags = {"Consumptions"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle del consumo obtenido exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConsumptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "El consumo o la tarjeta no existen",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public Mono<ConsumptionResponse> getConsumptionDetail(
            @Parameter(description = "Identificador único de la tarjeta", example = "card-123", required = true)
            @PathVariable("id") String id,
            @Parameter(description = "Identificador único del consumo", example = "cons-456", required = true)
            @PathVariable("consumptionId") String consumptionId) {
        return RxJava3Adapter.singleToMono(
                getConsumptionDetailUseCase.execute(id, consumptionId)
                        .map(ConsumptionRestMapper::toResponse));
    }

    /**
     * Registra un consumo en la tarjeta.
     *
     * @param id identificador de la tarjeta
     * @param request datos del consumo
     * @return consumo registrado
     */
    @PostMapping("/{id}/consumptions")
    @Operation(
            summary = "Registra un consumo",
            description = "Registra un nuevo consumo en la tarjeta indicada. "
                    + "Para tarjetas de crédito, valida el límite disponible y autoriza el consumo si hay saldo suficiente. "
                    + "Para tarjetas de débito, inicia el flujo asíncrono de pago desde la cuenta vinculada. "
                    + "La tarjeta debe estar en estado ACTIVE.",
            tags = {"Consumptions"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Consumo registrado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConsumptionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Monto inválido o límite de crédito insuficiente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "La tarjeta está bloqueada o no está activa",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "La tarjeta no existe",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ConsumptionResponse> registerConsumption(
            @Parameter(description = "Identificador único de la tarjeta", example = "card-123", required = true)
            @PathVariable("id") String id,
            @Parameter(description = "Datos del consumo a registrar", required = true)
            @Valid @RequestBody CreateConsumptionRequest request) {
        log.info("POST /api/cards/{}/consumptions - Registrando consumo, monto: {}, comercio: {}",
                id, request.amount(), request.merchantId());
        return RxJava3Adapter.singleToMono(
                registerConsumptionUseCase.execute(
                        id, request.amount(), request.merchantId(), request.description())
                        .map(ConsumptionRestMapper::toResponse))
                .doOnSuccess(response -> log.info("POST /api/cards/{}/consumptions - Consumo registrado: {}",
                        id, response.id()))
                .doOnError(error ->
                        log.error("POST /api/cards/{}/consumptions - Error: {}", id, error.getMessage()));
    }

    /**
     * Procesa el reverso (refund) de un consumo.
     *
     * @param id identificador de la tarjeta
     * @param consumptionId identificador del consumo
     * @return consumo actualizado
     */
    @PostMapping("/{id}/consumptions/{consumptionId}/refund")
    @Operation(
            summary = "Procesa el reverso de un consumo",
            description = "Procesa el reembolso (refund) de un consumo previamente autorizado o liquidado. "
                    + "El consumo debe estar en estado AUTHORIZED o SETTLED para poder ser reembolsado. "
                    + "Al procesar el reembolso, el monto se resta de los consumos autorizados y se restaura el límite disponible.",
            tags = {"Consumptions"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reembolso procesado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConsumptionResponse.class))),
            @ApiResponse(responseCode = "404", description = "El consumo o la tarjeta no existen",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "El consumo ya fue reembolsado o no es reembolsable en su estado actual",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public Mono<ConsumptionResponse> refundConsumption(
            @Parameter(description = "Identificador único de la tarjeta", example = "card-123", required = true)
            @PathVariable("id") String id,
            @Parameter(description = "Identificador único del consumo a reembolsar", example = "cons-456", required = true)
            @PathVariable("consumptionId") String consumptionId) {
        return RxJava3Adapter.singleToMono(
                refundConsumptionUseCase.execute(id, consumptionId)
                        .map(ConsumptionRestMapper::toResponse));
    }

    /**
     * Obtiene el estado de cuenta mensual de una tarjeta de credito.
     *
     * @param id identificador de la tarjeta
     * @param month mes del estado de cuenta (YYYY-MM)
     * @return estado de cuenta
     */
    @GetMapping("/{id}/statement")
    @Operation(
            summary = "Estado de cuenta mensual",
            description = "Devuelve el estado de cuenta mensual de una tarjeta de crédito, "
                    + "incluyendo la lista de consumos del periodo, el monto total y el límite disponible. "
                    + "Si no se especifica el mes, se devuelve el estado de cuenta del mes actual.",
            tags = {"Consumptions"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de cuenta obtenido exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StatementResponse.class))),
            @ApiResponse(responseCode = "404", description = "La tarjeta no existe o no se encontró estado de cuenta para el periodo indicado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    public Mono<StatementResponse> getStatement(
            @Parameter(description = "Identificador único de la tarjeta", example = "card-123", required = true)
            @PathVariable("id") String id,
            @Parameter(description = "Mes del estado de cuenta en formato YYYY-MM (ej: 2026-11). Si no se especifica, se usa el mes actual", example = "2026-11")
            @RequestParam(value = "month", defaultValue = "") String month) {
        return Flux.from(getStatementUseCase.execute(id, month))
                .map(result -> {
                    List<ConsumptionResponse> consumptions = result.consumptions().stream()
                            .map(ConsumptionRestMapper::toResponse).toList();
                    return new StatementResponse(
                            result.cardId(),
                            result.month(),
                            consumptions,
                            result.totalAmount(),
                            result.availableLimit()
                    );
                })
                .defaultIfEmpty(new StatementResponse(id, null, List.of(), BigDecimal.ZERO, BigDecimal.ZERO))
                .next();
    }

    // ==================== Pagos ====================

    /**
     * Registra un pago en una tarjeta de credito.
     *
     * @param id identificador de la tarjeta
     * @param request datos del pago
     * @return respuesta del pago
     */
    @PostMapping("/{id}/payments")
    @Operation(
            summary = "Registra un pago de tarjeta de crédito",
            description = "Registra un pago en una tarjeta de crédito, reduciendo el saldo pendiente "
                    + "y aumentando el límite disponible. El monto debe ser mayor a cero. "
                    + "La tarjeta debe estar en estado ACTIVE.",
            tags = {"Payments"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago registrado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Monto inválido o pago rechazado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "La tarjeta no está activa",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "La tarjeta no existe",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PaymentResponse> registerPayment(
            @Parameter(description = "Identificador único de la tarjeta", example = "card-123", required = true)
            @PathVariable("id") String id,
            @Parameter(description = "Datos del pago a registrar", required = true)
            @Valid @RequestBody CreatePaymentRequest request) {
        log.info("POST /api/cards/{}/payments - Registrando pago, monto: {}", id, request.amount());
        return RxJava3Adapter.singleToMono(
                registerPaymentUseCase.execute(id, request.amount())
                        .map(card -> {
                            log.info("POST /api/cards/{}/payments - Pago registrado, nuevo limite: {}",
                                    id, card.getAvailableLimit());
                            return new PaymentResponse(
                                    card.getId(),
                                    request.amount(),
                                    card.getAvailableLimit(),
                                    LocalDateTime.now()
                            );
                        }))
                .doOnError(error ->
                        log.error("POST /api/cards/{}/payments - Error: {}", id, error.getMessage()));
    }
}
