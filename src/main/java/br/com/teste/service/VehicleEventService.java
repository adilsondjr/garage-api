package br.com.teste.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class VehicleEventService {
    private static final Logger logger = LoggerFactory.getLogger(VehicleEventService.class);

    private final VehicleRepository vehicleRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final GarageConfigService garageConfigService;
    private final PricingService pricingService;
    private final RevenueService revenueService;

    public VehicleEventService(
            VehicleRepository vehicleRepository,
            ParkingSpotRepository parkingSpotRepository,
            GarageConfigService garageConfigService,
            PricingService pricingService,
            RevenueService revenueService) {
        this.vehicleRepository = vehicleRepository;
        this.parkingSpotRepository = parkingSpotRepository;
        this.garageConfigService = garageConfigService;
        this.pricingService = pricingService;
        this.revenueService = revenueService;
    }

    /**
     * Process webhook event from simulator
     */
    @Transactional
    public void processEvent(VehicleEventRequest request) {
        if (request.eventType() == null || request.licensePlate() == null) {
            throw new InvalidEventException("Missing required event parameters");
        }

        String eventType = request.eventType().toUpperCase();

        switch (eventType) {
            case "ENTRY" -> handleEntry(request);
            case "PARKED" -> handleParked(request);
            case "EXIT" -> handleExit(request);
            default -> throw new InvalidEventException("Unknown event type: " + eventType);
        }
    }

    /**
     * Handle ENTRY event: vehicle arrives at parking
     */
    private void handleEntry(VehicleEventRequest request) {
        Sector sector = garageConfigService.getSectorByName(request.sector());

        if (pricingService.isSectorFull(sector)) {
            throw new SectorFullException("Sector " + sector.getName() + " is full");
        }

        BigDecimal dynamicPrice = pricingService.calculateDynamicPrice(sector);

        LocalDateTime entryTime = request.entryTime() != null ? request.entryTime() : LocalDateTime.now();

        Vehicle vehicle = new Vehicle(
            request.licensePlate(),
            sector,
            entryTime,
            dynamicPrice,
            VehicleStatus.ENTRY
        );

        vehicleRepository.save(vehicle);

        logger.info("Vehicle {} entered sector {} at {} with price {}",
            request.licensePlate(), sector.getName(), entryTime, dynamicPrice);
    }

    /**
     * Handle PARKED event: vehicle parked in a spot
     */
    @Transactional
    private void handleParked(VehicleEventRequest request) {
        Vehicle vehicle = vehicleRepository.findByLicensePlate(request.licensePlate())
            .orElseThrow(() -> new VehicleNotFoundException(
                "Vehicle not found: " + request.licensePlate()));

        Sector sector = vehicle.getSector();

        ParkingSpot spot = parkingSpotRepository.findFirstBySectorAndIsOccupiedOrderById(sector, false)
            .orElseThrow(() -> new SectorFullException("No available spots in sector " + sector.getName()));

        spot.setIsOccupied(true);
        parkingSpotRepository.save(spot);

        vehicle.setParkingSpot(spot);
        vehicle.setStatus(VehicleStatus.PARKED);
        vehicleRepository.save(vehicle);

        logger.info("Vehicle {} parked at spot ({}, {}) in sector {}",
            request.licensePlate(), request.latitude(), request.longitude(), sector.getName());
    }

    /**
     * Handle EXIT event: vehicle leaves parking
     * Uses pessimistic lock to prevent race conditions during concurrent exits
     */
    @Transactional
    private void handleExit(VehicleEventRequest request) {
        Vehicle vehicle = vehicleRepository.findByLicensePlateForUpdate(request.licensePlate())
            .orElseThrow(() -> new VehicleNotFoundException(
                "Vehicle not found: " + request.licensePlate()));

        LocalDateTime exitTime = request.exitTime() != null ? request.exitTime() : LocalDateTime.now();
        vehicle.setExitTime(exitTime);

        BigDecimal totalCharge = pricingService.calculateFinalCharge(
            vehicle.getEntryTime(),
            exitTime,
            vehicle.getPriceAtEntry()
        );

        vehicle.setTotalPrice(totalCharge);
        vehicle.setStatus(VehicleStatus.EXITED);
        vehicleRepository.save(vehicle);

        if (vehicle.getParkingSpot() != null) {
            ParkingSpot spot = vehicle.getParkingSpot();
            spot.setIsOccupied(false);
            parkingSpotRepository.save(spot);
        }

        Sector sector = vehicle.getSector();
        if (sector != null) {
            LocalDate exitDate = exitTime.toLocalDate();
            revenueService.addVehicleCharge(sector, exitDate, totalCharge);
        }

        logger.info("Vehicle {} exited sector {} at {} with charge {}",
            request.licensePlate(), vehicle.getSector() != null ? vehicle.getSector().getName() : "unknown", exitTime, totalCharge);
    }
}
