package pl.mateuszpaszynski.footballtracker.repository;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import pl.mateuszpaszynski.footballtracker.model.Competition;
import pl.mateuszpaszynski.footballtracker.model.Standing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface StandingRepository extends JpaRepository<Standing,Long>{
    List<Standing> findByCompetitionOrderByPositionAsc(Competition competition);

    Optional<Standing> findByTeamIdAndCompetitionId(Long teamId, Long competitionId);
    
    @Transactional
    void deleteByCompetition(Competition competition);
}
