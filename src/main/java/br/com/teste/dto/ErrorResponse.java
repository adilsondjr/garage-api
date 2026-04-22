package br.com.teste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Error response returned by API")
public record ErrorResponse(
    @JsonProperty("error")
    @Schema(description = "Error message describing the problem", example = "Sector not found: X")
    String error,

    @JsonProperty("timestamp")
    @Schema(description = "Timestamp when error occurred", example = "2025-01-15T15:30:00")
    LocalDateTime timestamp,

    @JsonProperty("status")
    @Schema(description = "HTTP status code", example = "404")
    int status
) {}
