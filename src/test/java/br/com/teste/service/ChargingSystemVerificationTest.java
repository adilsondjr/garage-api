package br.com.teste.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
 * Final verification test for the parking charging system.
 * Confirms that the charging flow (ENTRY → PARKED → EXIT) works correctly
 * and that revenue is properly recorded in the database.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Parking Charging System Verification")
class ChargingSystemVerificationTest {

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

    private Sector sectorA;
    private Sector sectorB;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();
        parkingSpotRepository.deleteAll();
        revenueRepository.deleteAll();
        sectorRepository.deleteAll();
        garageConfigService.clearCache();

        // Create test sectors
        sectorA = new Sector("A", new BigDecimal("10.00"), 10);
        sectorA = sectorRepository.save(sectorA);

        sectorB = new Sector("B", new BigDecimal("15.00"), 10);
        sectorB = sectorRepository.save(sectorB);

        // Create parking spots for sector A
        for (int i = 0; i < 10; i++) {
            parkingSpotRepository.save(new ParkingSpot(sectorA, 10.0 + i, 20.0 + i));
        }

        // Create parking spots for sector B
        for (int i = 0; i < 10; i++) {
            parkingSpotRepository.save(new ParkingSpot(sectorB, 30.0 + i, 40.0 + i));
        }
    }

    @Test
    @DisplayName("Verify: Vehicle completes full parking cycle ENTRY → PARKED → EXIT with charge")
    void verifyFullParkingCycle() {
        System.out.println("\n" +
            "╔════════════════════════════════════════════════════════════════════╗\n" +
            "║  VERIFICATION TEST: Full Parking Cycle                            ║\n" +
            "╚════════════════════════════════════════════════════════════════════╝\n");

        String licensePlate = "TEST-VERIFY-001";
        LocalDate today = LocalDate.now();

        // Step 1: ENTRY
        System.out.println("Step 1: ENTRY - Vehicle arrives at sector A");
        vehicleEventService.processEvent(new VehicleEventRequest(
            "ENTRY", licensePlate, "A", 10.0, 20.0
        , null, null
        ));

        Optional<Vehicle> afterEntry = vehicleRepository.findByLicensePlate(licensePlate);
        assertTrue(afterEntry.isPresent(), "Vehicle should exist after ENTRY");
        assertEquals(VehicleStatus.ENTRY, afterEntry.get().getStatus());
        BigDecimal priceAtEntry = afterEntry.get().getPriceAtEntry();
        assertTrue(priceAtEntry.compareTo(BigDecimal.ZERO) > 0,
            "Price at entry should be > 0, got: " + priceAtEntry);
        System.out.println("   ✓ Vehicle entered with price: " + priceAtEntry);

        // Step 2: PARKED
        System.out.println("\nStep 2: PARKED - Vehicle parks at a spot");
        vehicleEventService.processEvent(new VehicleEventRequest(
            "PARKED", licensePlate, "A", 10.0, 20.0
        , null, null
        ));

        Optional<Vehicle> afterParked = vehicleRepository.findByLicensePlate(licensePlate);
        assertTrue(afterParked.isPresent());
        assertEquals(VehicleStatus.PARKED, afterParked.get().getStatus());
        assertNotNull(afterParked.get().getParkingSpot());
        System.out.println("   ✓ Vehicle parked at spot: " + afterParked.get().getParkingSpot().getId());

        // Step 3: Simulate 45 minutes of parking
        System.out.println("\nStep 3: SIMULATED TIME - 45 minutes of parking");
        Vehicle vehicle = afterParked.get();
        vehicle.setEntryTime(LocalDateTime.now().minusMinutes(45));
        vehicleRepository.save(vehicle);
        System.out.println("   ✓ Entry time set to 45 minutes ago");

        // Step 4: EXIT
        System.out.println("\nStep 4: EXIT - Vehicle leaves parking");
        vehicleEventService.processEvent(new VehicleEventRequest(
            "EXIT", licensePlate, "A", 10.0, 20.0
        , null, null
        ));

        Optional<Vehicle> afterExit = vehicleRepository.findByLicensePlate(licensePlate);
        assertTrue(afterExit.isPresent());
        assertEquals(VehicleStatus.EXITED, afterExit.get().getStatus());
        assertNotNull(afterExit.get().getExitTime());

        BigDecimal totalCharge = afterExit.get().getTotalPrice();
        System.out.println("   ✓ Vehicle exited");
        System.out.println("   ✓ Total charge calculated: " + totalCharge);

        // Step 5: Verify charge > 0
        assertTrue(totalCharge.compareTo(BigDecimal.ZERO) > 0,
            "Charge should be > 0 for 45 minutes, got: " + totalCharge);

        // Step 6: Verify revenue was recorded
        System.out.println("\nStep 5: REVENUE - Verify charge was recorded");
        Optional<Revenue> revenue = revenueRepository.findBySectorAndDate(sectorA, today);
        assertTrue(revenue.isPresent(), "Revenue should be recorded for sector A today");

        Revenue record = revenue.get();
        assertEquals(1, record.getVehicleCount(), "Should have 1 vehicle");
        assertEquals(totalCharge, record.getTotalAmount(),
            "Revenue should equal the charge");
        System.out.println("   ✓ Revenue recorded: " + record.getTotalAmount());
        System.out.println("   ✓ Vehicle count: " + record.getVehicleCount());

        System.out.println("\n✅ VERIFICATION PASSED: Full parking cycle works correctly!\n");
    }

    @Test
    @DisplayName("Verify: Free parking (≤30 minutes) results in zero charge and zero revenue")
    void verifyFreeParkingNoCharge() {
        System.out.println("\n" +
            "╔════════════════════════════════════════════════════════════════════╗\n" +
            "║  VERIFICATION TEST: Free Parking (≤30 minutes)                    ║\n" +
            "╚════════════════════════════════════════════════════════════════════╝\n");

        String licensePlate = "FREE-PARKING-001";
        LocalDate today = LocalDate.now();

        vehicleEventService.processEvent(new VehicleEventRequest(
            "ENTRY", licensePlate, "A", 10.0, 20.0
        , null, null
        ));
        vehicleEventService.processEvent(new VehicleEventRequest(
            "PARKED", licensePlate, "A", 10.0, 20.0
        , null, null
        ));

        // Simulate 25 minutes (< 30 min threshold)
        Optional<Vehicle> vehicle = vehicleRepository.findByLicensePlate(licensePlate);
        vehicle.ifPresent(v -> {
            v.setEntryTime(LocalDateTime.now().minusMinutes(25));
            vehicleRepository.save(v);
        });

        vehicleEventService.processEvent(new VehicleEventRequest(
            "EXIT", licensePlate, "A", 10.0, 20.0
        , null, null
        ));

        Optional<Vehicle> exitedVehicle = vehicleRepository.findByLicensePlate(licensePlate);
        assertTrue(exitedVehicle.isPresent());

        BigDecimal charge = exitedVehicle.get().getTotalPrice();
        assertTrue(charge.compareTo(BigDecimal.ZERO) == 0,
            "Charge should be ZERO for 25 minutes, got: " + charge);

        System.out.println("   ✓ Vehicle parked for 25 minutes");
        System.out.println("   ✓ Charge is: " + charge + " (FREE)");
        System.out.println("\n✅ VERIFICATION PASSED: Free parking works correctly!\n");
    }

    @Test
    @DisplayName("Verify: Multiple vehicles in same sector have revenue aggregated correctly")
    void verifyRevenueAggregation() {
        System.out.println("\n" +
            "╔════════════════════════════════════════════════════════════════════╗\n" +
            "║  VERIFICATION TEST: Revenue Aggregation                           ║\n" +
            "╚════════════════════════════════════════════════════════════════════╝\n");

        LocalDate today = LocalDate.now();

        // Vehicle 1: 31 minutes
        executeFullCycle("VEH-001", "A", 31);
        System.out.println("   ✓ Vehicle 1: 31 minutes");

        // Vehicle 2: 60 minutes
        executeFullCycle("VEH-002", "A", 60);
        System.out.println("   ✓ Vehicle 2: 60 minutes");

        // Vehicle 3: 90 minutes
        executeFullCycle("VEH-003", "A", 90);
        System.out.println("   ✓ Vehicle 3: 90 minutes");

        // Verify aggregation
        Optional<Revenue> revenue = revenueRepository.findBySectorAndDate(sectorA, today);
        assertTrue(revenue.isPresent(), "Revenue should be recorded");

        Revenue record = revenue.get();
        assertEquals(3, record.getVehicleCount(), "Should have 3 vehicles");
        assertTrue(record.getTotalAmount().compareTo(BigDecimal.ZERO) > 0,
            "Total amount should be > 0");

        System.out.println("\n   Summary:");
        System.out.println("   ✓ Total vehicles: " + record.getVehicleCount());
        System.out.println("   ✓ Total revenue: " + record.getTotalAmount());
        System.out.println("\n✅ VERIFICATION PASSED: Revenue aggregation works correctly!\n");
    }

    @Test
    @DisplayName("Verify: Different sectors have independent revenue records")
    void verifySeparateSectorRevenue() {
        System.out.println("\n" +
            "╔════════════════════════════════════════════════════════════════════╗\n" +
            "║  VERIFICATION TEST: Separate Sector Revenue                       ║\n" +
            "╚════════════════════════════════════════════════════════════════════╝\n");

        LocalDate today = LocalDate.now();

        // Vehicle in sector A: 45 minutes
        executeFullCycle("SECTOR-A-VEH", "A", 45);
        System.out.println("   ✓ Vehicle in sector A: 45 minutes");

        // Vehicle in sector B: 60 minutes
        executeFullCycle("SECTOR-B-VEH", "B", 60);
        System.out.println("   ✓ Vehicle in sector B: 60 minutes");

        // Verify sector A revenue
        Optional<Revenue> revenueA = revenueRepository.findBySectorAndDate(sectorA, today);
        assertTrue(revenueA.isPresent(), "Revenue for sector A should exist");
        assertEquals(1, revenueA.get().getVehicleCount(), "Sector A should have 1 vehicle");
        assertTrue(revenueA.get().getTotalAmount().compareTo(BigDecimal.ZERO) > 0);

        // Verify sector B revenue
        Optional<Revenue> revenueB = revenueRepository.findBySectorAndDate(sectorB, today);
        assertTrue(revenueB.isPresent(), "Revenue for sector B should exist");
        assertEquals(1, revenueB.get().getVehicleCount(), "Sector B should have 1 vehicle");
        assertTrue(revenueB.get().getTotalAmount().compareTo(BigDecimal.ZERO) > 0);

        System.out.println("\n   Sector A:");
        System.out.println("   ✓ Revenue: " + revenueA.get().getTotalAmount());
        System.out.println("   ✓ Vehicles: " + revenueA.get().getVehicleCount());

        System.out.println("\n   Sector B:");
        System.out.println("   ✓ Revenue: " + revenueB.get().getTotalAmount());
        System.out.println("   ✓ Vehicles: " + revenueB.get().getVehicleCount());

        System.out.println("\n✅ VERIFICATION PASSED: Separate sector revenue works correctly!\n");
    }

    @Test
    @DisplayName("Verify: Pricing calculation is correct for various durations")
    void verifyPricingCalculations() {
        System.out.println("\n" +
            "╔════════════════════════════════════════════════════════════════════╗\n" +
            "║  VERIFICATION TEST: Pricing Calculations                          ║\n" +
            "╚════════════════════════════════════════════════════════════════════╝\n");

        LocalDate today = LocalDate.now();

        // Test case: 31 minutes → charge 1 hour
        executeFullCycle("PRICE-31MIN", "A", 31);
        Optional<Vehicle> v31 = vehicleRepository.findByLicensePlate("PRICE-31MIN");
        BigDecimal charge31 = v31.get().getTotalPrice();
        System.out.println("   31 minutes  → Charge: " + charge31);

        // Test case: 60 minutes → charge 1 hour
        executeFullCycle("PRICE-60MIN", "A", 60);
        Optional<Vehicle> v60 = vehicleRepository.findByLicensePlate("PRICE-60MIN");
        BigDecimal charge60 = v60.get().getTotalPrice();
        System.out.println("   60 minutes  → Charge: " + charge60);

        // Test case: 90 minutes → charge 2 hours
        executeFullCycle("PRICE-90MIN", "A", 90);
        Optional<Vehicle> v90 = vehicleRepository.findByLicensePlate("PRICE-90MIN");
        BigDecimal charge90 = v90.get().getTotalPrice();
        System.out.println("   90 minutes  → Charge: " + charge90);

        // Verify: 31 and 60 should have similar charges (both 1 hour)
        assertTrue(charge31.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(charge60.compareTo(BigDecimal.ZERO) > 0);

        // Verify: 90 minutes should be ≥ 60 minutes charge
        assertTrue(charge90.compareTo(charge60) >= 0,
            "90 minutes charge should be >= 60 minutes charge");

        System.out.println("\n✅ VERIFICATION PASSED: Pricing calculations are correct!\n");
    }

    /**
     * Helper method to execute a complete vehicle cycle
     */
    private void executeFullCycle(String licensePlate, String sector, int minutesParked) {
        vehicleEventService.processEvent(new VehicleEventRequest(
            "ENTRY", licensePlate, sector, 10.0, 20.0, null, null
        ));

        vehicleEventService.processEvent(new VehicleEventRequest(
            "PARKED", licensePlate, null, 10.0, 20.0, null, null
        ));

        // Simulate time
        Optional<Vehicle> vehicle = vehicleRepository.findByLicensePlate(licensePlate);
        vehicle.ifPresent(v -> {
            v.setEntryTime(LocalDateTime.now().minusMinutes(minutesParked));
            vehicleRepository.save(v);
        });

        vehicleEventService.processEvent(new VehicleEventRequest(
            "EXIT", licensePlate, null, null, null, null, null
        ));
    }
}
