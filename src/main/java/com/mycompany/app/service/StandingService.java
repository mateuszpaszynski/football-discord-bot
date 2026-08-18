package com.mycompany.app.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Standing;
import com.mycompany.app.repository.StandingRepository;

@Service
public class StandingService {

    private final StandingRepository standingRepository;

    public StandingService(StandingRepository standingRepository) {
        this.standingRepository = standingRepository;
    }

    public List<Standing> getStandings(Competition competition) {
        return standingRepository.findByCompetitionOrderByPositionAsc(competition);
    }
}