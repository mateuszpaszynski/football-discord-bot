package com.mycompany.app.discord;

import com.mycompany.app.client.FootballClient;
import java.util.List;
import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Standing;
import com.mycompany.app.discord.formatter.StandingFormatter;
import com.mycompany.app.discord.formatter.CompetitionFormatter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class MyListener extends ListenerAdapter {
    
    private final FootballClient footballClient;
    public MyListener(FootballClient footballClient) {
        this.footballClient = footballClient;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        
        Message message = event.getMessage();
        String content = message.getContentRaw(); 

        MessageChannel channel = event.getChannel();
        
        String[] args = content.split("\\s+");

        // if (args[0].equals("!standings")) {
        //     try {
        //         if (args.length < 2) {
        //             channel.sendMessage("Too few arguments you need to specify the Leauge").queue();
        //             return;
        //         }
        //         String leagueCode = args[1];

        //         Competition Laliga = footballClient.getCompetition(2014L);
        //         List<Standing> standings = footballClient.getStandings(Laliga);
        //         String table = StandingFormatter.format(standings);
        //         channel.sendMessage(table).queue();
        //     }
        //     catch (IllegalArgumentException e) {
        //         channel.sendMessage("ACHTUNG!!!" + e.getMessage()).queue();
        //     }
        // }
        
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
        if (args[0].equals("!competitions")) {
            List<Competition> comps = footballClient.getCompetitions();
            String competitions = CompetitionFormatter.format(comps);
            channel.sendMessage(competitions).queue();
        }
        
    }
}