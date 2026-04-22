package br.com.teste.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import br.com.teste.dto.VehicleEventRequest;
import br.com.teste.model.ParkingSpot;
import br.com.teste.model.Revenue;
import br.com.teste.model.Sector;
import br.com.teste.model.Vehicle;
import br.com.teste.model.VehicleStatus;
import br.com.teste.repository.ParkingSpotRepository;
import br.com.teste.repository.RevenueRepository;
import br.com.teste.repository.SectorRepository;
import br.com.teste.repository.VehicleRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end test for the complete parking charging flow.
 * This tests the ACTUAL behavior without Thread.sleep() delays.
 * We simulate time by manipulating entryTime directly.
 */
@SpringBootTest
@ActiveProfiles("test")
class ChargingEndToEndTest {

    @Autowired
    private VehicleEventService vehicleEventService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    @Autowired
    private RevenueRepository revenueRepository;

    @Autowired
    private GarageConfigService garageConfigService;

    private Sector testSector;

    @BeforeEach
    void setUp() {
        // Clean all data
        vehicleRepository.deleteAll();
        parkingSpotRepository.deleteAll();
        revenueRepository.deleteAll();
        sectorRepository.deleteAll();

        // Clear cache
        garageConfigService.clearCache();

        // Create test sector
        testSector = new Sector("A", new BigDecimal("10.00"), 5);
        testSector = sectorRepository.save(testSector);

        // Create parking spots
        for (int i = 1; i <= 5; i++) {
            ParkingSpot spot = new ParkingSpot(testSector, 10.0 + i, 20.0 + i);
            parkingSpotRepository.save(spot);
        }
    }

    @Test
    void testChargingFlow_31Minutes_ShouldCharge() {
        System.out.println("\n=== TEST: 31 Minutes Parking ===");
        String licensePlate = "TEST0001";
        LocalDate today = LocalDate.now();

        // ENTRY: Dynamic price will be 9.00 (10% discount for low occupancy)
        vehicleEventService.processEvent(new VehicleEventRequest(
            "ENTRY", licensePlate, "A", 10.5, 20.5, null, null
        ));

        Optional<Vehicle> vehicleEntry = vehicleRepository.findByLicensePlate(licensePlate);
        assertTrue(vehicleEntry.isPresent());
        BigDecimal priceAtEntry = vehicleEntry.get().getPriceAtEntry();
        System.out.println("Price at Entry: " + priceAtEntry);
        assertTrue(priceAtEntry.compareTo(BigDecimal.ZERO) > 0);

        // PARKED
        vehicleEventService.processEvent(new VehicleEventRequest(
            "PARKED", licensePlate, null, 10.5, 20.5, null, null
        ));

        Optional<Vehicle> vehicleParked = vehicleRepository.findByLicensePlate(licensePlate);
        assertTrue(vehicleParked.isPresent());
        assertEquals(VehicleStatus.PARKED, vehicleParked.get().getStatus());

        // Now we manually simulate EXIT with entry time 31 minutes ago
        // This avoids Thread.sleep() and tests the actual calculation
        Vehicle vehicle = vehicleParked.get();
        LocalDateTime entryTimeSimulated = LocalDateTime.now().minusMinutes(31);
        vehicle.setEntryTime(entryTimeSimulated);
        vehicleRepository.save(vehicle);

        // EXIT
        vehicleEventService.processEvent(new VehicleEventRequest(
            "EXIT", licensePlate, null, null, null, null, null
        ));

        Optional<Vehicle> vehicleExit = vehicleRepository.findByLicensePlate(licensePlate);
        assertTrue(vehicleExit.isPresent());
        assertEquals(VehicleStatus.EXITED, vehicleExit.get().getStatus());

        BigDecimal chargeAmount = vehicleExit.get().getTotalPrice();
        System.out.println("Total Charge: " + chargeAmount);
        assertNotNull(chargeAmount);
        assertTrue(chargeAmount.compareTo(BigDecimal.ZERO) > 0,
            "Charge should be > 0 for 31 minutes parking");

        // Verify revenue was recorded
        Optional<Revenue> revenueRecord = revenueRepository.findBySectorAndDate(testSector, today);
        assertTrue(revenueRecord.isPresent(), "Revenue should be recorded");

        Revenue revenue = revenueRecord.get();
        assertEquals(1, revenue.getVehicleCount());
        assertTrue(revenue.getTotalAmount().compareTo(BigDecimal.ZERO) > 0,
            "Revenue should be > 0, but was: " + revenue.getTotalAmount());
        assertEquals(chargeAmount, revenue.getTotalAmount(),
            "Revenue should match the charge");

        System.out.println("Revenue Recorded: " + revenue.getTotalAmount());
        System.out.println("TEST PASSED: Charging works correctly!");
    }

