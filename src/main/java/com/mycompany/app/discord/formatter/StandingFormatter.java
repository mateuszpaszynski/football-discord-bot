package com.mycompany.app.discord.formatter;

import java.util.List;
import com.mycompany.app.model.Standing;
public class StandingFormatter {
    
    public static String format(List<Standing> standings) {
        
        StringBuilder sb = new StringBuilder();        
        sb.append("```\n");
        sb.append(" #| Team             |  M |  W |  D |  L |  GF |  GA |  GD | Pts |  Last 5   |\n");
        sb.append("------------------------------------------------------------------------------\n");
        for (Standing standing : standings) {
            String name = (standing.getTeam().getShortName() == null || standing.getTeam().getShortName().equals("null") ? standing.getTeam().getName() : standing.getTeam().getShortName());
            sb.append(String.format("%2d| %-16s | %2d | %2d | %2d | %2d | %3d | %3d | %3d | %3d | %9s |\n",
                    standing.getPosition(),
                    name,
                    standing.getPlayedGames(),
                    standing.getGamesWon(),
                    standing.getGamesDrawn(),
                    standing.getGamesLost(),
                    standing.getGoalsFor(),
                    standing.getGoalsAgainst(),
                    standing.getGoalDifference(),
                    standing.getPoints(),
                    standing.getForm()
            ));
        }
        sb.append("```");
        return sb.toString();
    }

}