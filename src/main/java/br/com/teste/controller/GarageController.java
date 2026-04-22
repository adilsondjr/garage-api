package br.com.teste.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.teste.dto.ParkingSpotDto;
import br.com.teste.dto.SectorDto;
import br.com.teste.model.ParkingSpot;
import br.com.teste.model.Sector;
import br.com.teste.repository.ParkingSpotRepository;
import br.com.teste.repository.SectorRepository;
import br.com.teste.service.OccupancyService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/garage")
@Tag(name = "Garage Monitoring", description = "Endpoints for monitoring parking garage status")
public class GarageController {
    private final SectorRepository sectorRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final OccupancyService occupancyService;

    public GarageController(SectorRepository sectorRepository, ParkingSpotRepository parkingSpotRepository,
                          OccupancyService occupancyService) {
        this.sectorRepository = sectorRepository;
        this.parkingSpotRepository = parkingSpotRepository;
        this.occupancyService = occupancyService;
    }

    @GetMapping("/sectors")
    @Operation(summary = "Get all sectors",
        description = "Retrieve list of all parking sectors with their configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of sectors",
            content = @Content(schema = @Schema(implementation = SectorDto.class)))
    })
    public ResponseEntity<List<SectorDto>> getSectors() {
        List<Sector> sectors = sectorRepository.findAll();
        List<SectorDto> dtos = sectors.stream()
            .map(this::toSectorDto)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/spots")
    @Operation(summary = "Get all parking spots",
        description = "Retrieve list of all parking spots with their occupancy status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of parking spots",
            content = @Content(schema = @Schema(implementation = ParkingSpotDto.class)))
    })
    public ResponseEntity<List<ParkingSpotDto>> getSpots() {
        List<ParkingSpot> spots = parkingSpotRepository.findAllWithSector();
        List<ParkingSpotDto> dtos = spots.stream()
            .map(this::toParkingSpotDto)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/occupancy")
    @Operation(summary = "Get sector occupancy",
        description = "Retrieve real-time occupancy information for all parking sectors")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Occupancy data by sector",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"A\": {\"occupied\": 25, \"total\": 100, \"available\": 75, \"percentage\": 25.0}}")))
    })
    public ResponseEntity<Map<String, Object>> getOccupancy() {
        return ResponseEntity.ok(occupancyService.getAllOccupancy());
    }

    private SectorDto toSectorDto(Sector sector) {
        return new SectorDto(
            sector.getId(),
            sector.getName(),
            sector.getBasePrice(),
            sector.getMaxCapacity(),
            sector.getCreatedAt(),
            sector.getUpdatedAt()
        );
    }

    private ParkingSpotDto toParkingSpotDto(ParkingSpot spot) {
        SectorDto sectorDto = toSectorDto(spot.getSector());
        return new ParkingSpotDto(
            spot.getId(),
            sectorDto,
            spot.getLatitude(),
            spot.getLongitude(),
            spot.getIsOccupied(),
            spot.getCreatedAt()
        );
    }
}
