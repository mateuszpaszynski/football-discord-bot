package pl.mateuszpaszynski.footballtracker.discord.formatter;

import java.util.List;

import pl.mateuszpaszynski.footballtracker.model.Standing;
public class StandingFormatter {
    
    public static String format(List<Standing> standings) {
        
        StringBuilder sb = new StringBuilder();        
        sb.append("```\n");
        sb.append(" #| Team             |  M | Pts |\n");
        sb.append("---------------------------------\n");
        for (Standing standing : standings) {
            
            sb.append(String.format("%2d| %-16s | %2d | %3d |\n",
                    standing.getPosition(),
                    standing.getTeam().getDisplayName(),
                    standing.getPlayedGames(),
                    standing.getPoints()
            ));
        }
        sb.append("```");
        sb.append("*Note: The API provider may take a few hours to update standings after matches finish.*");
        return sb.toString();
    }

}