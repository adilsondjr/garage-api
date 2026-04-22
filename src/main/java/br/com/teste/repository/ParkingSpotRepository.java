package br.com.teste.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import br.com.teste.model.ParkingSpot;
import br.com.teste.model.Sector;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    Optional<ParkingSpot> findFirstBySectorAndIsOccupiedOrderById(Sector sector, Boolean isOccupied);

    @Query("SELECT COUNT(ps) FROM ParkingSpot ps WHERE ps.sector = :sector AND ps.isOccupied = true")
    Integer countOccupiedInSector(@Param("sector") Sector sector);

    List<ParkingSpot> findBySector(Sector sector);

    List<ParkingSpot> findBySectorAndIsOccupied(Sector sector, Boolean isOccupied);

    @Query("SELECT DISTINCT ps FROM ParkingSpot ps LEFT JOIN FETCH ps.sector")
    List<ParkingSpot> findAllWithSector();

    @Query("SELECT s.name, COUNT(ps) FROM Sector s " +
           "LEFT JOIN ParkingSpot ps ON ps.sector.id = s.id AND ps.isOccupied = true " +
           "GROUP BY s.id, s.name " +
           "ORDER BY s.name")
    List<Object[]> findOccupancyData();
}
