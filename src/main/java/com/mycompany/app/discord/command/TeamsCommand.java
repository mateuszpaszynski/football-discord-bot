package com.mycompany.app.discord.command;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mycompany.app.discord.formatter.ErrorFormatter;
import com.mycompany.app.discord.formatter.TeamFormatter;
import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Team;
import com.mycompany.app.service.CompetitionService;
import com.mycompany.app.service.TeamService;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Component
public class TeamsCommand implements BotCommand{
    
    private final CompetitionService competitionService;
    private final TeamService teamService;

    public TeamsCommand(CompetitionService competitionService, TeamService teamService) {
        this.competitionService = competitionService;
        this.teamService = teamService;
    }
    @Override
    public boolean supports(String command) {
        return command.equalsIgnoreCase("!teams");
    }
    @Override
    public void execute(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        String message = event.getMessage().getContentRaw();
        
        String[] args = message.split(" ");
        if (args.length < 2) {
            channel.sendMessage(ErrorFormatter.format(competitionService.getCompetitions(),
            "**Error**: Too few arguments. You need to specify the Leauge.")).queue();
            return;
        }
        String searchPhrase = String.join(" ", Arrays.copyOfRange(args, 1, args.length));;
        try {
            Competition league = competitionService.getCompetition(searchPhrase);
            List<Team> teams = teamService.getTeams(league);
            channel.sendMessage("**Teams in " + league.getName() + "**\n" + TeamFormatter.format(teams)).queue();
        }
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            channel.sendMessage(ErrorFormatter.format(competitionService.getCompetitions(),
            "**Error** : League '" + searchPhrase + "' not found.")).queue();
        }
    }

}
