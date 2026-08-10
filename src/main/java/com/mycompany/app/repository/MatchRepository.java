package com.mycompany.app.repository;

import com.mycompany.app.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Team, Long> {
   
}