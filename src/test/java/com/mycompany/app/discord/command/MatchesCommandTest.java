package com.mycompany.app.discord.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycompany.app.model.Competition;
import com.mycompany.app.model.Match;
import com.mycompany.app.model.Team;
import com.mycompany.app.service.CompetitionService;
import com.mycompany.app.service.MatchService;
import com.mycompany.app.service.TeamService;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

@ExtendWith(MockitoExtension.class)
public class MatchesCommandTest {

    @Mock
    private CompetitionService competitionService;

    @Mock
    private TeamService teamService;

    @Mock
    private MatchService matchService;

    @Mock
    private OptionMapping optionMapping;

    @Mock
    private SlashCommandInteractionEvent event;

    @Mock
    private ReplyCallbackAction replyAction;

    @InjectMocks
    private MatchesCommand command;

    @Test
    void shouldReplyWithFormattedMatchesForTeamNoCollision() {
        Competition comp = new Competition();
        comp.setId(1L);
        comp.setCode("PL");
        comp.setName("Premier League");

        Team team1 = new Team();
        team1.setId(11L);
        team1.setName("Real Madrid");
        
        Team team2 = new Team();
        team2.setId(12L);
        team2.setName("FC Barcelona");

        Match match = new Match();
        match.setCompetition(comp);
        match.setHomeTeam(team1);
        match.setAwayTeam(team2);
        match.setTime("2026-08-07T18:00:00Z");
        match.setId(100L);
        match.setStatus("TIMED");
        
        when(event.getSubcommandName()).thenReturn("next");
        when(event.getOption("query")).thenReturn(optionMapping);
        when(optionMapping.getAsString()).thenReturn("Real Madrid");
        when(teamService.getTeam("Real Madrid")).thenReturn(List.of(team1));
        
        when(matchService.getNextMatches(team1)).thenReturn(List.of(match));
        when(event.reply(anyString())).thenReturn(replyAction);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        command.execute(event);

        verify(event).reply(captor.capture());
        verify(replyAction).queue();
        
        String message = captor.getValue();

        assertThat(message).contains("t:1786125600:f") // Epoch of  2026-08-07T18:00:00Z
                .doesNotContain("Premier League")
                .contains("PL")
                .contains("Real Madrid")
                .contains("FC Barcelona")
                .doesNotContain("null");
    }

    @Test
    void shouldReplyWithAvaibleTeamsWithCollision() {
        Competition comp = new Competition();
        comp.setId(1L);
        comp.setCode("PL");
        comp.setName("Premier League");

        Team team1 = new Team();
        team1.setId(11L);
        team1.setName("Bayern Monachium");
        team1.setTla("FCB");
        
        Team team2 = new Team();
        team2.setId(12L);
        team2.setName("FC Barcelona");
        team2.setTla("FCB");

        when(event.getSubcommandName()).thenReturn("next");
        when(event.getOption("query")).thenReturn(optionMapping);
        when(optionMapping.getAsString()).thenReturn("FCB");
        when(teamService.getTeam("FCB")).thenReturn(List.of(team1, team2));
        
        when(event.reply(anyString())).thenReturn(replyAction);
        when(replyAction.setEphemeral(true)).thenReturn(replyAction);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        command.execute(event);

        verify(event).reply(captor.capture());
        verify(replyAction).queue();
        
        String message = captor.getValue();

        assertThat(message).doesNotContain("t:1786125600:f")
                .contains("**Conflict!** Found multiple teams for `")
                .contains("FCB")
                .contains("Bayern Monachium")
                .contains("FC Barcelona")
                .doesNotContain("null");
    }

    @Test
    void shouldReplyWithFormattedMatchesForCompetition() {
        Competition comp = new Competition();
        comp.setId(1L);
        comp.setCode("PL");
        comp.setName("Premier League");

        Team team1 = new Team();
        team1.setId(11L);
        team1.setName("Real Madrid");
        
        Team team2 = new Team();
        team2.setId(12L);
        team2.setName("FC Barcelona");

        Match match = new Match();
        match.setCompetition(comp);
        match.setHomeTeam(team1);
        match.setAwayTeam(team2);
        match.setTime("2026-08-07T18:00:00Z");
        match.setId(100L);
        match.setStatus("TIMED");
        
        when(event.getSubcommandName()).thenReturn("next");
        when(event.getOption("query")).thenReturn(optionMapping);
        when(optionMapping.getAsString()).thenReturn("PL");
        when(teamService.getTeam("PL")).thenReturn(Collections.emptyList());
        when(competitionService.getCompetition("PL")).thenReturn(comp);
        
        when(matchService.getNextMatches(comp)).thenReturn(List.of(match));
        when(event.reply(anyString())).thenReturn(replyAction);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        command.execute(event);

        verify(event).reply(captor.capture());
        verify(replyAction).queue();
        
        String message = captor.getValue();

        assertThat(message).contains("t:1786125600:f")
                .contains("Premier League")
                .contains("PL")
                .contains("Real Madrid")
                .contains("FC Barcelona")
                .doesNotContain("null");
    }

