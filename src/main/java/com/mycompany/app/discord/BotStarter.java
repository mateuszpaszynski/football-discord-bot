package com.mycompany.app.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mycompany.app.repository.CompetitionRepository;
import com.mycompany.app.sync.ApiSyncManager;
import com.mycompany.app.sync.RateLimitManager;

import net.dv8tion.jda.api.requests.GatewayIntent;

@Component
public class BotStarter implements CommandLineRunner {

    @Value("${bot.token}")
    private String botToken;

    private final MyListener myListener;
    private final ApiSyncManager apiSyncManager;
    private final CompetitionRepository competitionRepository;
    public BotStarter(MyListener myListener, ApiSyncManager apiSyncManager, CompetitionRepository competitionRepository) {
        this.myListener = myListener;
        this.apiSyncManager = apiSyncManager;
        this.competitionRepository = competitionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Odpalam Hermesa...");
        if (competitionRepository.count() == 0) {
            apiSyncManager.fetchCompetitions();
            apiSyncManager.fetchTeams();
        }
        apiSyncManager.fetchFixtures();
        apiSyncManager.fetchStandings();
        JDA api = JDABuilder.createDefault(botToken)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(myListener)
                .build();
                
        System.out.println("Hermes połączony z Discordem!");
    }
}