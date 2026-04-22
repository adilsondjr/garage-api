package br.com.teste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record GarageConfigResponse(
    @JsonProperty("garage")
    List<SectorConfig> sectors,

    @JsonProperty("spots")
    List<SpotConfig> spots
) {
    public record SectorConfig(
        @JsonProperty("sector")
        String name,

        @JsonProperty("base_price")
        BigDecimal basePrice,

        @JsonProperty("max_capacity")
        Integer maxCapacity
    ) {}

    public record SpotConfig(
        @JsonProperty("id")
        Integer id,

        @JsonProperty("sector")
        String sector,

        @JsonProperty("lat")
        Double latitude,

        @JsonProperty("lng")
        Double longitude,

        @JsonProperty("occupied")
        Boolean occupied
    ) {}
}
