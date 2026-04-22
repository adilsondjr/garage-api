package br.com.teste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Schema(description = "Vehicle parking event from simulator")
public record VehicleEventRequest(
    @JsonProperty("event_type")
    @Schema(description = "Event type (ENTRY, PARKED, or EXIT)", example = "ENTRY")
    @NotBlank(message = "Event type cannot be blank")
    @Pattern(regexp = "ENTRY|PARKED|EXIT", message = "Event type must be ENTRY, PARKED, or EXIT")
    String eventType,

    @JsonProperty("license_plate")
    @Schema(description = "Vehicle license plate", example = "ABC1234")
    String licensePlate,

    @JsonProperty("sector")
    @Schema(description = "Parking sector name (required for ENTRY events)", example = "A")
    @Pattern(regexp = "^[A-Z]$", message = "Sector must be a single uppercase letter")
    String sector,

    @JsonProperty("latitude")
    @NotNull(message = "Latitude cannot be null")
    @Schema(description = "Latitude coordinate (required for PARKED events)", example = "10.5")
    Double latitude,

    @JsonProperty("longitude")
    @NotNull(message = "Longitude cannot be null")
    @Schema(description = "Longitude coordinate (required for PARKED events)", example = "20.5")
    Double longitude,

    @JsonProperty("entry_time")
    @Schema(description = "Entry timestamp from webhook (provided in ENTRY events)", example = "2026-04-20T10:30:00")
    LocalDateTime entryTime,

    @JsonProperty("exit_time")
    @Schema(description = "Exit timestamp from webhook (provided in EXIT events)", example = "2026-04-20T14:45:00")
    LocalDateTime exitTime
) {}
