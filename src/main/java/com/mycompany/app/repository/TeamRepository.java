package com.mycompany.app.repository;

import java.util.Optional;
import com.mycompany.app.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    
    List<Team> findByTla(String query);
    List<Team> findByName(String query);
    List<Team> findByShortName(String query);
}