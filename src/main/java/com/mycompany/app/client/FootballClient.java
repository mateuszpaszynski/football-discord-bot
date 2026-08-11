package com.mycompany.app.client;

import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.app.repository.TeamRepository;
import com.mycompany.app.model.Team;
import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Match;
import com.mycompany.app.repository.CompetitionRepository;
import com.mycompany.app.repository.MatchRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FootballClient {
    @Value("${football.api}")
    private String footballApi;

    private final String BASE_URL = "https://api.football-data.org";
    private final String HEADER = "X-Auth-Token";

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final CompetitionRepository competitionRepository;

    public FootballClient(TeamRepository teamRepository, MatchRepository matchRepository, CompetitionRepository competitionRepository) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.competitionRepository = competitionRepository;
    }
    public void fetchFixtures() {

        RestClient footballClient = RestClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader(HEADER, footballApi)
            .build();
        
        JsonNode rootNode = footballClient.get()
            .uri("/v4/teams/86/matches?status=SCHEDULED")
            .retrieve()
            .body(JsonNode.class);
        
        JsonNode matchesNode = rootNode.get("matches");
        int savedCount = 0;

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
            savedCount++;
        }
        
    }
    
    public String fetchTeams() {
        List<Team> allTeams = teamRepository.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("Teams from LaLiga: \n");

        for (Team team : allTeams) {
            sb.append(team.getName());
            sb.append("\n");
        }
        return sb.toString();
    }
    public void fetchCompetitions() {
        RestClient footballClient = RestClient.builder()
        .baseUrl(BASE_URL)
        .defaultHeader(HEADER,footballApi)
        .build();

        JsonNode rootNode = footballClient.get()
        .uri("/v4/competitions?areas=2077")
        .retrieve()
        .body(JsonNode.class);

        JsonNode competitionsNode = rootNode.get("competitions");

        for (JsonNode competition : competitionsNode) {
            Long competitionId = competition.get("id").asLong();
            String name = competition.get("name").asText();
            String type = competition.get("type").asText();
            
            Competition comp = new Competition(competitionId,name,type);
            competitionRepository.save(comp);
        }
        System.out.println("Success");
    }
}
