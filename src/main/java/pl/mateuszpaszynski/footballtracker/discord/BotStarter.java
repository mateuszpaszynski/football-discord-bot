package pl.mateuszpaszynski.footballtracker.discord;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.mateuszpaszynski.footballtracker.repository.CompetitionRepository;
import pl.mateuszpaszynski.footballtracker.sync.ApiSyncManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BotStarter implements CommandLineRunner {

    private final ApiSyncManager apiSyncManager;
    private final CompetitionRepository competitionRepository;

    public BotStarter(ApiSyncManager apiSyncManager, CompetitionRepository competitionRepository) {
        this.apiSyncManager = apiSyncManager;
        this.competitionRepository = competitionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Hermes initial data sync...");
        if (competitionRepository.count() == 0) {
            apiSyncManager.fetchCompetitions();
            apiSyncManager.fetchTeams();
        }
        apiSyncManager.fetchFixtures();
        apiSyncManager.fetchStandings();
        log.info("Hermes is running");
    }
}