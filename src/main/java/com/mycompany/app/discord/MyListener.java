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
    private void sendHelpMessage(MessageChannel channel, String errorMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append(errorMessage).append("\n\n**Available Leagues:**\n");
        sb.append(CompetitionFormatter.format(footballClient.getCompetitions()));
        channel.sendMessage(sb.toString()).queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        
        Message message = event.getMessage();
        String content = message.getContentRaw(); 

        MessageChannel channel = event.getChannel();
        
        String[] args = content.split("\\s+");

        if (args[0].equals("!standings")) {
            if (args.length < 2) {
                sendHelpMessage(channel, "Error: Too few arguments. You need to specify the League.");
                return;
            }
            String searchPhrase = args[1];
            try {
                Competition league = footballClient.getCompetition(searchPhrase);
                List<Standing> standings = footballClient.getStandings(league);
                if ( standings.size() > 20) {
                    int mid = standings.size() / 2;
                    channel.sendMessage(StandingFormatter.format(standings.subList(0,mid))).queue();
                    channel.sendMessage(StandingFormatter.format(standings.subList(mid,standings.size()))).queue();
                }
                else {
                    channel.sendMessage(StandingFormatter.format(standings)).queue();
                }

            } catch (IllegalArgumentException e) {
                sendHelpMessage(channel, "Error : League '" + searchPhrase + "' not found.");
            }
        }
        if (args[0].equals("!competitions")) {
            List<Competition> comps = footballClient.getCompetitions();
            String competitions = CompetitionFormatter.format(comps);
            channel.sendMessage(competitions).queue();
        }
        
    }
}