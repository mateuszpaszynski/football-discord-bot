package pl.mateuszpaszynski.footballtracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.mateuszpaszynski.footballtracker.model.Team;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    
    List<Team> findByTla(String query);
    List<Team> findByName(String query);
    List<Team> findByShortName(String query);
}