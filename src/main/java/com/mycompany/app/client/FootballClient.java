package com.mycompany.app.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FootballClient {
    @Value("${football.api}")
    private String footballApi;

    public String fetchFixtures() {

        RestClient footballClient = RestClient.builder()
            .baseUrl("https://api.football-data.org")
            .defaultHeader("X-Auth-Token", footballApi)
            .build();
        
        JsonNode rootNode = footballClient.get()
            .uri("/v4/teams/86/matches?status=SCHEDULED")
            .retrieve()
            .body(JsonNode.class);
        
        StringBuilder discordMessage = new StringBuilder();
        discordMessage.append("Incoming Real Madrid matches:\n\n");

        JsonNode matches = rootNode.get("matches");
        int count = 0;
        for (JsonNode match : matches) {
            if (count >= 5) {
                break;
            }
            String date = match.get("utcDate").asText();
            String homeTeam = match.get("homeTeam").get("name").asText();
            String awayTeam = match.get("awayTeam").get("name").asText();
            
            discordMessage.append("📅 ").append(date).append("\n");
            discordMessage.append("🏟️ ").append(homeTeam).append(" vs ").append(awayTeam).append("\n\n");
            count++;
        }
        
        return discordMessage.toString();
    }
}
