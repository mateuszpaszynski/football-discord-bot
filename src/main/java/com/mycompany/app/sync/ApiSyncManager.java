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

@Service
public class ApiSyncManager {


    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final CompetitionRepository competitionRepository;
    private final StandingRepository standingRepository;
    
    private final CompetitionService competitionService;
    
    private final FootballApiClient footballApiClient;

    private final RateLimitManager rateLimitManager;

    public ApiSyncManager(TeamRepository teamRepository, MatchRepository matchRepository, CompetitionRepository competitionRepository, StandingRepository standingRepository, 
                         CompetitionService competitionService, FootballApiClient footballApiClient, RateLimitManager rateLimitManager
    ) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.competitionRepository = competitionRepository;
        this.standingRepository = standingRepository;

        this.competitionService = competitionService;

        this.footballApiClient = footballApiClient;

        this.rateLimitManager = rateLimitManager;
    }

public void fetchFixtures() {
        List<Competition> competitions = competitionService.getCompetitions();
        for (Competition competition : competitions) {
            String leagueCode = competition.getCode();
            
            try {
                JsonNode matchesNode = footballApiClient.fetchRawFixtures(leagueCode);
                rateLimitManager.apply(); 

                for (JsonNode matchJson : matchesNode) {
                    try {
                        if (!matchJson.hasNonNull("homeTeam") || !matchJson.get("homeTeam").hasNonNull("id") || !matchJson.get("homeTeam").hasNonNull("name") // Poprawione name
                         || !matchJson.hasNonNull("awayTeam") || !matchJson.get("awayTeam").hasNonNull("id") || !matchJson.get("awayTeam").hasNonNull("name")
                         || !matchJson.hasNonNull("id") || !matchJson.hasNonNull("status") 
                         || (matchJson.get("status").asText().equals("FINISHED") && 
                            (!matchJson.hasNonNull("score") || !matchJson.get("score").hasNonNull("fullTime") || !matchJson.get("score").get("fullTime").hasNonNull("home") || !matchJson.get("score").get("fullTime").hasNonNull("away")))
                        ) {
                            throw new IllegalArgumentException("Missing crucial data for match");
                        }

                        JsonNode homeTeamNode = matchJson.get("homeTeam");
                        Long homeTeamId = homeTeamNode.get("id").asLong();
                        String homeTeamName = homeTeamNode.get("name").asText();
                        String homeTeamShortName = homeTeamNode.hasNonNull("shortName") ? homeTeamNode.get("shortName").asText() : null;
                        String homeTla = homeTeamNode.hasNonNull("tla") ? homeTeamNode.get("tla").asText() : null;
                        Team homeTeam = teamRepository.findById(homeTeamId)
                            .orElseGet(() -> teamRepository.save(new Team(homeTeamId, homeTeamName, homeTeamShortName, homeTla)));

                        JsonNode awayTeamNode = matchJson.get("awayTeam");
                        Long awayTeamId = awayTeamNode.get("id").asLong();
                        String awayTeamName = awayTeamNode.get("name").asText(); 
                        String awayTeamShortName = awayTeamNode.hasNonNull("shortName") ? awayTeamNode.get("shortName").asText() : null;
                        String awayTeamTla = awayTeamNode.hasNonNull("tla") ? awayTeamNode.get("tla").asText() : null;
                        Team awayTeam = teamRepository.findById(awayTeamId)
                            .orElseGet(() -> teamRepository.save(new Team(awayTeamId, awayTeamName, awayTeamShortName, awayTeamTla)));

                        Long matchId = matchJson.get("id").asLong();
                        String utcDate = matchJson.get("utcDate").asText();
                        String status = matchJson.get("status").asText();
                        
                        String score = "TBD";
                        if (status.equals("FINISHED") || status.equals("LIVE") || status.equals("PAUSED") || status.equals("IN_PLAY")) {
                            JsonNode scoreNode = matchJson.get("score");
                            String homeGoals = scoreNode.get("fullTime").get("home").asText();
                            String awayGoals = scoreNode.get("fullTime").get("away").asText();
                            score = homeGoals + " - " + awayGoals;
                        }
                        Match match = new Match(matchId, competition, utcDate, status, homeTeam, awayTeam, score);
                        matchRepository.save(match);
                        
                    } catch (Exception e) {
                        System.err.println("Discarded match in league " + leagueCode + ": " + e.getMessage());
                    }
                }
                System.out.println("Saved all matches form league: " + leagueCode);
                
            } catch (Exception e) {
                System.err.println("Critical error fetching matches for competition " + leagueCode + " cause " + e.getMessage());
            } 
        }
    }

    public void fetchStandings() {

        List<Competition> allComps = competitionService.getCompetitions();
        
        for (Competition competition : allComps) {
            if (competition.getType().equals("LEAGUE")) {
                
                JsonNode standingsNode = footballApiClient.fetchRawStandings(competition.getId().toString());
                rateLimitManager.apply();
                for (JsonNode standing : standingsNode) {

                    if (standing.get("type").asText().equals("TOTAL")) {
                        JsonNode table = standing.get("table");
                        for (JsonNode tableNode : table) {
                            try {
                                if (!tableNode.hasNonNull("position") || !tableNode.hasNonNull("team") || !tableNode.get("team").hasNonNull("id") 
                                || !tableNode.get("team").hasNonNull("name") || !tableNode.hasNonNull("playedGames") || !tableNode.hasNonNull("won")
                                || !tableNode.hasNonNull("draw") || !tableNode.hasNonNull("lost") || !tableNode.hasNonNull("points") || !tableNode.hasNonNull("goalsFor")
                                || !tableNode.hasNonNull("goalsAgainst") || !tableNode.hasNonNull("goalDifference")) 
                                {
                                    throw new IllegalArgumentException("Missing crucial data");
                                }
                                Integer position = tableNode.get("position").asInt();
                                JsonNode teamNode = tableNode.get("team");
                                Long teamId = teamNode.get("id").asLong();
                                String teamName = teamNode.get("name").asText();
                                String teamShortName = teamNode.hasNonNull("shortName") ? teamNode.get("shortName").asText() : null;
                                String teamTla = teamNode.hasNonNull("tla") ? teamNode.get("tla").asText() : null;
                                Team team = teamRepository.findById(teamId)
                                .orElseGet(() -> teamRepository.save(new Team(teamId, teamName, teamShortName, teamTla)));
                                
                                String form = tableNode.hasNonNull("form") ? tableNode.get("form").asText() : null;
                                
                                Integer playedGames = tableNode.get("playedGames").asInt();
                                Integer gamesWon = tableNode.get("won").asInt();
                                Integer gamesDrawn = tableNode.get("draw").asInt();
                                Integer gamesLost = tableNode.get("lost").asInt();
                                Integer points = tableNode.get("points").asInt();
                                Integer goalsFor = tableNode.get("goalsFor").asInt();
                                Integer goalsAgainst = tableNode.get("goalsAgainst").asInt();
                                Integer goalDifference = tableNode.get("goalDifference").asInt();

                                Standing existingStanding = standingRepository.findByTeamIdAndCompetitionId(teamId, competition.getId())
                                .orElse(new Standing());

                                existingStanding.setTeam(team);
                                existingStanding.setCompetition(competition);
                                existingStanding.setPosition(position);
                                existingStanding.setPlayedGames(playedGames);
                                existingStanding.setForm(form);
                                existingStanding.setGamesWon(gamesWon);
                                existingStanding.setGamesDrawn(gamesDrawn);
                                existingStanding.setGamesLost(gamesLost);
                                existingStanding.setPoints(points);
                                existingStanding.setGoalsFor(goalsFor);
                                existingStanding.setGoalsAgainst(goalsAgainst);
                                existingStanding.setGoalDifference(goalDifference);
                                standingRepository.save(existingStanding);    
                                    
                            } catch (Exception e) {
                                System.err.println("One team from league " + competition.getName() + " discarded, reason: " + e.getMessage());
                                
                            }
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
            rateLimitManager.apply();
            for (JsonNode teamNode : teamsNode) {
                try {
                    if (!teamNode.hasNonNull("id") || !teamNode.hasNonNull("name")) {
                        throw new IllegalArgumentException("Missing crucial data: ID or Name");
                    }
                    Long teamId = teamNode.get("id").asLong();
                    String name = teamNode.get("name").asText();

                    String shortName = teamNode.hasNonNull("shortName") ? teamNode.get("shortName").asText() : null;
                    String tla = teamNode.hasNonNull("tla") ? teamNode.get("tla").asText() : null;
                    
                    Team team = new Team(teamId,name,shortName, tla);
                    teamRepository.save(team);
                } 
                catch (Exception e) {
                    System.err.println("One team from league " + comp.getName() + " discarded, reason: " + e.getMessage());
                }
            }
        }
    }    
    public void fetchCompetitions() {
        
        JsonNode competitionsNode = footballApiClient.fetchRawCompetitions();
        rateLimitManager.apply();
        for (JsonNode competition : competitionsNode) {
            try {
                if (!competition.hasNonNull("id") || !competition.hasNonNull("name") ||
                 !competition.hasNonNull("code") || !competition.hasNonNull("type") ) {
                    throw new IllegalArgumentException("Missing crucial data: ID or Name");
                }
                Long competitionId = competition.get("id").asLong();
                String name = competition.get("name").asText();
                String code = competition.get("code").asText();
                String type = competition.get("type").asText();
                
                String country = "Unknown";
                if (competition.hasNonNull("area") && competition.get("area").hasNonNull("name")) {
                    country = competition.get("area").get("name").asText();
                }
                
                Competition comp = new Competition(competitionId, name, code, type, country);
                competitionRepository.save(comp);
                
            } catch (Exception e) {
                System.err.println("One league discarded, reason: " + e.getMessage());
            }
        }
    }
}
