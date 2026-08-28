package com.mycompany.app.service;

import java.util.Collections;
import java.util.List;

import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Standing;
import com.mycompany.app.model.Team;
import com.mycompany.app.repository.TeamRepository;

import java.util.Optional;
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

            List<Team> teamsByShortName = teamRepository.findByShortName(query);
            if (!teamsByShortName.isEmpty()) return teamsByShortName;

            List<Team> teamsByName = teamRepository.findByName(query);
            if (!teamsByName.isEmpty()) return teamsByName;

            if (query.matches("\\d+")) {
               Optional<Team> optionalTeam = teamRepository.findById(Long.valueOf(query));
               if (optionalTeam.isEmpty()) {
                    return Collections.emptyList();
               }
               return List.of(optionalTeam.get());
            }
            return Collections.emptyList();
        }
        public List<Team> getTeams(Competition league) {
            
            return standingService.getStandings(league).stream()
            .map(Standing::getTeam)
            .distinct()
            .toList();
        }
}