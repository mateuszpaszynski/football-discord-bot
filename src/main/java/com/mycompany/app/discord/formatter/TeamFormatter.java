package com.mycompany.app.discord.formatter;
import java.util.List;
import com.mycompany.app.model.Team;

public class TeamFormatter {
    public static String format(List<Team> teams) {
        StringBuilder sb = new StringBuilder();
        sb.append("```");
        sb.append("Team             | code |\n");
        sb.append("-------------------------\n");
        for (Team team : teams) {
            
            sb.append(String.format("%-16s | %3s  |\n",
            team.getDisplayName(),
            team.getTla()
            ));
        }
        sb.append("```");
        sb.append("\n**Note** : if your team doesnt have a code ('-' is displayed instead )  in our database you have to type **full name** to proceed");
        return sb.toString();
    }
}
