package br.com.teste.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.com.teste.dto.RevenueResponse;
import br.com.teste.model.Revenue;
import br.com.teste.model.Sector;
import br.com.teste.service.GarageConfigService;
import br.com.teste.service.RevenueService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/revenue")
@Tag(name = "Revenue Management", description = "Query and manage parking revenue")
public class RevenueController {
    private final RevenueService revenueService;
    private final GarageConfigService garageConfigService;

    public RevenueController(RevenueService revenueService, GarageConfigService garageConfigService) {
        this.revenueService = revenueService;
        this.garageConfigService = garageConfigService;
    }

    @GetMapping
    @Operation(summary = "Get revenue for sector and date",
        description = "Retrieve total revenue collected from a parking sector on a specific date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Revenue data found",
            content = @Content(schema = @Schema(implementation = RevenueResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid date or sector parameter",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "Revenue not found or sector does not exist",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
        @ApiResponse(responseCode = "503", description = "Configuration service unavailable",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<RevenueResponse> getRevenue(
            @Parameter(description = "Date in YYYY-MM-DD format", required = true, example = "2025-01-15")
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Sector name (e.g., A, B, C)", required = true, example = "A")
            @RequestParam(required = true) String sector) {

        Sector sectorEntity = garageConfigService.getSectorByName(sector);
        Optional<Revenue> revenue = revenueService.getRevenue(sectorEntity, date);

        if (revenue.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Revenue revenueData = revenue.get();
        RevenueResponse response = new RevenueResponse(
            revenueData.getTotalAmount(),
            "BRL",
            LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }
}
