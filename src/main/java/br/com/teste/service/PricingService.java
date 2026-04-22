package br.com.teste.service;

import org.springframework.stereotype.Service;
import br.com.teste.config.PricingConfiguration;
import br.com.teste.model.Sector;
import br.com.teste.repository.ParkingSpotRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class PricingService {
    private final ParkingSpotRepository parkingSpotRepository;
    private final PricingConfiguration pricingConfig;

    public PricingService(ParkingSpotRepository parkingSpotRepository, PricingConfiguration pricingConfig) {
        this.parkingSpotRepository = parkingSpotRepository;
        this.pricingConfig = pricingConfig;
    }

    /**
     * Calculate dynamic price at entry based on sector occupancy
     * Uses configurable thresholds and multipliers
     */
    public BigDecimal calculateDynamicPrice(Sector sector) {
        BigDecimal basePrice = sector.getBasePrice();
        int maxCapacity = sector.getMaxCapacity();
        int occupiedCount = parkingSpotRepository.countOccupiedInSector(sector);

        double occupancy = (double) occupiedCount / maxCapacity;

        BigDecimal multiplier;
        if (occupancy < pricingConfig.getLowOccupancyThreshold()) {
            multiplier = BigDecimal.valueOf(pricingConfig.getLowOccupancyMultiplier());
        } else if (occupancy <= pricingConfig.getMediumOccupancyThreshold()) {
            multiplier = BigDecimal.valueOf(pricingConfig.getMediumOccupancyMultiplier());
        } else if (occupancy <= pricingConfig.getHighOccupancyThreshold()) {
            multiplier = BigDecimal.valueOf(pricingConfig.getHighOccupancyMultiplier());
        } else {
            multiplier = BigDecimal.valueOf(pricingConfig.getFullOccupancyMultiplier());
        }

        return basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate final charge for parking
     * First N minutes: free (configurable)
     * After N minutes: ceil(hoursParked) * adjustedPrice
     */
    public BigDecimal calculateFinalCharge(
            LocalDateTime entryTime,
            LocalDateTime exitTime,
            BigDecimal priceAtEntry) {

        long minutesParked = ChronoUnit.MINUTES.between(entryTime, exitTime);

        if (minutesParked <= pricingConfig.getFreeParsingMinutes()) {
            return BigDecimal.ZERO;
        }

        long minutesAfterFree = minutesParked - pricingConfig.getFreeParsingMinutes();
        long hoursParked = (minutesAfterFree + 59) / 60;

        return priceAtEntry.multiply(BigDecimal.valueOf(hoursParked))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get current occupancy percentage for a sector
     */
    public double getOccupancyPercentage(Sector sector) {
        int maxCapacity = sector.getMaxCapacity();
        int occupiedCount = parkingSpotRepository.countOccupiedInSector(sector);
        return (double) occupiedCount / maxCapacity * 100;
    }

    /**
     * Check if sector is full (100% occupancy)
     */
    public boolean isSectorFull(Sector sector) {
        int maxCapacity = sector.getMaxCapacity();
        int occupiedCount = parkingSpotRepository.countOccupiedInSector(sector);
        return occupiedCount >= maxCapacity;
    }
}
