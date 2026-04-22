package br.com.teste.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.teste.model.Revenue;
import br.com.teste.model.Sector;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RevenueRepository extends JpaRepository<Revenue, Long> {
    Optional<Revenue> findBySectorAndDate(Sector sector, LocalDate date);
}
