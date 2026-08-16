package com.mycompany.app.repository;

import java.util.List;
import com.mycompany.app.model.Team;
import com.mycompany.app.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
   //List<Match> findByAwayTeam(Team team);
   @Query("SELECT m from Match m where m.homeTeam = :team OR m.awayTeam = :team ORDER BY m.time ASC")
   List<Match> findNextMatchesForTeam(@Param("team") Team team);
}