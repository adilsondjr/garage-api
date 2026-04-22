package br.com.teste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Revenue information for a parking sector on a specific date")
public record RevenueResponse(
    @JsonProperty("amount")
    @Schema(description = "Total revenue amount in currency units", example = "150.50")
    BigDecimal amount,

    @JsonProperty("currency")
    @Schema(description = "Currency code (ISO 4217)", example = "BRL")
    String currency,

    @JsonProperty("timestamp")
    @Schema(description = "Timestamp when revenue was queried", example = "2025-01-15T15:30:00")
    LocalDateTime timestamp
) {}
