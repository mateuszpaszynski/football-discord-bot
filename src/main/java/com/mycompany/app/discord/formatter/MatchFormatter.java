package com.mycompany.app.discord.formatter;

import java.time.Instant;
import java.util.List;
import com.mycompany.app.model.Match;
import com.mycompany.app.model.Team;
import com.mycompany.app.model.Competition;

public class MatchFormatter {
    public static String format(List<Match> matches) {
        StringBuilder sb = new StringBuilder();
        String lastUpdated = "";
        
        for (Match match : matches) {
            
            Team homeTeam = match.getHomeTeam();
            Team awayTeam = match.getAwayTeam();
            Competition comp = match.getCompetition();

            Instant matchTime = Instant.parse(match.getTime());
            long unixSeconds = matchTime.getEpochSecond();
            
            String status = match.getStatus();
            if (status.equals("LIVE") || status.equals("PAUSED") || status.equals("IN_PLAY")) {
                lastUpdated = String.format("<t:%d:R>", match.getLastUpdated().getEpochSecond());
            }
            String score = match.getScore();
            String discordTime = String.format("<t:%d:f>", unixSeconds);

            String relativeTime = String.format("<t:%d:R>", unixSeconds); 
            switch (status) {
                case "FINISHED":
                    sb.append(String.format("🏁 %s **%s** %s (%s) \n📅 %s \n\n", 
                    homeTeam.getDisplayName(),
                    score,
                    awayTeam.getDisplayName(),
                    comp.getCode(), 
                    relativeTime
                    )); 
                    break;
                case "SCHEDULED":
                    sb.append(String.format("🏟️ %s vs %s (%s) \n📅 %s (%s)\n\n", 
                    homeTeam.getDisplayName(), 
                    awayTeam.getDisplayName(),
                    comp.getCode(), 
                    discordTime, 
                    "SCHEDULED - hour **will change**"
                    ));  
                    break;
                case "TIMED":
                    sb.append(String.format("🏟️ %s vs %s (%s) \n📅 %s (%s)\n\n", 
                    homeTeam.getDisplayName(), 
                    awayTeam.getDisplayName(),
                    comp.getCode(), 
                    discordTime, 
                    relativeTime
                    ));     
                    break;
                case "LIVE":
                case "IN_PLAY":
                case "PAUSED":
                    sb.append(String.format("🔴 **LIVE**\n🏟️ %s **%s** %s (%s) \n⏱️ Start: %s\n\n", 
                    homeTeam.getDisplayName(),
                    score,
                    awayTeam.getDisplayName(),
                    comp.getCode(), 
                    relativeTime 
                    )); 
                    break;
                case "POSTPONED":
                case "SUSPENDED":
                case "CANCELLED":
                    break;
                    
                default:
                    sb.append(String.format("❓ %s vs %s (%s) - Status: %s\n\n", 
                        homeTeam.getDisplayName(), 
                        awayTeam.getDisplayName(),
                        comp.getCode(),
                        status
                    )); 
            }
        }
        if (!lastUpdated.isEmpty()) {
            sb.append("Please note that the API provides result with 5 minut delay\n**Last update with API: " + lastUpdated + "**");
        }
        return sb.toString();
    }    
}
