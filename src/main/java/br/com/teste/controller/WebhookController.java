package br.com.teste.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.teste.dto.VehicleEventRequest;
import br.com.teste.service.VehicleEventService;

@RestController
@RequestMapping("/webhook")
@Tag(name = "Webhook Events", description = "Event processing from parking simulator")
public class WebhookController {
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    private final VehicleEventService vehicleEventService;

    public WebhookController(VehicleEventService vehicleEventService) {
        this.vehicleEventService = vehicleEventService;
    }

    @PostMapping
    @Operation(summary = "Process parking event",
        description = "Handle ENTRY, PARKED, or EXIT events from the parking simulator")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid event parameters",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "Vehicle not found",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
        @ApiResponse(responseCode = "409", description = "Sector is full",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
        @ApiResponse(responseCode = "503", description = "Configuration service unavailable",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<Void> handleEvent(@Valid @RequestBody VehicleEventRequest request) {
        logger.info("📡 WEBHOOK RECEIVED - Event Type: {}, License Plate: {}, Sector: {}, Location: ({}, {})",
            request.eventType(), request.licensePlate(), request.sector(), request.latitude(), request.longitude());

        vehicleEventService.processEvent(request);

        logger.info("✅ WEBHOOK PROCESSED - Event Type: {}, License Plate: {}, Status: Success",
            request.eventType(), request.licensePlate());

        return ResponseEntity.ok().build();
    }
}
