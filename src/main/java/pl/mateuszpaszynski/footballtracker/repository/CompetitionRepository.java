package pl.mateuszpaszynski.footballtracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.mateuszpaszynski.footballtracker.model.Competition;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    
    Optional<Competition> findByCode(String code);
    Optional<Competition> findByName(String code);
}