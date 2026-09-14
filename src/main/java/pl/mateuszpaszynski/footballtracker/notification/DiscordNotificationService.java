package pl.mateuszpaszynski.footballtracker.notification;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;

import pl.mateuszpaszynski.footballtracker.event.GoalScoredEvent;
import pl.mateuszpaszynski.footballtracker.model.Match;
import pl.mateuszpaszynski.footballtracker.model.Team;
import java.time.*;

@Service
@RequiredArgsConstructor 
public class DiscordNotificationService {
    
    private final JDA jda;

    @EventListener
    public void handleGoalScored(GoalScoredEvent event) {
        
        Match match = event.match();
        Team homeTeam = match.getHomeTeam();
        Team awayTeam = match.getAwayTeam();

        long unixSeconds = Instant.parse(match.getTime()).getEpochSecond();
        String relativeTime = String.format("<t:%d:R>", unixSeconds); 
        String message = String.format("🔴 GOAL **%s** \n🏟️ %s **%s** %s (%s) \n⏱️ Start: %s\n\n",
                    event.scoringTeam().getDisplayName(), 
                    homeTeam.getDisplayName(),
                    event.newScore(),
                    awayTeam.getDisplayName(),
                    match.getCompetition().getCode(), 
                    relativeTime 
                    );
        jda.getTextChannelById("goal-notifications").sendMessage(message).queue();
    }

}
