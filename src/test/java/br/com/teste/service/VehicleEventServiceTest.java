package br.com.teste.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import br.com.teste.dto.VehicleEventRequest;
import br.com.teste.exception.InvalidEventException;
import br.com.teste.exception.SectorFullException;
import br.com.teste.exception.VehicleNotFoundException;
import br.com.teste.model.ParkingSpot;
import br.com.teste.model.Sector;
import br.com.teste.model.Vehicle;
import br.com.teste.model.VehicleStatus;
import br.com.teste.repository.ParkingSpotRepository;
import br.com.teste.repository.VehicleRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleEventServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    @Mock
    private GarageConfigService garageConfigService;

    @Mock
    private PricingService pricingService;

    @Mock
    private RevenueService revenueService;

    private VehicleEventService vehicleEventService;
    private Sector testSector;

    @BeforeEach
    void setUp() {
        vehicleEventService = new VehicleEventService(
            vehicleRepository,
            parkingSpotRepository,
            garageConfigService,
            pricingService,
            revenueService
        );

        testSector = new Sector("A", new BigDecimal("10.00"), 100);
        testSector.setId(1L);
    }

    @Test
    void testHandleEntry_withAvailableSpot_createsVehicle() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 4, 20, 10, 30, 0);
        VehicleEventRequest request = new VehicleEventRequest(
            "ENTRY", "ABC1234", "A", 10.5, 20.5, entryTime, null
        );

        when(garageConfigService.getSectorByName("A")).thenReturn(testSector);
        when(pricingService.isSectorFull(testSector)).thenReturn(false);
        when(pricingService.calculateDynamicPrice(testSector)).thenReturn(new BigDecimal("9.00"));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        vehicleEventService.processEvent(request);

        verify(vehicleRepository).save(any(Vehicle.class));
        verify(pricingService).calculateDynamicPrice(testSector);
    }

    @Test
    void testHandleEntry_withFullSector_throwsSectorFullException() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 4, 20, 10, 30, 0);
        VehicleEventRequest request = new VehicleEventRequest(
            "ENTRY", "ABC1234", "A", 10.5, 20.5, entryTime, null
        );

        when(garageConfigService.getSectorByName("A")).thenReturn(testSector);
        when(pricingService.isSectorFull(testSector)).thenReturn(true);

        assertThrows(SectorFullException.class, () -> vehicleEventService.processEvent(request));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void testHandleParked_updatesVehicleStatus() {
        VehicleEventRequest request = new VehicleEventRequest(
            "PARKED", "ABC1234", null, 10.5, 20.5, null, null
        );

        Vehicle vehicle = new Vehicle("ABC1234", testSector, LocalDateTime.now(),
            new BigDecimal("10.00"), VehicleStatus.ENTRY);
        vehicle.setId(1L);

        ParkingSpot spot = new ParkingSpot(testSector, 10.5, 20.5);
        spot.setId(1L);

        when(vehicleRepository.findByLicensePlate("ABC1234")).thenReturn(Optional.of(vehicle));
        when(parkingSpotRepository.findFirstBySectorAndIsOccupiedOrderById(testSector, false)).thenReturn(Optional.of(spot));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        vehicleEventService.processEvent(request);

        verify(vehicleRepository).save(any(Vehicle.class));
        verify(parkingSpotRepository).save(any(ParkingSpot.class));
    }

    @Test
    void testHandleExit_calculatesChargeAndUpdatesRevenue() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 4, 20, 10, 30, 0);
        LocalDateTime exitTime = LocalDateTime.of(2026, 4, 20, 14, 45, 0);
        VehicleEventRequest request = new VehicleEventRequest(
            "EXIT", "ABC1234", null, null, null, null, exitTime
        );

        ParkingSpot spot = new ParkingSpot(testSector, 10.5, 20.5);
        spot.setId(1L);
        spot.setIsOccupied(true);

        Vehicle vehicle = new Vehicle("ABC1234", testSector, entryTime,
            new BigDecimal("10.00"), VehicleStatus.PARKED);
        vehicle.setId(1L);
        vehicle.setParkingSpot(spot);

        when(vehicleRepository.findByLicensePlateForUpdate("ABC1234")).thenReturn(Optional.of(vehicle));
        when(pricingService.calculateFinalCharge(any(LocalDateTime.class), any(LocalDateTime.class),
            any(BigDecimal.class))).thenReturn(new BigDecimal("20.00"));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(parkingSpotRepository.save(any(ParkingSpot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        vehicleEventService.processEvent(request);

        verify(vehicleRepository).save(any(Vehicle.class));
        verify(parkingSpotRepository).save(any(ParkingSpot.class));
        verify(revenueService).addVehicleCharge(any(Sector.class), any(java.time.LocalDate.class), any(BigDecimal.class));
    }

    @Test
    void testProcessEvent_withMissingEventType_throwsInvalidEventException() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 4, 20, 10, 30, 0);
        VehicleEventRequest request = new VehicleEventRequest(
            null, "ABC1234", "A", 10.5, 20.5, entryTime, null
        );

        assertThrows(InvalidEventException.class, () -> vehicleEventService.processEvent(request));
    }

    @Test
    void testProcessEvent_withUnknownEventType_throwsInvalidEventException() {
        LocalDateTime entryTime = LocalDateTime.of(2026, 4, 20, 10, 30, 0);
        VehicleEventRequest request = new VehicleEventRequest(
            "UNKNOWN", "ABC1234", "A", 10.5, 20.5, entryTime, null
        );

        assertThrows(InvalidEventException.class, () -> vehicleEventService.processEvent(request));
    }

    @Test
    void testHandleParked_withVehicleNotFound_throwsVehicleNotFoundException() {
        VehicleEventRequest request = new VehicleEventRequest(
            "PARKED", "ABC1234", null, 10.5, 20.5, null, null
        );

        when(vehicleRepository.findByLicensePlate("ABC1234")).thenReturn(Optional.empty());

        assertThrows(VehicleNotFoundException.class, () -> vehicleEventService.processEvent(request));
    }
}
