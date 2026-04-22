package br.com.teste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SectorDto(
    Long id,
    String name,
    @JsonProperty("basePrice")
    BigDecimal basePrice,
    @JsonProperty("maxCapacity")
    Integer maxCapacity,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
