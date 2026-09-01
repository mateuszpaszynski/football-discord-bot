package pl.mateuszpaszynski.footballtracker.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.requests.GatewayIntent;
import pl.mateuszpaszynski.footballtracker.repository.CompetitionRepository;
import pl.mateuszpaszynski.footballtracker.sync.ApiSyncManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
       log.info("Running Hermes");
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
                
        log.info("Hermes connected");
    }
}