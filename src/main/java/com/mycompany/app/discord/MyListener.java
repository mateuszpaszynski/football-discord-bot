package com.mycompany.app.discord;

import com.mycompany.app.client.FootballClient;

import java.util.Arrays;
import java.util.List;
import com.mycompany.app.model.Match;
import com.mycompany.app.model.Team;
import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Standing;
import com.mycompany.app.discord.formatter.TeamFormatter;
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
        sb.append(errorMessage).append("\n\n");
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
        if (args[0].equals("!load")) {
            footballClient.fetchStandings();
            //footballClient.fetchFixtures();
            return;
        }
        
        if (args[0].equals("!matches")) {
            if (args.length < 2) {
                sendHelpMessage(channel, "**Error** : Too few arguments. You need to specify the Team");
                return;
            }
            String searchPhrase = String.join(" ", Arrays.copyOfRange(args,1,args.length));
            try {
                Team team = footballClient.getTeam(searchPhrase);
                List<Match> matches = footballClient.getMatches(team);
                StringBuilder sb = new StringBuilder();
                sb.append("```\n");
                int i = 0;
                for (Match match : matches) {
                    sb.append(match.getHomeTeam().getName() + " - " + match.getAwayTeam().getName() + "\n" + match.getTime() + "\n");    
                    i++;
                    if ( i > 5) {
                        break;
                    }
                }
                sb.append("```");
                channel.sendMessage(sb.toString()).queue();
            }
            catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                sendHelpMessage(channel, "**Error** : Team '" + searchPhrase + "' not found.");
            }
        }
        if (args[0].equals("!standings")) {
            if (args.length < 2) {
                sendHelpMessage(channel, "**Error**: Too few arguments. You need to specify the League.");
                return;
            }
            //footballClient.fetchStandings();
            String searchPhrase = String.join(" ", Arrays.copyOfRange(args, 1, args.length));;
            try {
                Competition league = footballClient.getCompetition(searchPhrase);
                List<Standing> standings = footballClient.getStandings(league);
                if ( standings.size() > 20) {
                    int mid = standings.size() / 2;
                    channel.sendMessage(StandingFormatter.format(standings.subList(0,mid))).queue();
                    channel.sendMessage(StandingFormatter.format(standings.subList(mid,standings.size()))).queue();
                }
                else {
                    channel.sendMessage("** " + league.getName() + " standings**\n" + StandingFormatter.format(standings)).queue();
                }

            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                sendHelpMessage(channel, "**Error** : League '" + searchPhrase + "' not found.");
            }
        }
        if (args[0].equals("!teams")) {
            if (args.length < 2) {
                sendHelpMessage(channel,"**Error**: Too few arguments. You need to specify the Leauge.");
                return;
            }
            String searchPhrase = String.join(" ", Arrays.copyOfRange(args, 1, args.length));;
            try {
                Competition league = footballClient.getCompetition(searchPhrase);
                List<Team> teams = footballClient.getTeams(league);
                channel.sendMessage("**Teams in " + league.getName() + "**\n" + TeamFormatter.format(teams)).queue();
            }
            catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                sendHelpMessage(channel,"**Error** : League '" + searchPhrase + "' not found." );
            }
        }
        if (args[0].equals("!competitions")) {
            List<Competition> comps = footballClient.getCompetitions();
            String competitions = CompetitionFormatter.format(comps);
            channel.sendMessage(competitions).queue();
        }
        
    }
}