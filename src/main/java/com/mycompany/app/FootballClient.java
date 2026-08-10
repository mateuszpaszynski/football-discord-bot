package com.mycompany.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FootballClient {
    @Value("${football.api}")
    private String footballApi;

    public String fetchFixtures() {
        RestClient footballClient = RestClient.builder()
            .baseUrl("https://api.football-data.org/v4")
            .defaultHeader("X-Auth-Token", footballApi)
            .build();
        
        String jsonResponse = footballClient.get()
        .uri("/teams/86/matches")
        .retrieve()
        .body(String.class);

        return jsonResponse;
    }
}
