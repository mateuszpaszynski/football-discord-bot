package com.mycompany.app.discord.command;

import java.util.List;
import org.springframework.stereotype.Component;

import com.mycompany.app.discord.formatter.ErrorFormatter;
import com.mycompany.app.discord.formatter.MatchFormatter;
import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Match;
import com.mycompany.app.model.Team;
import com.mycompany.app.service.CompetitionService;
import com.mycompany.app.service.MatchService;
import com.mycompany.app.service.TeamService;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

@Component
public class MatchesCommand implements BotCommand {

    private final TeamService teamService;
    private final MatchService matchService;
    private final CompetitionService competitionService;

    public MatchesCommand(TeamService teamService, MatchService matchService, CompetitionService competitionService) {
        this.teamService = teamService;
        this.matchService = matchService;
        this.competitionService = competitionService;
    }

    @Override
    public String getName() {
        return "matches";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping queryOption = event.getOption("query");
        
        String searchPhrase = queryOption.getAsString();

        List<Team> teams = teamService.getTeam(searchPhrase);

        if (teams.size() > 1) {
            StringBuilder sb = new StringBuilder();
            sb.append("**Conflict!** Found multiple teams for `").append(searchPhrase).append("`:\n```\n");
            for (Team t : teams) {
                sb.append(String.format("- %s\n", t.getShortName()));
            }
            sb.append("```\nPlease use one of the listed **names** above (e.g., `/matches query: Barca`).\n");
            event.reply(sb.toString()).setEphemeral(true).queue();
            return;
        }

        if (teams.size() == 1) {
            Team team = teams.get(0);
            List<Match> matches = matchService.getMatches(team);
            event.reply("Next 5 **" + team.getShortName() + "** matches\n\n" + MatchFormatter.format(matches)).queue();
            return;
        }

        try {
            Competition competition = competitionService.getCompetition(searchPhrase);
            List<Match> matches = matchService.getMatches(competition);
            event.reply("Next 5 **" + competition.getName() + "** matches\n\n" + MatchFormatter.format(matches)).queue();
            
        } catch (IllegalArgumentException e) {
            event.reply(ErrorFormatter.format(competitionService.getCompetitions(),
                "**Error**: Neither Team nor League found for **'" + searchPhrase + "'**."
            )).setEphemeral(true).queue();    
        }
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(),"See next 5 matches for given team or competition")
        .addOption(OptionType.STRING,"query","Provide TEAM name or code or LEAGUE name or code",true)
        ;
    }

}