package com.mycompany.app.discord;

import com.mycompany.app.client.FootballClient;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class MyListener extends ListenerAdapter {
    
    private final FootballClient footballClient;

    public MyListener(FootballClient _footballClient) {
        footballClient = _footballClient;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        
        Message message = event.getMessage();
        String content = message.getContentRaw(); 
        
        if (content.equals("!ping")) {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Pong!").queue();
        }
        if (content.equals("!fixtures")) {
            MessageChannel channel = event.getChannel();
    
            channel.sendMessage("Saved some matches").queue();
        }
        if (content.equals("!teams")) {
            MessageChannel channel = event.getChannel();
            String teams = footballClient.fetchTeams();
            channel.sendMessage(teams).queue();
        }
        if (content.equals("!competitions")) {
            MessageChannel channel = event.getChannel();
            footballClient.fetchCompetitions();
            channel.sendMessage("Saved some competitions").queue();
        }
        
    }
}