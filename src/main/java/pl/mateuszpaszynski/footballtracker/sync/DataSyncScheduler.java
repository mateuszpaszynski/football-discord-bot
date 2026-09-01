package pl.mateuszpaszynski.footballtracker.sync;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DataSyncScheduler {
    
    private final ApiSyncManager apiSyncManager;
    public DataSyncScheduler(ApiSyncManager apiSyncManager) {
        this.apiSyncManager = apiSyncManager;
    }

    @Scheduled(cron = "0 * * * * *") 
    public void pollLiveMatches()    
    {
        apiSyncManager.fetchFixturesForToday();
    }

    @Scheduled(cron = "0 0 5 * * *")
    public void scheduleDailySync() {
        apiSyncManager.fetchCompetitions();
        apiSyncManager.fetchTeams();
        apiSyncManager.fetchStandings();
        apiSyncManager.fetchFixtures();
    }
}
