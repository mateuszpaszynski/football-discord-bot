package com.mycompany.app.repository;

import java.util.List;

import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Standing;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface StandingRepository extends JpaRepository<Standing,Long>{
    List<Standing> findByCompetitionOrderByPositionAsc(Competition competition);

    // Nowa metoda do czyszczenia bazy przed updatem
    @Transactional
    void deleteByCompetition(Competition competition);
}
