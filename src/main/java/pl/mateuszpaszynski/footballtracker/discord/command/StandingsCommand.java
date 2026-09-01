package pl.mateuszpaszynski.footballtracker.discord.command;

import java.util.List;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.mateuszpaszynski.footballtracker.discord.formatter.ErrorFormatter;
import pl.mateuszpaszynski.footballtracker.discord.formatter.StandingFormatter;
import pl.mateuszpaszynski.footballtracker.model.Competition;
import pl.mateuszpaszynski.footballtracker.model.Standing;
import pl.mateuszpaszynski.footballtracker.service.CompetitionService;
import pl.mateuszpaszynski.footballtracker.service.StandingService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Component
public class StandingsCommand implements BotCommand{
    
    private final CompetitionService competitionService;
    private final StandingService standingService;
    
    public StandingsCommand(CompetitionService competitionService, StandingService standingService) {
        this.competitionService = competitionService;
        this.standingService = standingService;
    }

    @Override
    public String getName() {
        return "standings";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {

        OptionMapping leagueOption = event.getOption("league");
            
        String searchPhrase = leagueOption.getAsString();
        try {
            Competition league = competitionService.getCompetition(searchPhrase);
            List<Standing> standings = standingService.getStandings(league);
            if (standings.size() > 20) {
                int mid = standings.size() / 2;
                event.reply(StandingFormatter.format(standings.subList(0, mid)))
                    .queue(v -> event.getHook().sendMessage(StandingFormatter.format(standings.subList(mid, standings.size()))).queue());
            }
            else {
                event.reply("** " + league.getName() + " standings**\n" + StandingFormatter.format(standings)).queue();
            }

        } catch (IllegalArgumentException e) {
            event.reply(ErrorFormatter.format(competitionService.getCompetitions(),
                "**Error** : League '" + searchPhrase + "' not found.\n Available Competitions: "
            )).setEphemeral(true).queue();
        }
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), "See standings for a given competition")
        .addOption(OptionType.STRING,"league","Provide league name or code",true);
    }
}