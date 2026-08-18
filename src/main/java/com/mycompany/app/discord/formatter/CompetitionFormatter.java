package com.mycompany.app.discord.formatter;
import java.util.List;
import com.mycompany.app.model.Competition;

public class CompetitionFormatter {
    public static String format(List<Competition> allComps) {
        StringBuilder sb = new StringBuilder();
        sb.append("**Available competitions** : \n");
        sb.append("```");
        sb.append("Name                  | code |\n");
        sb.append("------------------------------\n");

        for (Competition comp : allComps) {
            sb.append(String.format("%-21s | %-3s  |\n",
            comp.getName(),
            comp.getCode()
            ));
        }
        sb.append("```");
        return sb.toString();
    }
}
