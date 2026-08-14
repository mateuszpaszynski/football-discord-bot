package com.mycompany.app.repository;

import java.util.Optional;
import com.mycompany.app.model.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    
    Optional<Competition> findByCode(String code);
}