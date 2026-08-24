package com.mycompany.app.service;

import java.util.List;

import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Match;
import com.mycompany.app.model.Team;
import com.mycompany.app.repository.MatchRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }
    public List<Match> getMatches(Competition competition) {
        return matchRepository.findNextMatchesForCompetition(competition,PageRequest.of(0,5));
    }

    public List<Match> getMatches(Team team) {
        return matchRepository.findNextMatchesForTeam(team,PageRequest.of(0, 5));
    }
}