package pl.mateuszpaszynski.footballtracker.notification;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import pl.mateuszpaszynski.footballtracker.event.GoalScoredEvent;
import pl.mateuszpaszynski.footballtracker.model.Match;
import pl.mateuszpaszynski.footballtracker.model.Team;
import java.util.List;
import java.time.*;

@Service
public class DiscordNotificationService {
    
    private final JDA jda;
    public DiscordNotificationService(JDA jda) {
        this.jda = jda;
    }
    @EventListener
    public void handleGoalScored(GoalScoredEvent event) {
        
        Match match = event.match();
        Team homeTeam = match.getHomeTeam();
        Team awayTeam = match.getAwayTeam();

        long unixSeconds = Instant.parse(match.getTime()).getEpochSecond();
        String relativeTime = String.format("<t:%d:R>", unixSeconds); 
        String message = String.format("--------------------------------\n⚽ GOAL **%s** \n🏟️ %s **%s** %s (%s) \n⏱️ Start: %s",
                    event.scoringTeam().getDisplayName(), 
                    homeTeam.getDisplayName(),
                    event.newScore(),
                    awayTeam.getDisplayName(),
                    match.getCompetition().getCode(), 
                    relativeTime 
                    );
        List<TextChannel> channels = jda.getTextChannelsByName("goal-notifications", true);
        if (!channels.isEmpty()) {
            channels.get(0).sendMessage(message).queue();
        }
    }
}
