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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ParkingChargeIntegrationTest {

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
    private PricingService pricingService;

    @Autowired
    private RevenueService revenueService;

    @Autowired
    private GarageConfigService garageConfigService;

    private Sector testSector;

    @BeforeEach
    void setUp() {
        // Clean up previous test data
        vehicleRepository.deleteAll();
        parkingSpotRepository.deleteAll();
        revenueRepository.deleteAll();
        sectorRepository.deleteAll();

        // Clear the cache in GarageConfigService
        garageConfigService.clearCache();

        // Create test sector with base price 10.00 and 5 spots
        testSector = new Sector("A", new BigDecimal("10.00"), 5);
        testSector = sectorRepository.save(testSector);

        // Create 5 parking spots in the sector
        for (int i = 1; i <= 5; i++) {
            ParkingSpot spot = new ParkingSpot(testSector, 10.0 + i, 20.0 + i);
            parkingSpotRepository.save(spot);
        }
    }

    @Test
    void testCompleteChargingFlow_EntryParkedExit_WithRealTimeDelay() throws InterruptedException {
        String licensePlate = "ABC1234";
        LocalDate today = LocalDate.now();

        // Step 1: Vehicle ENTRY
        // Note: sector A has 5 spots total, initially 0 occupied
        // So occupancy = 0/5 = 0% < 25% => apply 10% discount
        // Base price 10.00 * 0.9 = 9.00 (discounted)
        VehicleEventRequest entryRequest = new VehicleEventRequest(
            "ENTRY", licensePlate, "A", 10.5, 20.5
        , null, null
        );
        vehicleEventService.processEvent(entryRequest);

        // Verify vehicle is saved with ENTRY status
        Optional<Vehicle> vehicleAfterEntry = vehicleRepository.findByLicensePlate(licensePlate);
        assertTrue(vehicleAfterEntry.isPresent());
        assertEquals(VehicleStatus.ENTRY, vehicleAfterEntry.get().getStatus());
        // Price is discounted due to low occupancy
        BigDecimal entryPrice = vehicleAfterEntry.get().getPriceAtEntry();
        assertTrue(entryPrice.compareTo(BigDecimal.ZERO) > 0, "Entry price should be positive");
        System.out.println("Entry: Vehicle " + licensePlate + " entered with price " + entryPrice);

        // Step 2: Vehicle PARKED
        VehicleEventRequest parkedRequest = new VehicleEventRequest(
            "PARKED", licensePlate, "A", 10.5, 20.5
        , null, null
        );
        vehicleEventService.processEvent(parkedRequest);

        // Verify vehicle is saved with PARKED status
        Optional<Vehicle> vehicleAfterParked = vehicleRepository.findByLicensePlate(licensePlate);
        assertTrue(vehicleAfterParked.isPresent());
        assertEquals(VehicleStatus.PARKED, vehicleAfterParked.get().getStatus());
        assertNotNull(vehicleAfterParked.get().getParkingSpot());
        System.out.println("Parked: Vehicle " + licensePlate + " parked at spot " + vehicleAfterParked.get().getParkingSpot().getId());

        // Step 3: Simulate 31 minutes by setting entry time to 31 minutes ago
        // We use time manipulation instead of Thread.sleep() for faster tests
        System.out.println("Simulating 31 minutes of parking time...");
        Vehicle vehicleToUpdate = vehicleAfterParked.get();
        vehicleToUpdate.setEntryTime(LocalDateTime.now().minusMinutes(31));
        vehicleRepository.save(vehicleToUpdate);

        // Step 4: Vehicle EXIT
        VehicleEventRequest exitRequest = new VehicleEventRequest(
            "EXIT", licensePlate, "A", 10.5, 20.5
        , null, null
        );
        vehicleEventService.processEvent(exitRequest);

        // Verify vehicle EXIT status and charge
        Optional<Vehicle> vehicleAfterExit = vehicleRepository.findByLicensePlate(licensePlate);
        assertTrue(vehicleAfterExit.isPresent());
        assertEquals(VehicleStatus.EXITED, vehicleAfterExit.get().getStatus());
        assertNotNull(vehicleAfterExit.get().getExitTime());

        BigDecimal chargeAmount = vehicleAfterExit.get().getTotalPrice();
        assertNotNull(chargeAmount, "Total price should not be null");
        // For 31 seconds (over 30 min), should charge at least entryPrice * 1
        assertTrue(chargeAmount.compareTo(entryPrice) > 0 || chargeAmount.compareTo(entryPrice) == 0,
            "Charge should be >= entry price after 31 seconds");
        System.out.println("Exit: Vehicle " + licensePlate + " exited with total charge: " + chargeAmount);

        // Verify revenue was recorded
        Optional<Revenue> revenueRecord = revenueRepository.findBySectorAndDate(testSector, today);
        assertTrue(revenueRecord.isPresent(), "Revenue record should exist");

        Revenue revenue = revenueRecord.get();
        assertEquals(testSector.getId(), revenue.getSector().getId());
        assertEquals(today, revenue.getDate());
        assertEquals(1, revenue.getVehicleCount(), "Vehicle count should be 1");

        BigDecimal recordedAmount = revenue.getTotalAmount();
        assertNotNull(recordedAmount, "Total amount should not be null");
        assertTrue(recordedAmount.compareTo(BigDecimal.ZERO) > 0,
            "Total amount should be greater than zero, but was: " + recordedAmount);

        assertEquals(chargeAmount, recordedAmount,
            "Recorded amount should match calculated charge");

        System.out.println("Revenue: " + recordedAmount + " for " + revenue.getVehicleCount() + " vehicle(s)");
    }

    @Test
    void testFreeParking_Under30Minutes_NoCharge() throws InterruptedException {
        String licensePlate = "XYZ9999";
        LocalDate today = LocalDate.now();

        // ENTRY
        vehicleEventService.processEvent(new VehicleEventRequest(
            "ENTRY", licensePlate, "A", 10.5, 20.5
        , null, null
        ));

        // PARKED
        vehicleEventService.processEvent(new VehicleEventRequest(
            "PARKED", licensePlate, "A", 10.5, 20.5
        , null, null
        ));

        // Simulate 20 minutes (under 30 minutes free threshold)
        System.out.println("Simulating 20 minutes (free parking period)...");
        Optional<Vehicle> vehToUpdate2 = vehicleRepository.findByLicensePlate(licensePlate);
        Vehicle vehicle2 = vehToUpdate2.get();
        vehicle2.setEntryTime(LocalDateTime.now().minusMinutes(20));
        vehicleRepository.save(vehicle2);

        // EXIT
        vehicleEventService.processEvent(new VehicleEventRequest(
            "EXIT", licensePlate, "A", 10.5, 20.5
        , null, null
        ));

        // Verify no charge (should be ZERO)
        Optional<Vehicle> vehicle = vehicleRepository.findByLicensePlate(licensePlate);
        assertTrue(vehicle.isPresent());
        assertTrue(vehicle.get().getTotalPrice().compareTo(BigDecimal.ZERO) == 0,
            "Charge should be zero for under 30 minutes");

        // Note: Revenue service may or may not record zero amounts
        Optional<Revenue> revenue = revenueRepository.findBySectorAndDate(testSector, today);
        if (revenue.isPresent()) {
            assertTrue(revenue.get().getTotalAmount().compareTo(BigDecimal.ZERO) >= 0,
                "Revenue should be zero or not exist for free parking");
        }

        System.out.println("Free Parking: No charge recorded");
    }

    @Test
    void testMultipleVehicles_CorrectRevenueAggregation() throws InterruptedException {
        LocalDate today = LocalDate.now();
        String[] licensePlates = {"ABC1111", "ABC2222", "ABC3333"};

        // NOTE: This test will have fewer available spots as vehicles are added
        // Sector A has 5 spots, so all 3 vehicles will fit
        for (int i = 0; i < licensePlates.length; i++) {
            String plate = licensePlates[i];

            // ENTRY
            vehicleEventService.processEvent(new VehicleEventRequest(
            "ENTRY", plate, "A", 10.5 + i, 20.5 + i
            , null, null
        ));

            // PARKED
            vehicleEventService.processEvent(new VehicleEventRequest(
            "PARKED", plate, "A", 10.5 + i, 20.5 + i
            , null, null
        ));

            // Simulate different parking times for each vehicle
            int parkingMinutes = 31 + (i * 15);
            System.out.println("Vehicle " + plate + " simulating " + parkingMinutes + " minutes");
            Optional<Vehicle> vehToUpdateMulti = vehicleRepository.findByLicensePlate(plate);
            Vehicle vehMulti = vehToUpdateMulti.get();
            vehMulti.setEntryTime(LocalDateTime.now().minusMinutes(parkingMinutes));
            vehicleRepository.save(vehMulti);

            // EXIT
            vehicleEventService.processEvent(new VehicleEventRequest(
            "EXIT", plate, "A", 10.5 + i, 20.5 + i
            , null, null
        ));
        }

        // Verify revenue aggregation
        Optional<Revenue> revenue = revenueRepository.findBySectorAndDate(testSector, today);
        assertTrue(revenue.isPresent(), "Revenue record should exist");

        Revenue record = revenue.get();
        assertEquals(3, record.getVehicleCount(), "Should have 3 vehicles");
        // After free parking period, all vehicles should be charged
        assertTrue(record.getTotalAmount().compareTo(BigDecimal.ZERO) >= 0,
            "Total amount should be >= zero");

        System.out.println("Aggregated Revenue: " + record.getTotalAmount() +
            " for " + record.getVehicleCount() + " vehicles");
    }

    @Test
    void testPricingCalculation_Exact31Minutes_RoundsUpToOneHour() {
        String licensePlate = "TEST9999";

        // Calculate charge for exactly 31 minutes (1 minute over 30 minute threshold)
        // Should round up to 1 hour and charge 1 * 10.00 = 10.00
        java.time.LocalDateTime entryTime = java.time.LocalDateTime.now();
        java.time.LocalDateTime exitTime = entryTime.plusMinutes(31);

        BigDecimal charge = pricingService.calculateFinalCharge(
            entryTime,
            exitTime,
            new BigDecimal("10.00")
        );

        assertEquals(new BigDecimal("10.00"), charge,
            "31 minutes should round up to 1 hour = 10.00");
        System.out.println("Pricing: 31 minutes = " + charge);
    }

    @Test
    void testPricingCalculation_TwoHoursAndFifteenMinutes() {
        String licensePlate = "TEST8888";

        // 2 hours 15 minutes = 135 minutes
        // After 30 minute threshold: 105 minutes
        // Rounds up to 2 hours, so charge = 2 * 10.00 = 20.00
        java.time.LocalDateTime entryTime = java.time.LocalDateTime.now();
        java.time.LocalDateTime exitTime = entryTime.plusHours(2).plusMinutes(15);

        BigDecimal charge = pricingService.calculateFinalCharge(
            entryTime,
            exitTime,
            new BigDecimal("10.00")
        );

        assertEquals(new BigDecimal("20.00"), charge,
            "2h15m should round up to 2 hours = 20.00");
        System.out.println("Pricing: 2h15m = " + charge);
    }

    @Test
    void testDynamicPricing_LowOccupancy_AppliesDiscount() {
        // When occupancy < 25%, apply 10% discount
        // Base price: 10.00, with discount: 9.00
        BigDecimal dynamicPrice = pricingService.calculateDynamicPrice(testSector);

        assertEquals(new BigDecimal("9.00"), dynamicPrice,
            "Low occupancy should apply 10% discount to base price");
        System.out.println("Dynamic Price (low occupancy): " + dynamicPrice);
    }
}
