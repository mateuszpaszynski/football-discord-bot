package com.mycompany.app.discord.formatter;

import java.time.Instant;
import java.util.List;
import com.mycompany.app.model.Match;
import com.mycompany.app.model.Team;
import com.mycompany.app.model.Competition;

public class MatchFormatter {
    public static String format(List<Match> matches) {
        StringBuilder sb = new StringBuilder();
        
        for (Match match : matches) {
            Team homeTeam = match.getHomeTeam();
            Team awayTeam = match.getAwayTeam();
            Competition comp = match.getCompetition();

            Instant matchTime = Instant.parse(match.getTime());
            long unixSeconds = matchTime.getEpochSecond();
            String discordTime = String.format("<t:%d:f>", unixSeconds);

            String relativeTime = String.format("<t:%d:R>", unixSeconds); 

            sb.append(String.format("🏟️ %s vs %s (%s) \n📅 %s (%s)\n\n", 
                homeTeam.getShortName(), 
                awayTeam.getShortName(),
                comp.getCode(), 
                discordTime, 
                relativeTime
            ));
        }

        return sb.toString();
    }    
}
