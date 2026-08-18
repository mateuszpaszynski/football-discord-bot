package com.mycompany.app.discord.formatter;

import java.util.List;
import com.mycompany.app.model.Competition;

public class ErrorFormatter {
    public static String format(List<Competition> competitions, String errorMessage) {

        StringBuilder sb = new StringBuilder();
        sb.append(errorMessage).append("\n\n");
        sb.append(CompetitionFormatter.format(competitions));
        return sb.toString();
    }
}
