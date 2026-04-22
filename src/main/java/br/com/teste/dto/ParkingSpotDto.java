package br.com.teste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record ParkingSpotDto(
    Long id,
    SectorDto sector,
    Double latitude,
    Double longitude,
    @JsonProperty("isOccupied")
    Boolean isOccupied,
    LocalDateTime createdAt
) {
}
