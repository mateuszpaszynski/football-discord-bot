package com.mycompany.app.sync;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.app.client.FootballApiClient;
import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Match;
import com.mycompany.app.model.Standing;
import com.mycompany.app.model.Team;
import com.mycompany.app.repository.CompetitionRepository;
import com.mycompany.app.repository.MatchRepository;
import com.mycompany.app.repository.StandingRepository;
import com.mycompany.app.repository.TeamRepository;
import com.mycompany.app.service.CompetitionService;
import com.mycompany.app.service.MatchService;
import com.mycompany.app.service.StandingService;
import com.mycompany.app.service.TeamService;

@Service
public class ApiSyncManager {


    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final CompetitionRepository competitionRepository;
    private final StandingRepository standingRepository;
    
    private final CompetitionService competitionService;
    
    private final FootballApiClient footballApiClient;

    public ApiSyncManager(TeamRepository teamRepository, MatchRepository matchRepository, CompetitionRepository competitionRepository, StandingRepository standingRepository, 
                         CompetitionService competitionService, FootballApiClient footballApiClient
    ) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.competitionRepository = competitionRepository;
        this.standingRepository = standingRepository;

        this.competitionService = competitionService;

        this.footballApiClient = footballApiClient;
    }



    public void fetchFixtures() {

        List<Competition> competitions = competitionService.getCompetitions();
        for (Competition competition : competitions) {

            String leagueCode = competition.getCode();
            try {

                JsonNode matchesNode = footballApiClient.fetchRawFixtures(leagueCode);

                for (JsonNode matchJson : matchesNode) {
                
                    JsonNode homeTeamNode = matchJson.get("homeTeam");
                    Long homeTeamId = homeTeamNode.get("id").asLong();
                    String homeTeamName = homeTeamNode.get("name").asText();
                    
                    String homeTeamShortName = homeTeamNode.hasNonNull("shortName") ? homeTeamNode.get("shortName").asText() : homeTeamName;
                    String rawHomeTla = homeTeamNode.hasNonNull("tla") ? homeTeamNode.get("tla").asText() : "N/A";
                    String homeTeamTla = rawHomeTla.equals("N/A") 
                            ? homeTeamName.substring(0, Math.min(3, homeTeamName.length())).toUpperCase() 
                            : rawHomeTla;

                    Team homeTeam = teamRepository.findById(homeTeamId)
                        .orElseGet(() -> teamRepository.save(new Team(homeTeamId, homeTeamName, homeTeamShortName, homeTeamTla)));


                    JsonNode awayTeamNode = matchJson.get("awayTeam");
                    Long awayTeamId = awayTeamNode.get("id").asLong();
                    
                    String awayTeamName = awayTeamNode.get("name").asText(); 
                    
                    String awayTeamShortName = awayTeamNode.hasNonNull("shortName") ? awayTeamNode.get("shortName").asText() : awayTeamName;
                    String rawAwayTla = awayTeamNode.hasNonNull("tla") ? awayTeamNode.get("tla").asText() : "N/A";
                    String awayTeamTla = rawAwayTla.equals("N/A") 
                            ? awayTeamName.substring(0, Math.min(3, awayTeamName.length())).toUpperCase() 
                            : rawAwayTla;

                    Team awayTeam = teamRepository.findById(awayTeamId)
                        .orElseGet(() -> teamRepository.save(new Team(awayTeamId, awayTeamName, awayTeamShortName, awayTeamTla)));

                    Long matchId = matchJson.get("id").asLong();
                    String utcDate = matchJson.get("utcDate").asText();
                    String status = matchJson.get("status").asText();
                    JsonNode scoreNode = matchJson.get("score");
                    String homeGoals = scoreNode.get("fullTime").get("home").asText();
                    String awayGoals = scoreNode.get("fullTime").get("away").asText();
                    String score = (status.equals("FINISHED")) ? (homeGoals + " - " + awayGoals) : "TBD";
                    Match match = new Match(matchId, competition, utcDate, status, homeTeam, awayTeam, score);
                    
                    matchRepository.save(match);
                }
                
                System.out.println("Saved all matches form league: " + leagueCode);
                
            } catch (Exception e) {
                System.err.println("Error with fetching matches for competition " + leagueCode + " cause " + e.getMessage());
            }
            
            System.out.println("6.5 seconds of sleep for obeying rate limiting...");
            try {
                Thread.sleep(6500); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();    
            }
        }
    }

    public void fetchStandings() {

        List<Competition> allComps = competitionService.getCompetitions();
        
        for (Competition competition : allComps) {
            
            if (competition.getType().equals("LEAGUE")) {
                
                JsonNode standingsNode = footballApiClient.fetchRawStandings(competition.getId().toString());
                
                for (JsonNode standing : standingsNode) {

                    if (standing.get("type").asText().equals("TOTAL")) {
                        JsonNode table = standing.get("table");
                        for (JsonNode tableNode : table) {
                            Integer position = tableNode.get("position").asInt();
                            JsonNode teamNode = tableNode.get("team");
                            Long teamId = teamNode.get("id").asLong();
                            String teamName = teamNode.get("name").asText();
                            String teamShortName = teamNode.get("shortName").asText();
                            String teamTla = teamNode.get("tla").asText();
                            Team team = teamRepository.findById(teamId)
                            .orElseGet(() -> teamRepository.save(new Team(teamId, teamName, teamShortName, teamTla)));
                            
                            Integer playedGames = tableNode.get("playedGames").asInt();
                            JsonNode formNode = tableNode.get("form");
                            String form = (formNode == null || formNode.isNull()) ? "    -    " : formNode.asText();
                            Integer gamesWon = tableNode.get("won").asInt();
                            Integer gamesDrawn = tableNode.get("draw").asInt();
                            Integer gamesLost = tableNode.get("lost").asInt();
                            Integer points = tableNode.get("points").asInt();
                            Integer goalsFor = tableNode.get("goalsFor").asInt();
                            Integer goalsAgainst = tableNode.get("goalsAgainst").asInt();
                            Integer goalDifference = tableNode.get("goalDifference").asInt();
                            standingRepository.save(new Standing(team,competition, position, playedGames, form, gamesWon, gamesDrawn, gamesLost, points, goalsFor, goalsAgainst, goalDifference));    
                        }
                    break;
                    }
                }
            }
        }
    }

    public void fetchTeams() {

        List<Competition> allComps = competitionService.getCompetitions();
        for (Competition comp : allComps) {
            String compId = comp.getId().toString();

            JsonNode teamsNode = footballApiClient.fetchRawTeams(compId);

            for (JsonNode teamNode : teamsNode) {

                Long teamId = teamNode.get("id").asLong();
                String name = teamNode.get("name").asText();
                String shortName = teamNode.get("shortName").asText();
                String tla = teamNode.get("tla").asText();
                Team team = new Team(teamId,name,shortName, tla);
                teamRepository.save(team);
            }
        }
    }    

    public void fetchCompetitions() {
        
        JsonNode competitionsNode = footballApiClient.fetchRawCompetitions();
        
        for (JsonNode competition : competitionsNode) {
            Long competitionId = competition.get("id").asLong();
            String name = competition.get("name").asText();
            String code = competition.get("code").asText();
            String type = competition.get("type").asText();
            JsonNode area = competition.get("area");
            String country = area.get("name").asText();
            Competition comp = new Competition(competitionId,name,code,type,country);
            competitionRepository.save(comp);
        }
    }
}
