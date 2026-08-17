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
import com.mycompany.app.discord.formatter.MatchFormatter;
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
            
            channel.sendMessage("Fetching standings").queue();

            new Thread(() -> {
                try {
                    footballClient.fetchStandings();
                    channel.sendMessage("Fetched standings, after minute starting to fetch matches").queue();
                    Thread.sleep(1000 * 60); 

                    footballClient.fetchFixtures();
                    channel.sendMessage("Finished fetching all of the matches!").queue(); 
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    // 3. Łapiemy błędy, żeby bot nie umarł w ciszy
                    channel.sendMessage("**Error**: " + e.getMessage()).queue();
                }
            }).start();

            return;
        }
        
        if (args[0].equals("!matches")) {
            if (args.length < 2) {
                sendHelpMessage(channel, "**Error** : Too few arguments. You need to specify the Team");
                return;
            }
            String searchPhrase = String.join(" ", Arrays.copyOfRange(args,1,args.length));
            try {
                List<Team> teams = footballClient.getTeam(searchPhrase);
                if (teams.isEmpty()) {
                    sendHelpMessage(channel, "**Error**: Team '" + searchPhrase + "' not found");
                    return;
                }
                if (teams.size() > 1) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("**Conflict!** Found multiple teams for `").append(searchPhrase).append("`:\n```\n");
                    for (Team t : teams) {
                        sb.append(String.format("- %s\n", t.getShortName()));
                    }
                    sb.append("```\nPlease use one of the listed **names** above to specify which one you mean (e.g., `!matches Barca`).\n");
                    channel.sendMessage(sb.toString()).queue();
                    return;
                }
                Team team = teams.get(0);
                List<Match> matches = footballClient.getMatches(team);
                channel.sendMessage("Next 5 **" + team.getShortName() + "** matches\n\n" + MatchFormatter.format(matches)).queue();
            }
            catch (IllegalArgumentException e) {
                try {
                    Competition competition = footballClient.getCompetition(searchPhrase);
                    List<Match> matches = footballClient.getMatches(competition);
                    channel.sendMessage("Next 5 **" + competition.getName() + "** matches\n\n" + MatchFormatter.format(matches)).queue();
                } catch(IllegalArgumentException es) {
                    System.out.println(e.getMessage());
                    sendHelpMessage(channel, "**Error** : Team '" + searchPhrase + "' not found.");
                }
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