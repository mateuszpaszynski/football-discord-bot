package com.mycompany.app.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

@Service    
public class FootballApiClient {
    
    private final String BASE_URL = "https://api.football-data.org";
    private final String HEADER = "X-Auth-Token";

    private final RestClient restClient;

    public FootballApiClient(    @Value("${football.api}") String footballApi) {
        this.restClient = RestClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader(HEADER, footballApi)
            .build();
    }
    public JsonNode fetchRawFixtures(String leagueCode) {
        try {
            String URI = "/v4/competitions/" + leagueCode + "/matches";
            JsonNode rootNode = restClient.get()
                .uri(URI)
                .retrieve()
                .body(JsonNode.class)
                .get("matches");
            
            return rootNode; 
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch fixtures for " + leagueCode, e);
            }
        }

    public JsonNode fetchRawStandings(String leagueCode) {
        try {
            String URI = "/v4/competitions/" + leagueCode + "/standings";
            JsonNode rootNode = restClient.get()
            .uri(URI)
            .retrieve()
            .body(JsonNode.class)
            .get("standings");
            
            return rootNode;
        
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch standings for " + leagueCode, e);
        }
    }

    public JsonNode fetchRawTeams(String leagueCode) {

        try {
            String URI = "/v4/competitions/" + leagueCode + "/teams";
            JsonNode rootNode = restClient.get()
            .uri(URI)
            .retrieve()
            .body(JsonNode.class)
            .get("teams");

            return rootNode;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch teams for " + leagueCode, e);
        }
    }    

    public JsonNode fetchRawCompetitions() {

        try {
            JsonNode rootNode = restClient.get()
            .uri("/v4/competitions?areas=2077") //2077 for Europe
            .retrieve()
            .body(JsonNode.class);

            return rootNode.get("competitions");
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch competitions ", e);
        }
    }
}