    @Test
    void shouldReplyWithListOfCompetitionsWhenMatchQueryDoesntMatchTeamOrCompetition() {
        Competition comp1 = new Competition();
        comp1.setId(1L);
        comp1.setCode("PL");
        comp1.setName("Premier League");

        Competition comp2 = new Competition();
        comp2.setId(2L);
        comp2.setCode("PD");
        comp2.setName("Primera Division");

        when(event.getSubcommandName()).thenReturn("next");
        when(event.getOption("query")).thenReturn(optionMapping);
        when(optionMapping.getAsString()).thenReturn("BB");
        when(teamService.getTeam("BB")).thenReturn(Collections.emptyList());
        when(competitionService.getCompetition("BB")).thenThrow(new IllegalArgumentException("League 'BB' not found"));
        when(competitionService.getCompetitions()).thenReturn(List.of(comp1, comp2));
        
        when(event.reply(anyString())).thenReturn(replyAction);
        when(replyAction.setEphemeral(true)).thenReturn(replyAction);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        command.execute(event);

        verify(event).reply(captor.capture());
        verify(replyAction).queue();
        
        String message = captor.getValue();

        assertThat(message).contains("Neither Team nor League found for")
                .contains("Available Competitions: ")
                .contains("BB")
                .contains("Premier League")
                .contains("PL")
                .contains("Primera Division")
                .contains("PD")
                .doesNotContain("null");
    }

    @Test
    void shouldReplyWithTodayMatchesWhenQueryIsAnyForNext() {
        when(event.getSubcommandName()).thenReturn("next");
        when(event.getOption("query")).thenReturn(null); // Brak argumentu = domyślnie "any"
        
        when(matchService.getTodayMatches()).thenReturn(Collections.emptyList());
        when(event.reply(anyString())).thenReturn(replyAction);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        command.execute(event);

        verify(event).reply(captor.capture());
        verify(replyAction).queue();
        
        assertThat(captor.getValue()).contains("Today's matches");
    }

    @Test
    void shouldReplyWithLast24hMatchesWhenQueryIsAnyForLast() {
        when(event.getSubcommandName()).thenReturn("last");
        when(event.getOption("query")).thenReturn(null);
        
        Match dummyMatch = new Match();
        dummyMatch.setStatus("FINISHED");
        dummyMatch.setHomeTeam(new Team());
        dummyMatch.setAwayTeam(new Team());
        dummyMatch.setCompetition(new Competition());
        dummyMatch.setTime("2026-08-07T18:00:00Z"); // Potrzebne do formatera

        when(matchService.getLast24hMatches()).thenReturn(List.of(dummyMatch));
        when(event.reply(anyString())).thenReturn(replyAction);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        command.execute(event);

        verify(event).reply(captor.capture());
        verify(replyAction).queue();
        
        assertThat(captor.getValue()).contains("Matches finished in the **last 24 hours**");
    }

    @Test
    void shouldReplyWithEmptyMessageForLast24hIfNoMatches() {
        when(event.getSubcommandName()).thenReturn("last");
        when(event.getOption("query")).thenReturn(null);
        
        when(matchService.getLast24hMatches()).thenReturn(Collections.emptyList());
        when(event.reply(anyString())).thenReturn(replyAction);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        command.execute(event);

        verify(event).reply(captor.capture());
        verify(replyAction).queue();
        
        assertThat(captor.getValue()).contains("No matches finished in the **last 24 hours**");
    }

    @Test
    void shouldReplyWithLiveMatches() {
        when(event.getSubcommandName()).thenReturn("live");
        
        Match dummyMatch = new Match();
        dummyMatch.setStatus("LIVE");
        dummyMatch.setHomeTeam(new Team());
        dummyMatch.setAwayTeam(new Team());
        dummyMatch.setCompetition(new Competition());
        dummyMatch.setTime("2026-08-07T18:00:00Z");

        when(matchService.getLiveMatches()).thenReturn(List.of(dummyMatch));
        when(event.reply(anyString())).thenReturn(replyAction);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        command.execute(event);

        verify(event).reply(captor.capture());
        verify(replyAction).queue();
        
        assertThat(captor.getValue()).contains("Live matches:");
    }

    @Test
    void shouldReplyWithNoLiveMatchesMessageIfEmpty() {
        when(event.getSubcommandName()).thenReturn("live");
        
        when(matchService.getLiveMatches()).thenReturn(Collections.emptyList());
        when(event.reply(anyString())).thenReturn(replyAction);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        command.execute(event);

        verify(event).reply(captor.capture());
        verify(replyAction).queue();
        
        assertThat(captor.getValue()).contains("No matches are live right now");
    }

    @Test
    void shouldAskForSubcommandWhenNull() {
        when(event.getSubcommandName()).thenReturn(null);
        
        when(event.reply(anyString())).thenReturn(replyAction);
        when(replyAction.setEphemeral(true)).thenReturn(replyAction);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        command.execute(event);

        verify(event).reply(captor.capture());
        verify(replyAction).queue();
        
        assertThat(captor.getValue()).contains("Choose from (next/last/live)");
    }
}