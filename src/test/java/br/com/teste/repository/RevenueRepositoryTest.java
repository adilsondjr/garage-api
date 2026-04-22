package br.com.teste.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import br.com.teste.model.Revenue;
import br.com.teste.model.Sector;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class RevenueRepositoryTest {

    @Autowired
    private RevenueRepository revenueRepository;

    @Autowired
    private SectorRepository sectorRepository;

    private Sector testSector;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testSector = new Sector("A", new BigDecimal("10.00"), 100);
        testSector = sectorRepository.save(testSector);
        testDate = LocalDate.now();
    }

    @Test
    void testFindBySectorAndDate() {
        Revenue revenue = new Revenue(testSector, testDate);
        revenue.setTotalAmount(new BigDecimal("150.00"));
        revenue.setVehicleCount(3);
        revenueRepository.save(revenue);

        Optional<Revenue> found = revenueRepository.findBySectorAndDate(testSector, testDate);

        assertTrue(found.isPresent());
        assertEquals(new BigDecimal("150.00"), found.get().getTotalAmount());
        assertEquals(3, found.get().getVehicleCount());
    }

    @Test
    void testFindBySectorAndDate_notFound() {
        LocalDate differentDate = testDate.minusDays(1);

        Optional<Revenue> found = revenueRepository.findBySectorAndDate(testSector, differentDate);

        assertTrue(found.isEmpty());
    }

    @Test
    void testUniquenessConstraint() {
        Revenue revenue1 = new Revenue(testSector, testDate);
        revenue1.setTotalAmount(new BigDecimal("100.00"));
        revenueRepository.save(revenue1);

        LocalDate nextDay = testDate.plusDays(1);
        Revenue revenue2 = new Revenue(testSector, nextDay);
        revenue2.setTotalAmount(new BigDecimal("200.00"));
        revenueRepository.save(revenue2);

        Optional<Revenue> found1 = revenueRepository.findBySectorAndDate(testSector, testDate);
        Optional<Revenue> found2 = revenueRepository.findBySectorAndDate(testSector, nextDay);

        assertTrue(found1.isPresent());
        assertTrue(found2.isPresent());
        assertEquals(new BigDecimal("100.00"), found1.get().getTotalAmount());
        assertEquals(new BigDecimal("200.00"), found2.get().getTotalAmount());
    }
}
