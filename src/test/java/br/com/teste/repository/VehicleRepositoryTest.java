package br.com.teste.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import br.com.teste.model.Sector;
import br.com.teste.model.Vehicle;
import br.com.teste.model.VehicleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class VehicleRepositoryTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private SectorRepository sectorRepository;

    private Sector testSector;

    @BeforeEach
    void setUp() {
        testSector = new Sector("A", new BigDecimal("10.00"), 100);
        testSector = sectorRepository.save(testSector);
    }

    @Test
    void testFindByLicensePlate() {
        Vehicle vehicle = new Vehicle("ABC1234", testSector, LocalDateTime.now(),
            new BigDecimal("10.00"), VehicleStatus.ENTRY);
        vehicleRepository.save(vehicle);

        Optional<Vehicle> found = vehicleRepository.findByLicensePlate("ABC1234");

        assertTrue(found.isPresent());
        assertEquals("ABC1234", found.get().getLicensePlate());
    }

    @Test
    void testFindByStatusAndSector() {
        Vehicle vehicle1 = new Vehicle("ABC1234", testSector, LocalDateTime.now(),
            new BigDecimal("10.00"), VehicleStatus.PARKED);
        Vehicle vehicle2 = new Vehicle("XYZ5678", testSector, LocalDateTime.now(),
            new BigDecimal("10.00"), VehicleStatus.PARKED);
        Vehicle vehicle3 = new Vehicle("DEF9012", testSector, LocalDateTime.now(),
            new BigDecimal("10.00"), VehicleStatus.ENTRY);

        vehicleRepository.save(vehicle1);
        vehicleRepository.save(vehicle2);
        vehicleRepository.save(vehicle3);

        List<Vehicle> result = vehicleRepository.findByStatusAndSector(VehicleStatus.PARKED, testSector);

        assertEquals(2, result.size());
    }

    @Test
    void testFindByStatus() {
        Vehicle vehicle1 = new Vehicle("ABC1234", testSector, LocalDateTime.now(),
            new BigDecimal("10.00"), VehicleStatus.EXITED);
        Vehicle vehicle2 = new Vehicle("XYZ5678", testSector, LocalDateTime.now(),
            new BigDecimal("10.00"), VehicleStatus.PARKED);

        vehicleRepository.save(vehicle1);
        vehicleRepository.save(vehicle2);

        List<Vehicle> result = vehicleRepository.findByStatus(VehicleStatus.EXITED);

        assertEquals(1, result.size());
        assertEquals("ABC1234", result.get(0).getLicensePlate());
    }
}
