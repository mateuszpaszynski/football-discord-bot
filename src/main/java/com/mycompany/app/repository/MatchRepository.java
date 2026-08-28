package com.mycompany.app.repository;

import java.util.List;
import com.mycompany.app.model.Team;
import com.mycompany.app.model.Match;
import com.mycompany.app.model.Competition;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
   //List<Match> findByAwayTeam(Team team);
   @Query("SELECT m from Match m where (m.status = 'TIMED' OR m.status = 'SCHEDULED') and (m.homeTeam = :team OR m.awayTeam = :team) ORDER BY m.time ASC")
   List<Match> findNextMatchesForTeam(@Param("team") Team team, Pageable pageable);

   @Query("SELECT m from Match m where (m.status = 'TIMED' or m.status = 'SCHEDULED') and m.competition = :competition ORDER BY m.time ASC")
   List<Match> findNextMatchesForCompetition(@Param("competition") Competition competition, Pageable pageable);

   @Query("SELECT m from Match m where m.status = 'FINISHED' and (m.homeTeam = :team OR m.awayTeam = :team) ORDER BY m.time DESC")
   List<Match> findLastMatchesForTeam(@Param("team") Team team, Pageable pageable);

   @Query("SELECT m from Match m where m.status = 'FINISHED' and m.competition = :competition ORDER BY m.time DESC")
   List<Match> findLastMatchesForCompetition(@Param("competition") Competition competition, Pageable pageable);

   @Query("SELECT m FROM Match m WHERE m.status IN ('TIMED', 'SCHEDULED') AND m.time >= :startOfDay AND m.time <= :endOfDay ORDER BY m.time ASC")
   List<Match> findTodayMatches(@Param("startOfDay") String startOfDay, @Param("endOfDay") String endOfDay);

   @Query("SELECT m FROM Match m WHERE m.status = 'FINISHED' AND m.time >= :yesterday AND m.time <= :now ORDER BY m.time DESC LIMIT 15")
   List<Match> findRecentFinishedMatches(@Param("yesterday") String yesterday, @Param("now") String now);

   @Query("SELECT m FROM Match m WHERE m.status IN ('LIVE', 'IN_PLAY', 'PAUSED') ORDER BY m.time ASC")
   List<Match> findLiveMatches();
}