package com.mycompany.app.discord.command;

import org.springframework.stereotype.Component;
import java.util.List;

import com.mycompany.app.discord.formatter.CompetitionFormatter;
import com.mycompany.app.model.Competition;
import com.mycompany.app.service.CompetitionService;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Component
public class CompetitionsCommand implements BotCommand{
    
    private final CompetitionService competitionService;

    public CompetitionsCommand(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @Override 
    public boolean supports(String commandName) {
        return commandName.equalsIgnoreCase("!competitions");
    }
    @Override 
    public void execute(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        List<Competition> comps = competitionService.getCompetitions();
        String competitions = CompetitionFormatter.format(comps);
        channel.sendMessage(competitions).queue();
    }
}
