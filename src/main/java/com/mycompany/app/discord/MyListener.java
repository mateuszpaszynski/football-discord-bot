package com.mycompany.app.discord;

import com.mycompany.app.client.FootballClient;
import java.util.List;
import com.mycompany.app.model.Competition;
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
        // if (content.equals("!fixtures")) {
        //     MessageChannel channel = event.getChannel();
        //     channel.sendMessage("I'm starting to save matches").queue();
        //     new Thread(() -> {
        //         footballClient.fetchFixtures();
        //         channel.sendMessage("Finished fetching all of the matches");
        //     }).start();
        //     channel.sendMessage("I love fortnite").queue();
        //  }
        // if (content.equals("!teams")) {
        //     MessageChannel channel = event.getChannel();
            
        //     channel.sendMessage("fetched some teams").queue();
        // }
        if (content.equals("!competitions")) {
            MessageChannel channel = event.getChannel();
            
            List<Competition> comps = footballClient.getCompetitions();
            
            int maxLength = 0;
            for (Competition comp : comps) {
                maxLength = Math.max(maxLength,comp.getName().length());
            }
            
            StringBuilder sb = new StringBuilder();

            sb.append("Available competitions: \n");
            sb.append("```text\n");
            
            for (Competition comp : comps) {
                String formatPattern = "%-" + maxLength + "s";   
   
                String paddedName = String.format(formatPattern, comp.getName());
                
                sb.append(paddedName).append(" | ").append(comp.getCountry()).append("\n");
            }
            sb.append("```");
            channel.sendMessage(sb.toString()).queue();
        }
        
    }
}