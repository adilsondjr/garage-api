package br.com.teste.service;

import org.springframework.stereotype.Service;
import br.com.teste.model.Sector;
import br.com.teste.repository.ParkingSpotRepository;
import br.com.teste.repository.SectorRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OccupancyService {
    private final ParkingSpotRepository parkingSpotRepository;
    private final SectorRepository sectorRepository;

    public OccupancyService(ParkingSpotRepository parkingSpotRepository, SectorRepository sectorRepository) {
        this.parkingSpotRepository = parkingSpotRepository;
        this.sectorRepository = sectorRepository;
    }

    /**
     * Get occupancy percentage for a specific sector
     */
    public double getOccupancyPercentage(Sector sector) {
        if (sector == null || sector.getMaxCapacity() == 0) {
            return 0.0;
        }
        Integer occupied = parkingSpotRepository.countOccupiedInSector(sector);
        return (double) occupied / sector.getMaxCapacity() * 100;
    }

    /**
     * Get occupancy information for all sectors
     * Uses aggregated query to prevent N+1 queries
     */
    public Map<String, Object> getAllOccupancy() {
        Map<String, Object> occupancyMap = new HashMap<>();

        // Single aggregated query instead of N+1
        List<Object[]> occupancyData = parkingSpotRepository.findOccupancyData();
        Map<String, Sector> sectorMap = new HashMap<>();
        sectorRepository.findAll().forEach(s -> sectorMap.put(s.getName(), s));

        for (Object[] row : occupancyData) {
            String sectorName = (String) row[0];
            Long occupied = (Long) row[1];
            Sector sector = sectorMap.get(sectorName);

            if (sector != null) {
                int total = sector.getMaxCapacity();
                double percentage = (double) occupied / total * 100;

                Map<String, Object> sectorInfo = new HashMap<>();
                sectorInfo.put("occupied", occupied.intValue());
                sectorInfo.put("total", total);
                sectorInfo.put("available", total - occupied.intValue());
                sectorInfo.put("percentage", Math.round(percentage * 100.0) / 100.0);

                occupancyMap.put(sectorName, sectorInfo);
            }
        }

        return occupancyMap;
    }
}
