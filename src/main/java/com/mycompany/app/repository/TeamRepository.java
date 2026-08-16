package com.mycompany.app.repository;

import java.util.Optional;
import com.mycompany.app.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
   
    Optional<Team> findByTla(String query);
    Optional<Team> findByName(String query);
    Optional<Team> findByShortName(String query);
}