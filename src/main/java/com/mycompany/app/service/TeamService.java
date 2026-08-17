package com.mycompany.app.service;

import java.util.List;

import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Standing;
import com.mycompany.app.model.Team;
import com.mycompany.app.repository.TeamRepository;

import org.springframework.stereotype.Service;

@Service
public class TeamService {

        private final TeamRepository teamRepository;
        private final StandingService standingService;
        
        public TeamService(TeamRepository teamRepository, StandingService standingService) {
            this.teamRepository = teamRepository;
            this.standingService = standingService;
        }

        public List<Team> getTeam(String query) {
        
            List<Team> teamsByTla = teamRepository.findByTla(query.toUpperCase());
            if (!teamsByTla.isEmpty()) return teamsByTla;

            List<Team> teamsByName = teamRepository.findByShortName(query);
            if (!teamsByName.isEmpty()) return teamsByName;

            if (query.matches("\\d+")) {
                return teamRepository.findById(Long.valueOf(query))
                        .map(List::of) 
                        .orElseThrow(() -> new IllegalArgumentException("Team ID " + query + " not found"));
            }
            throw new IllegalArgumentException("Team '" + query + "' not found");
        }

        public List<Team> getTeams(Competition league) {
            
            return standingService.getStandings(league).stream()
            .map(Standing::getTeam)
            .distinct()
            .toList();
        }
}