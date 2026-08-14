package com.mycompany.app.discord.formatter;

import java.util.List;
import com.mycompany.app.model.Standing;
public class StandingFormatter {
    
    public static String format(List<Standing> standings) {
        
        StringBuilder sb = new StringBuilder();        
        sb.append("```\n"); // Start bloku kodu Discorda
        sb.append("Pos | Team                      |  M |  W |  D |  L |  GF |  GA |  GD | Pts |  Last 5   |\n");
        sb.append("-----------------------------------------------------------------------------------------\n");
        for (Standing standing : standings) {
            
            // Tutaj używamy String.format do stworzenia równego wiersza 
            // i NATYCHMIAST wrzucamy go do naszego StringBuildera
            sb.append(String.format("%3d |  %-26s | %2d | %2d | %2d | %2d | %3d | %3d | %3d | %3d | %9s |\n",
                    standing.getPosition(),
                    standing.getTeam().getName(), 
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