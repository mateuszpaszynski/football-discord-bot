package pl.mateuszpaszynski.footballtracker.discord.command;

import org.springframework.stereotype.Component;
import java.util.List;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.mateuszpaszynski.footballtracker.discord.formatter.CompetitionFormatter;
import pl.mateuszpaszynski.footballtracker.model.Competition;
import pl.mateuszpaszynski.footballtracker.service.CompetitionService;
@Component
public class CompetitionsCommand implements BotCommand{
    
    private final CompetitionService competitionService;

    public CompetitionsCommand(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @Override 
    public String getName() {
        return "competitions";
    }
    @Override 
    public void execute(SlashCommandInteractionEvent event) {
        
        List<Competition> comps = competitionService.getCompetitions();
        
        event.reply(CompetitionFormatter.format(comps)).queue();
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), "Displays list of available competitions");
    }
}