package pl.mateuszpaszynski.footballtracker.discord.command;
import java.util.List;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.mateuszpaszynski.footballtracker.discord.formatter.ErrorFormatter;
import pl.mateuszpaszynski.footballtracker.discord.formatter.TeamFormatter;
import pl.mateuszpaszynski.footballtracker.model.Competition;
import pl.mateuszpaszynski.footballtracker.model.Team;
import pl.mateuszpaszynski.footballtracker.service.CompetitionService;
import pl.mateuszpaszynski.footballtracker.service.TeamService;

@Component
public class TeamsCommand implements BotCommand {
    
    private final CompetitionService competitionService;
    private final TeamService teamService;

    public TeamsCommand(CompetitionService competitionService, TeamService teamService) {
        this.competitionService = competitionService;
        this.teamService = teamService;
    }
    @Override
    public String getName() {
        return "teams";
    }
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        
        OptionMapping leagueOption = event.getOption("league");

        String searchPhrase = leagueOption.getAsString();
        try {
            Competition league = competitionService.getCompetition(searchPhrase);
            List<Team> teams = teamService.getTeams(league);
            event.reply("**Teams in " + league.getName() + "**\n" + TeamFormatter.format(teams)).queue();
        }
        catch (IllegalArgumentException e) {
            event.reply(ErrorFormatter.format(competitionService.getCompetitions(),
            "**Error** : League '" + searchPhrase + "' not found.\n Available Competitions: ")).setEphemeral(true).queue();
        }
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(),"See teams for given competition")
        .addOption(OptionType.STRING, "league", "Provide league name or code", true);
    }
}
