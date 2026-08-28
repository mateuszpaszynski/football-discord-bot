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
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

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

        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            event.reply("Choose from (next/last/live)").setEphemeral(true).queue();
            return;
        }
        OptionMapping queryOption = event.getOption("query");
        String searchPhrase = "any";
        if (queryOption != null) {
            searchPhrase = queryOption.getAsString();
        }
        switch (subcommand) { 
            case "next": 
                handleNextMatches(event, searchPhrase);
                break;
            case "last":
                handleLastMatches(event, searchPhrase);
                break;
            case "live": 
                handleLiveMatches(event);
                break;
            default: 
                event.reply("Choose from (next/last/live)").queue();
        }
    }
    
    private void handleNextMatches(SlashCommandInteractionEvent event, String searchPhrase) {
        
        if (searchPhrase.equals("any")) {
            event.reply("**Today's matches**\n" + MatchFormatter.format(matchService.getTodayMatches())).queue();
            return;
        }
        List<Team> teams = teamService.getTeam(searchPhrase);

        if (teams.size() > 1) {
            StringBuilder sb = new StringBuilder();
            sb.append("**Conflict!** Found multiple teams for `").append(searchPhrase).append("`:\n```\n");
            for (Team t : teams) {
                sb.append(String.format("- %s\n", t.getDisplayName()));
            }
            sb.append("```\nPlease use one of the listed **names** above (e.g., `/matches query: Barca`).\n");
            event.reply(sb.toString()).setEphemeral(true).queue();
            return;
        }

        if (teams.size() == 1) {
            Team team = teams.get(0);
            List<Match> matches = matchService.getNextMatches(team);
            event.reply("Next 5 **" + team.getDisplayName() + "** matches\n\n" + MatchFormatter.format(matches)).queue();
            return;
        }

        try {
            Competition competition = competitionService.getCompetition(searchPhrase);
            List<Match> matches = matchService.getNextMatches(competition);
            event.reply("Next 5 **" + competition.getName() + "** matches\n\n" + MatchFormatter.format(matches)).queue();
            
        } catch (IllegalArgumentException e) {
            event.reply(ErrorFormatter.format(competitionService.getCompetitions(),
                "**Error**: Neither Team nor League found for **'" + searchPhrase + "'**.\n Available Competitions: "
            )).setEphemeral(true).queue();    
        }
    }
    private void handleLastMatches(SlashCommandInteractionEvent event, String searchPhrase) {
        if (searchPhrase.equals("any")) {
            List<Match> matches = matchService.getLast24hMatches();
            if (matches.size() == 0) {
                event.reply("No matches finished in the **last 24 hours**").queue();
                return;
            }
            event.reply("Matches finished in the **last 24 hours** across all available competitions\n" + MatchFormatter.format(matches)).queue();
            return;
        }
        List<Team> teams = teamService.getTeam(searchPhrase);

        if (teams.size() > 1) {
            StringBuilder sb = new StringBuilder();
            sb.append("**Conflict!** Found multiple teams for `").append(searchPhrase).append("`:\n```\n");
            for (Team t : teams) {
                sb.append(String.format("- %s\n", t.getDisplayName()));
            }
            sb.append("```\nPlease use one of the listed **names** above (e.g., `/matches query: Barca`).\n");
            event.reply(sb.toString()).setEphemeral(true).queue();
            return;
        }

        if (teams.size() == 1) {
            Team team = teams.get(0);
            List<Match> matches = matchService.getLastMatches(team);
            event.reply("Last 5 **" + team.getDisplayName() + "** matches\n\n" + MatchFormatter.format(matches)).queue();
            return;
        }

        try {
            Competition competition = competitionService.getCompetition(searchPhrase);
            List<Match> matches = matchService.getLastMatches(competition);
            event.reply("Last 5 **" + competition.getName() + "** matches\n\n" + MatchFormatter.format(matches)).queue();
            
        } catch (IllegalArgumentException e) {
            event.reply(ErrorFormatter.format(competitionService.getCompetitions(),
                "**Error**: Neither Team nor League found for **'" + searchPhrase + "'**.\n Available Competitions: "
            )).setEphemeral(true).queue();    
        }
    }
    private void handleLiveMatches(SlashCommandInteractionEvent event) {
        List<Match> liveMatches = matchService.getLiveMatches();
        
        if (liveMatches.isEmpty()) {
            event.reply("No matches are live right now 😴").queue();
            return;
        }
        String formattedMatches = MatchFormatter.format(liveMatches);
        if (formattedMatches.length() > 2000) {
            formattedMatches = formattedMatches.substring(0, 1990) + "...";
        }
        event.reply("**🔴 Live matches:**\n\n" + formattedMatches).queue();
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(),"View matches")
         .addSubcommands(
                new SubcommandData("next", "See next 5 matches for a team or competition")
                    .addOption(OptionType.STRING,"query","TEAM (eg: 'RMA') or LEAGUE (eg: 'PL') or leave empty for today's matches",false),
                
                new SubcommandData("last", "See last 5 matches for a team or competition")
                   .addOption(OptionType.STRING,"query","TEAM (eg: 'RMA') or LEAGUE (eg: 'PL') or leave empty for today's matches",false),
                
                new SubcommandData("live", "See all live matches across all competitions")
            );
    }

}