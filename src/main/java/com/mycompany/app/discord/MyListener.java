package com.mycompany.app.discord;

import com.mycompany.app.client.FootballApiClient;

import java.util.Arrays;
import java.util.List;
import com.mycompany.app.model.Match;
import com.mycompany.app.model.Team;
import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Standing;
import com.mycompany.app.discord.formatter.TeamFormatter;
import com.mycompany.app.discord.formatter.StandingFormatter;

import com.mycompany.app.discord.command.BotCommand;

import com.mycompany.app.discord.formatter.CompetitionFormatter;
import com.mycompany.app.discord.formatter.MatchFormatter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;


@Component
public class MyListener extends ListenerAdapter {
    

    private final List<BotCommand> commands;
    
    public MyListener(List<BotCommand> commands) {
        this.commands = commands;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        
        String rawMessage = event.getMessage().getContentRaw();
        String commandName = rawMessage.split(" ")[0]; 

        for (BotCommand command : commands) {
            if (command.supports(commandName)) {
                command.execute(event);
                return;
            }
        }
    }
}