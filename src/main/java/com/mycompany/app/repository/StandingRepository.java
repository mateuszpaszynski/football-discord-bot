package com.mycompany.app.repository;

import java.util.List;
import java.util.Optional;

import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Standing;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface StandingRepository extends JpaRepository<Standing,Long>{
    List<Standing> findByCompetitionOrderByPositionAsc(Competition competition);

    Optional<Standing> findByTeamIdAndCompetitionId(Long teamId, Long competitionId);
    
    @Transactional
    void deleteByCompetition(Competition competition);
}
