package com.mycompany.app.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Match;
import com.mycompany.app.model.Standing;
import com.mycompany.app.model.Team;
import com.mycompany.app.repository.CompetitionRepository;
import com.mycompany.app.repository.MatchRepository;
import com.mycompany.app.repository.StandingRepository;
import com.mycompany.app.repository.TeamRepository;

@Service
public class FootballClient {
    @Value("${football.api}")
    private String footballApi;

    private final String BASE_URL = "https://api.football-data.org";
    private final String HEADER = "X-Auth-Token";

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final CompetitionRepository competitionRepository;
    private final StandingRepository standingRepository;
    public FootballClient(TeamRepository teamRepository, MatchRepository matchRepository, CompetitionRepository competitionRepository, StandingRepository standingRepository) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.competitionRepository = competitionRepository;
        this.standingRepository = standingRepository;
    }
    public void fetchFixtures() {

        RestClient footballClient = RestClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader(HEADER, footballApi)
            .build();
        
            List<Team> allTeams = teamRepository.findAll();

            for (Team team : allTeams) {

                Long teamId = team.getId();
                
                try {
                    String URI = "/v4/teams/" + teamId.toString() + "/matches?status=SCHEDULED";
                
                    JsonNode rootNode = footballClient.get()
                    .uri(URI)
                    .retrieve()
                    .body(JsonNode.class);
                    
                    JsonNode matchesNode = rootNode.get("matches");

                    for (JsonNode matchJson : matchesNode) {
                    
                        JsonNode homeTeamNode = matchJson.get("homeTeam");
                        Long homeTeamId = homeTeamNode.get("id").asLong();
                        String homeTeamName = homeTeamNode.get("shortName").asText();

                        Team homeTeam = teamRepository.findById(homeTeamId)
                            .orElseGet(() -> teamRepository.save(new Team(homeTeamId, homeTeamName)));

                        JsonNode awayTeamNode = matchJson.get("awayTeam");
                        Long awayTeamId = awayTeamNode.get("id").asLong();
                        String awayTeamName = awayTeamNode.get("shortName").asText();
                        
                        Team awayTeam = teamRepository.findById(awayTeamId)
                            .orElseGet(() -> teamRepository.save(new Team(awayTeamId, awayTeamName)));

                        Long matchId = matchJson.get("id").asLong();
                        String utcDate = matchJson.get("utcDate").asText();
                        String status = matchJson.get("status").asText();
                        
                        Match match = new Match(matchId, 2014L, utcDate, status, homeTeam, awayTeam, "TBD");

                        matchRepository.save(match);
                    }
                }
                
                catch (Exception e) {
                    System.err.println("Error with fetching matches for team " + teamId + " cause " + e.getMessage());
                }
                System.out.println("Saved matches. 6.5 seconds of sleep for obeying rate limiting");
                try {
                    Thread.sleep(6500); 
                } catch (InterruptedException e) {
                    
                    Thread.currentThread().interrupt();    
                }
            }
    }
    public List<Match> getMatches() {
        List<Match> allMatches = matchRepository.findAll();
        return allMatches;
    }
    public Competition getCompetition(String code) {
        return competitionRepository.findByCode(code)
        .orElseThrow(() -> new IllegalArgumentException("League with code " + code + " not found"));
    }
    public void fetchStandings() {
        RestClient footballClient = RestClient.builder()
        .baseUrl(BASE_URL)
        .defaultHeader(HEADER,footballApi)
        .build();

        List<Competition> allComps = getCompetitions();
        
        for (Competition competition : allComps) {
            
            if (competition.getType().equals("LEAGUE")) {
                standingRepository.deleteByCompetition(competition);
                String URI = "/v4/competitions/" + competition.getId().toString() + "/standings";
                JsonNode rootNode = footballClient.get()
                .uri(URI)
                .retrieve()
                .body(JsonNode.class);
                
                JsonNode standingsNode = rootNode.get("standings");
                for (JsonNode standing : standingsNode) {
                    if (standing.get("type").asText().equals("TOTAL")) {
                        JsonNode table = standing.get("table");
                        for (JsonNode tableNode : table) {
                            Integer position = tableNode.get("position").asInt();
                            JsonNode teamNode = tableNode.get("team");
                            Long teamId = teamNode.get("id").asLong();
                            String teamName = teamNode.get("shortName").asText();
                            
                            Team team = teamRepository.findById(teamId)
                            .orElseGet(() -> teamRepository.save(new Team(teamId, teamName)));
                            
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

    public List<Standing> getStandings(Competition competition) {
        return standingRepository.findByCompetitionOrderByPositionAsc(competition);
    }
    public void fetchTeams() {
        RestClient footballClient = RestClient.builder()
        .baseUrl(BASE_URL)
        .defaultHeader(HEADER,footballApi)
        .build();
        
        List<Competition> allComps = competitionRepository.findAll();
        for (Competition comp : allComps) {
            Long compId = comp.getId();

            String URI = "/v4/competitions/" + compId.toString() + "/teams";
            JsonNode rootNode = footballClient.get()
            .uri(URI)
            .retrieve()
            .body(JsonNode.class);
            
            JsonNode teamsNode = rootNode.get("teams");

            for (JsonNode teamNode : teamsNode) {
                Long teamId = teamNode.get("id").asLong();
                String name = teamNode.get("name").asText();
                Team team = new Team(teamId,name);
                teamRepository.save(team);
            }

        }
    }    
    public List<Team> getTeams() {
        List<Team> allTeams = teamRepository.findAll();
        return allTeams;
    }
    
    public void fetchCompetitions() {
        RestClient footballClient = RestClient.builder()
        .baseUrl(BASE_URL)
        .defaultHeader(HEADER,footballApi)
        .build();

        JsonNode rootNode = footballClient.get()
        .uri("/v4/competitions?areas=2077") //2077 for Europe
        .retrieve()
        .body(JsonNode.class);

        JsonNode competitionsNode = rootNode.get("competitions");

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
    public List<Competition> getCompetitions() {
        List<Competition> allComps = competitionRepository.findAll();
        return allComps;
    }
}
