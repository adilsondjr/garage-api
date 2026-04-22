package br.com.teste.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import br.com.teste.config.PricingConfiguration;
import br.com.teste.model.Sector;
import br.com.teste.repository.ParkingSpotRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    private PricingService pricingService;
    private Sector testSector;
    private PricingConfiguration pricingConfig;

    @BeforeEach
    void setUp() {
        pricingConfig = new PricingConfiguration();
        pricingService = new PricingService(parkingSpotRepository, pricingConfig);
        testSector = new Sector("A", new BigDecimal("10.00"), 100);
        testSector.setId(1L);
    }

    @Test
    void testCalculateDynamicPrice_lowOccupancy_applies10PercentDiscount() {
        when(parkingSpotRepository.countOccupiedInSector(testSector)).thenReturn(20);

        BigDecimal result = pricingService.calculateDynamicPrice(testSector);

        assertEquals(new BigDecimal("9.00"), result);
    }

    @Test
    void testCalculateDynamicPrice_mediumOccupancy_noAdjustment() {
        when(parkingSpotRepository.countOccupiedInSector(testSector)).thenReturn(50);

        BigDecimal result = pricingService.calculateDynamicPrice(testSector);

        assertEquals(new BigDecimal("10.00"), result);
    }

    @Test
    void testCalculateDynamicPrice_highOccupancy_applies10PercentIncrease() {
        when(parkingSpotRepository.countOccupiedInSector(testSector)).thenReturn(60);

        BigDecimal result = pricingService.calculateDynamicPrice(testSector);

        assertEquals(new BigDecimal("11.00"), result);
    }

    @Test
    void testCalculateDynamicPrice_fullOccupancy_applies25PercentIncrease() {
        when(parkingSpotRepository.countOccupiedInSector(testSector)).thenReturn(80);

        BigDecimal result = pricingService.calculateDynamicPrice(testSector);

        assertEquals(new BigDecimal("12.50"), result);
    }

    @Test
    void testCalculateFinalCharge_under30Minutes_isFree() {
        LocalDateTime entryTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2025, 1, 1, 10, 20);
        BigDecimal priceAtEntry = new BigDecimal("10.00");

        BigDecimal result = pricingService.calculateFinalCharge(entryTime, exitTime, priceAtEntry);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testCalculateFinalCharge_exactly30Minutes_isFree() {
        LocalDateTime entryTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2025, 1, 1, 10, 30);
        BigDecimal priceAtEntry = new BigDecimal("10.00");

        BigDecimal result = pricingService.calculateFinalCharge(entryTime, exitTime, priceAtEntry);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testCalculateFinalCharge_over30Minutes_chargesPerHour() {
        LocalDateTime entryTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2025, 1, 1, 12, 0);
        BigDecimal priceAtEntry = new BigDecimal("10.00");

        BigDecimal result = pricingService.calculateFinalCharge(entryTime, exitTime, priceAtEntry);

        assertEquals(new BigDecimal("20.00"), result);
    }

    @Test
    void testCalculateFinalCharge_lessThanhourAfter30Min_roundsUp() {
        LocalDateTime entryTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2025, 1, 1, 11, 15);
        BigDecimal priceAtEntry = new BigDecimal("10.00");

        BigDecimal result = pricingService.calculateFinalCharge(entryTime, exitTime, priceAtEntry);

        assertEquals(new BigDecimal("10.00"), result);
    }

    @Test
    void testGetOccupancyPercentage() {
        when(parkingSpotRepository.countOccupiedInSector(testSector)).thenReturn(50);

        double result = pricingService.getOccupancyPercentage(testSector);

        assertEquals(50.0, result);
    }

    @Test
    void testIsSectorFull_whenNotFull() {
        when(parkingSpotRepository.countOccupiedInSector(testSector)).thenReturn(50);

        boolean result = pricingService.isSectorFull(testSector);

        assertFalse(result);
    }

    @Test
    void testIsSectorFull_whenFull() {
        when(parkingSpotRepository.countOccupiedInSector(testSector)).thenReturn(100);

        boolean result = pricingService.isSectorFull(testSector);

        assertTrue(result);
    }
}
