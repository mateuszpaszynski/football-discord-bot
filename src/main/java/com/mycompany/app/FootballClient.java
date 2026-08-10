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
            .baseUrl("https://v3.football.api-sports.io")
            .defaultHeader("x-apisports-key", footballApi)
            .build();
        
        String jsonResponse = footballClient.get()
        .uri("/fixtures?next=5&team=137")
        .retrieve()
        .body(String.class);

        return jsonResponse;
    }
}
