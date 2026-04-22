package br.com.teste.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import br.com.teste.dto.GarageConfigResponse;
import br.com.teste.exception.GarageConfigException;
import br.com.teste.model.ParkingSpot;
import br.com.teste.model.Sector;
import br.com.teste.repository.ParkingSpotRepository;
import br.com.teste.repository.SectorRepository;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GarageConfigService {
    private static final Logger logger = LoggerFactory.getLogger(GarageConfigService.class);

    private final SectorRepository sectorRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final RestTemplate restTemplate;
    private final String simulatorUrl;
    private final String garageEndpoint;

    public GarageConfigService(
            SectorRepository sectorRepository,
            ParkingSpotRepository parkingSpotRepository,
            RestTemplate restTemplate,
            @Value("${simulator.url}") String simulatorUrl,
            @Value("${simulator.garage-endpoint}") String garageEndpoint) {
        this.sectorRepository = sectorRepository;
        this.parkingSpotRepository = parkingSpotRepository;
        this.restTemplate = restTemplate;
        this.simulatorUrl = simulatorUrl;
        this.garageEndpoint = garageEndpoint;
    }

    /**
     * Initialize garage configuration from simulator
     */
    public void initializeFromSimulator() {
        try {
            String url = simulatorUrl + garageEndpoint;
            logger.info("Fetching garage configuration from: {}", url);

            GarageConfigResponse config = restTemplate.getForObject(url, GarageConfigResponse.class);
            if (config == null || config.sectors() == null) {
                throw new GarageConfigException("Invalid garage configuration received from simulator");
            }

            // Save sectors
            for (GarageConfigResponse.SectorConfig sectorConfig : config.sectors()) {
                saveSector(sectorConfig);
            }

            // Save parking spots
            if (config.spots() != null) {
                for (GarageConfigResponse.SpotConfig spotConfig : config.spots()) {
                    try {
                        Sector sector = getSectorByName(spotConfig.sector());
                        ParkingSpot spot = new ParkingSpot(
                            sector,
                            spotConfig.latitude(),
                            spotConfig.longitude()
                        );
                        parkingSpotRepository.save(spot);
                    } catch (Exception ex) {
                        logger.debug("Failed to save spot for sector {}: {}", spotConfig.sector(), ex.getMessage());
                    }
                }
                logger.debug("Saved {} parking spots", config.spots().size());
            }

            logger.info("Garage configuration initialized successfully with {} sectors",
                config.sectors().size());
        } catch (ResourceAccessException ex) {
            throw new GarageConfigException("Cannot connect to simulator at " + simulatorUrl + ". Make sure the simulator is running and accessible.", ex);
        } catch (HttpServerErrorException ex) {
            throw new GarageConfigException("Simulator returned an error (HTTP " + ex.getStatusCode() + "): " + ex.getMessage(), ex);
        } catch (HttpClientErrorException ex) {
            throw new GarageConfigException("Invalid request to simulator (HTTP " + ex.getStatusCode() + "): " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new GarageConfigException("Failed to initialize garage configuration from simulator: " + ex.getMessage(), ex);
        }
    }

    /**
     * Save or update sector configuration
     */
    private Sector saveSector(GarageConfigResponse.SectorConfig sectorConfig) {
        Optional<Sector> existing = sectorRepository.findByName(sectorConfig.name());

        if (existing.isPresent()) {
            Sector sector = existing.get();
            sector.setBasePrice(sectorConfig.basePrice());
            sector.setMaxCapacity(sectorConfig.maxCapacity());
            return sectorRepository.save(sector);
        } else {
            Sector newSector = new Sector(
                sectorConfig.name(),
                sectorConfig.basePrice(),
                sectorConfig.maxCapacity()
            );
            return sectorRepository.save(newSector);
        }
    }


    /**
     * Get sector by name (cached with Spring Cache abstraction)
     */
    @Cacheable(value = "sectors", key = "#name")
    public Sector getSectorByName(String name) {
        Sector sector = sectorRepository.findByName(name)
            .orElseThrow(() -> new GarageConfigException("Sector not found: " + name));

        logger.debug("Sector cache miss for: {}", name);
        return sector;
    }

    /**
     * Clear sector cache
     */
    @CacheEvict(value = "sectors", allEntries = true)
    public void clearCache() {
        logger.debug("Cache cleared for all sectors");
    }

    /**
     * Get current occupancy for a sector
     */
    public int getOccupiedCount(Sector sector) {
        return parkingSpotRepository.countOccupiedInSector(sector);
    }

    /**
     * Get total available spots in a sector
     */
    public int getAvailableCount(Sector sector) {
        int occupied = getOccupiedCount(sector);
        return sector.getMaxCapacity() - occupied;
    }
}
