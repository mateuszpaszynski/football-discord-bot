package com.mycompany.app.discord.command;



import java.util.Arrays;

import java.util.List;



import org.springframework.stereotype.Component;



import com.mycompany.app.discord.formatter.ErrorFormatter;

import com.mycompany.app.discord.formatter.MatchFormatter;

import com.mycompany.app.model.Competition;

import com.mycompany.app.model.Match;

import com.mycompany.app.model.Team;

import com.mycompany.app.service.CompetitionService;

import com.mycompany.app.service.MatchService;

import com.mycompany.app.service.TeamService;



import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;



@Component

public class MatchesCommand implements BotCommand{

    

    private final TeamService teamService;

    private final MatchService matchService;

    private final CompetitionService competitionService;



    public MatchesCommand(TeamService teamService, MatchService matchService, CompetitionService competitionService) {

        this.teamService = teamService;

        this.matchService = matchService;

        this.competitionService = competitionService;

    }

    @Override

    public boolean supports(String message) {

        return message.equalsIgnoreCase("!matches");

    }

    @Override

    public void execute(MessageReceivedEvent event) {

        

        MessageChannel channel = event.getChannel();

        String message = event.getMessage().getContentRaw();



        String[] args = message.split(" ");



        if (args.length < 2) {

                channel.sendMessage(ErrorFormatter.format(competitionService.getCompetitions(),

                "**Error** : Too few arguments. You need to specify the Team")).queue();    

                return;

            }

            String searchPhrase = String.join(" ", Arrays.copyOfRange(args,1,args.length));

            try {

                List<Team> teams = teamService.getTeam(searchPhrase);

                if (teams.isEmpty()) {

                    channel.sendMessage(ErrorFormatter.format(competitionService.getCompetitions(),

                    "**Error**: Team '" + searchPhrase + "' not found")).queue();   

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

                List<Match> matches = matchService.getMatches(team);

                channel.sendMessage("Next 5 **" + team.getShortName() + "** matches\n\n" + MatchFormatter.format(matches)).queue();

            }

            catch (IllegalArgumentException e) {

                try {

                    Competition competition = competitionService.getCompetition(searchPhrase);

                    List<Match> matches = matchService.getMatches(competition);

                    channel.sendMessage("Next 5 **" + competition.getName() + "** matches\n\n" + MatchFormatter.format(matches)).queue();

                } catch(IllegalArgumentException es) {

                    System.out.println(e.getMessage());

                    channel.sendMessage(ErrorFormatter.format(competitionService.getCompetitions(),

                    "\"**Error** : Team '\" + searchPhrase + \"' not found.\"")).queue();    

                }
            }
    }
} 

