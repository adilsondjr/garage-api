package br.com.teste.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import br.com.teste.model.Sector;
import br.com.teste.model.Vehicle;
import br.com.teste.model.VehicleStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    List<Vehicle> findByStatusAndSector(VehicleStatus status, Sector sector);

    List<Vehicle> findByStatus(VehicleStatus status);

    List<Vehicle> findBySectorAndExitTimeIsNotNullAndExitTimeGreaterThanEqual(
        Sector sector, LocalDateTime exitTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Vehicle v WHERE v.licensePlate = :licensePlate")
    Optional<Vehicle> findByLicensePlateForUpdate(@Param("licensePlate") String licensePlate);
}