    @Test
    void testChargingFlow_20Minutes_ShouldBeFree() {
        System.out.println("\n=== TEST: 20 Minutes Parking (Free) ===");
        String licensePlate = "TEST0002";
        LocalDate today = LocalDate.now();

        // ENTRY
        vehicleEventService.processEvent(new VehicleEventRequest(
            "ENTRY", licensePlate, "A", 10.5, 20.5, null, null
        ));

        // PARKED
        vehicleEventService.processEvent(new VehicleEventRequest(
            "PARKED", licensePlate, null, 10.5, 20.5, null, null
        ));

        // Simulate 20 minutes ago (under threshold)
        Optional<Vehicle> vehicleParked = vehicleRepository.findByLicensePlate(licensePlate);
        Vehicle vehicle = vehicleParked.get();
        vehicle.setEntryTime(LocalDateTime.now().minusMinutes(20));
        vehicleRepository.save(vehicle);

        // EXIT
        vehicleEventService.processEvent(new VehicleEventRequest(
            "EXIT", licensePlate, null, null, null, null, null
        ));

        Optional<Vehicle> vehicleExit = vehicleRepository.findByLicensePlate(licensePlate);
        assertTrue(vehicleExit.isPresent());

        BigDecimal chargeAmount = vehicleExit.get().getTotalPrice();
        System.out.println("Total Charge: " + chargeAmount);
        assertTrue(chargeAmount.compareTo(BigDecimal.ZERO) == 0,
            "Charge should be ZERO for 20 minutes (under threshold)");

        System.out.println("TEST PASSED: Free parking works correctly!");
    }

    @Test
    void testChargingFlow_2Hours_ShouldCharge200() {
        System.out.println("\n=== TEST: 2 Hours Parking ===");
        String licensePlate = "TEST0003";
        LocalDate today = LocalDate.now();

        // ENTRY with base price 10.00, low occupancy discount = 9.00
        vehicleEventService.processEvent(new VehicleEventRequest(
            "ENTRY", licensePlate, "A", 10.5, 20.5, null, null
        ));

        Optional<Vehicle> vehicleEntry = vehicleRepository.findByLicensePlate(licensePlate);
        BigDecimal priceAtEntry = vehicleEntry.get().getPriceAtEntry();

        // PARKED
        vehicleEventService.processEvent(new VehicleEventRequest(
            "PARKED", licensePlate, null, 10.5, 20.5, null, null
        ));

        // Simulate 120 minutes (2 hours)
        Optional<Vehicle> vehicleParked = vehicleRepository.findByLicensePlate(licensePlate);
        Vehicle vehicle = vehicleParked.get();
        vehicle.setEntryTime(LocalDateTime.now().minusMinutes(120));
        vehicleRepository.save(vehicle);

        // EXIT
        vehicleEventService.processEvent(new VehicleEventRequest(
            "EXIT", licensePlate, null, null, null, null, null
        ));

        Optional<Vehicle> vehicleExit = vehicleRepository.findByLicensePlate(licensePlate);
        BigDecimal chargeAmount = vehicleExit.get().getTotalPrice();
        System.out.println("Price at Entry: " + priceAtEntry);
        System.out.println("Parking Duration: 2 hours (120 minutes)");
        System.out.println("Total Charge: " + chargeAmount);

        // After 30 minutes threshold: 90 minutes remaining
        // Rounds up to 2 hours: 2 * priceAtEntry
        BigDecimal expectedCharge = priceAtEntry.multiply(BigDecimal.valueOf(2));
        assertEquals(expectedCharge, chargeAmount,
            "Charge should be 2 * " + priceAtEntry);

        // Verify revenue
        Optional<Revenue> revenueRecord = revenueRepository.findBySectorAndDate(testSector, today);
        assertTrue(revenueRecord.isPresent());
        assertEquals(chargeAmount, revenueRecord.get().getTotalAmount());

        System.out.println("Revenue: " + revenueRecord.get().getTotalAmount());
        System.out.println("TEST PASSED: 2-hour charging works correctly!");
    }

    @Test
    void testMultipleVehicles_AggregateRevenue() {
        System.out.println("\n=== TEST: Multiple Vehicles Revenue Aggregation ===");
        LocalDate today = LocalDate.now();

        // Vehicle 1: 31 minutes
        simulateVehicleCharge("CAR001", 31);

        // Vehicle 2: 45 minutes
        simulateVehicleCharge("CAR002", 45);

        // Vehicle 3: 90 minutes
        simulateVehicleCharge("CAR003", 90);

        // Check aggregated revenue
        Optional<Revenue> revenue = revenueRepository.findBySectorAndDate(testSector, today);
        assertTrue(revenue.isPresent());

        System.out.println("Total Vehicles: " + revenue.get().getVehicleCount());
        System.out.println("Total Revenue: " + revenue.get().getTotalAmount());

        assertEquals(3, revenue.get().getVehicleCount());
        assertTrue(revenue.get().getTotalAmount().compareTo(BigDecimal.ZERO) > 0,
            "Aggregated revenue should be > 0");

        System.out.println("TEST PASSED: Multi-vehicle revenue aggregation works!");
    }

    private void simulateVehicleCharge(String licensePlate, int minutes) {
        System.out.println("  - Vehicle " + licensePlate + ": " + minutes + " minutes");

        // ENTRY
        vehicleEventService.processEvent(new VehicleEventRequest(
            "ENTRY", licensePlate, "A", 10.5, 20.5, null, null
        ));

        // PARKED
        vehicleEventService.processEvent(new VehicleEventRequest(
            "PARKED", licensePlate, null, 10.5, 20.5, null, null
        ));

        // Simulate parking duration
        Optional<Vehicle> vehicleParked = vehicleRepository.findByLicensePlate(licensePlate);
        Vehicle vehicle = vehicleParked.get();
        vehicle.setEntryTime(LocalDateTime.now().minusMinutes(minutes));
        vehicleRepository.save(vehicle);

        // EXIT
        vehicleEventService.processEvent(new VehicleEventRequest(
            "EXIT", licensePlate, null, null, null, null, null
        ));
    }
}
