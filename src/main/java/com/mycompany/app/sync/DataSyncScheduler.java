package com.mycompany.app.sync;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataSyncScheduler {
    
    private final ApiSyncManager apiSyncManager;
    public DataSyncScheduler(ApiSyncManager apiSyncManager) {
        this.apiSyncManager = apiSyncManager;
    }

    @Scheduled(cron = "0 0 * * * *") 
    public void scheduleHourlySync()    
    {
        apiSyncManager.fetchFixtures(); 
        apiSyncManager.fetchStandings();
    }

    @Scheduled(cron = "0 0 3 1 * *")
    public void scheduleMonthlySync() {
        apiSyncManager.fetchTeams();
        apiSyncManager.fetchCompetitions();
    }
}
