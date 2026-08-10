package com.mycompany.app;

import com.mycompany.app.FootballClient;

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
            String fixtures = footballClient.fetchFixtures();
            channel.sendMessage("Next 5 Real Madrid fixtures" + fixtures).queue();
        }
        
    }
}