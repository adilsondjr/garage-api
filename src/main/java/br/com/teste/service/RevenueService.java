package br.com.teste.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import br.com.teste.model.Revenue;
import br.com.teste.model.Sector;
import br.com.teste.repository.RevenueRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class RevenueService {
    private static final Logger logger = LoggerFactory.getLogger(RevenueService.class);

    private final RevenueRepository revenueRepository;

    public RevenueService(RevenueRepository revenueRepository) {
        this.revenueRepository = revenueRepository;
    }

    /**
     * Get or create revenue record for sector and date
     */
    public Revenue getOrCreateRevenue(Sector sector, LocalDate date) {
        Optional<Revenue> existing = revenueRepository.findBySectorAndDate(sector, date);

        if (existing.isPresent()) {
            return existing.get();
        }

        Revenue newRevenue = new Revenue(sector, date);
        return revenueRepository.save(newRevenue);
    }

    /**
     * Update revenue with a new vehicle charge (financial transaction audit)
     */
    public void addVehicleCharge(Sector sector, LocalDate date, BigDecimal charge) {
        Revenue revenue = getOrCreateRevenue(sector, date);

        BigDecimal oldTotal = revenue.getTotalAmount();
        revenue.setTotalAmount(revenue.getTotalAmount().add(charge));
        revenue.setVehicleCount(revenue.getVehicleCount() + 1);

        revenueRepository.save(revenue);

        logger.info("Revenue updated - Sector: {}, Date: {}, Charge: {}, Old Total: {}, New Total: {}, Vehicle Count: {}",
            sector.getName(), date, charge, oldTotal, revenue.getTotalAmount(), revenue.getVehicleCount());
    }

    /**
     * Get revenue for a specific sector and date
     */
    public Optional<Revenue> getRevenue(Sector sector, LocalDate date) {
        return revenueRepository.findBySectorAndDate(sector, date);
    }
}
