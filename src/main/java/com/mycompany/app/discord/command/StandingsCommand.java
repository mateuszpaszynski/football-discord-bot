package com.mycompany.app.discord.command;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mycompany.app.discord.formatter.ErrorFormatter;
import com.mycompany.app.discord.formatter.StandingFormatter;
import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Standing;
import com.mycompany.app.service.CompetitionService;
import com.mycompany.app.service.StandingService;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Component
public class StandingsCommand implements BotCommand{
    
    private final CompetitionService competitionService;
    private final StandingService standingService;
    public StandingsCommand(CompetitionService competitionService, StandingService standingService) {
        this.competitionService = competitionService;
        this.standingService = standingService;
    }

    @Override
    public boolean supports(String command) {
        return command.equalsIgnoreCase("!standings");
    }

    @Override
    public void execute(MessageReceivedEvent event) {

        MessageChannel channel = event.getChannel();
        String message = event.getMessage().getContentRaw();    

        String[] args = message.split(" ");

        if (args.length < 2) {
            channel.sendMessage(ErrorFormatter.format(competitionService.getCompetitions(),
            "**Error**: Too few arguments. You need to specify the League.")).queue();    
            return;
        }
            //footballClient.fetchStandings();
            String searchPhrase = String.join(" ", Arrays.copyOfRange(args, 1, args.length));;
            try {
                Competition league = competitionService.getCompetition(searchPhrase);
                List<Standing> standings = standingService.getStandings(league);
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
                channel.sendMessage(ErrorFormatter.format(competitionService.getCompetitions(),
                    "**Error** : League '" + searchPhrase + "' not found."
                )).queue();
            }
    }

}

