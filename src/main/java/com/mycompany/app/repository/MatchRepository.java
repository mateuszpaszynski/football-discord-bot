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

   @Query("SELECT m FROM Match m WHERE (m.homeTeam = :team OR m.awayTeam = :team) " +
         "AND m.status IN ('SCHEDULED', 'TIMED') " +
         "AND m.time >= :now " +
         "ORDER BY m.time ASC")
   List<Match> findNextMatchesForTeam(@Param("team") Team team, Pageable pageable, @Param("now") String now);

   @Query("SELECT m FROM Match m WHERE m.competition = :comp " +
         "AND m.status IN ('SCHEDULED', 'TIMED') " +
         "AND m.time >= :now " +
         "ORDER BY m.time ASC")
   List<Match> findNextMatchesForCompetition(@Param("comp") Competition comp, Pageable pageable, @Param("now") String now);

   @Query("SELECT m FROM Match m WHERE (m.status = 'FINISHED' OR (m.status IN ('TIMED', 'SCHEDULED') AND m.time < :twoHoursAgo)) AND (m.homeTeam = :team OR m.awayTeam = :team) ORDER BY m.time DESC")
   List<Match> findLastMatchesForTeam(@Param("team") Team team, Pageable pageable, @Param("twoHoursAgo") String twoHoursAgo);

   @Query("SELECT m FROM Match m WHERE (m.status = 'FINISHED' OR (m.status IN ('TIMED', 'SCHEDULED') AND m.time < :twoHoursAgo)) AND m.competition = :competition ORDER BY m.time DESC")
   List<Match> findLastMatchesForCompetition(@Param("competition") Competition competition, Pageable pageable, @Param("twoHoursAgo") String twoHoursAgo);

   @Query("SELECT m FROM Match m WHERE m.status IN ('TIMED', 'SCHEDULED') AND m.time >= :startOfDay AND m.time <= :endOfDay ORDER BY m.time ASC")
   List<Match> findTodayMatches(@Param("startOfDay") String startOfDay, @Param("endOfDay") String endOfDay);

   @Query("SELECT m FROM Match m WHERE m.status = 'FINISHED' AND m.time >= :yesterday AND m.time <= :now ORDER BY m.time DESC LIMIT 15")
   List<Match> findRecentFinishedMatches(@Param("yesterday") String yesterday, @Param("now") String now);

   @Query("SELECT m FROM Match m WHERE m.status IN ('LIVE', 'IN_PLAY', 'PAUSED') ORDER BY m.time ASC")
   List<Match> findLiveMatches();
}