package pl.mateuszpaszynski.footballtracker.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import pl.mateuszpaszynski.footballtracker.discord.command.StandingsCommand;
import pl.mateuszpaszynski.footballtracker.model.Competition;
import pl.mateuszpaszynski.footballtracker.model.Standing;
import pl.mateuszpaszynski.footballtracker.model.Team;
import pl.mateuszpaszynski.footballtracker.service.CompetitionService;
import pl.mateuszpaszynski.footballtracker.service.StandingService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.entities.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StandingsCommandTest {
    @Mock
    private CompetitionService competitionService;
    
    @Mock 
    private StandingService standingService;
    
    @Mock
    private OptionMapping optionMapping;
    
    @Mock
    private SlashCommandInteractionEvent event;
    
    @Mock
    private WebhookMessageCreateAction<Message> hookAction;

    @Mock
    private InteractionHook hook;

    @Mock 
    private ReplyCallbackAction replyAction;

    @InjectMocks
    private StandingsCommand command;

    @Test
    void shouldReplyWithFormattedStanding() {
        Competition comp = new Competition();
        comp.setId(1L);
        comp.setCode("PL");
        comp.setName("Premier League");

        Team team1 = new Team();
        team1.setId(11L);
        team1.setName("Real Madrid");
        
        Standing standing1 = new Standing();
        standing1.setCompetition(comp);
        standing1.setTeam(team1);
        standing1.setPosition(1);
        standing1.setGamesWon(3);
        standing1.setGamesLost(0);
        standing1.setGamesDrawn(1);
        standing1.setPoints(10);
        standing1.setPlayedGames(4);
        standing1.setGoalsFor(10);
        standing1.setGoalsAgainst(2);
        standing1.setGoalDifference(8);

        when(event.getOption(anyString())).thenReturn(optionMapping);
        when(optionMapping.getAsString()).thenReturn("PL");
        when(competitionService.getCompetition("PL")).thenReturn(comp);
        when(standingService.getStandings(comp)).thenReturn(List.of(standing1));
        when(event.reply(anyString())).thenReturn(replyAction);
        command.execute(event);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(event).reply(captor.capture());
        verify(replyAction).queue();
        
        String message = captor.getValue();

        assertThat(message).contains("Premier League")
        .contains("Real Madrid")
        .doesNotContain("null");
    }

    @Test
    void shouldSplitReplyWhenThereAreMoreThan20Teams() {
        
        Competition comp = new Competition();
        comp.setId(1L);
        comp.setCode("PL");
        comp.setName("Premier League");


        List<Standing> standings = new ArrayList<>();
        for (int i = 1; i <= 21; i++) {
            
            Team team = new Team();
            team.setId((10L + i));
            team.setName("Real Madrid " + i);
            
            Standing standing1 = new Standing();
            standing1.setCompetition(comp);
            standing1.setTeam(team);
            standing1.setPosition(i);
            standing1.setGamesWon(38 - i);
            standing1.setGamesLost(i);
            standing1.setGamesDrawn(0);
            standing1.setPoints(3 * (38 - i));
            standing1.setPlayedGames(38);
            standing1.setGoalsFor(100 - 5 * i);
            standing1.setGoalsAgainst(5 * i);
            standing1.setGoalDifference(100 - 10 * i);
            standings.add(standing1);
        }
        
        when(event.getOption(anyString())).thenReturn(optionMapping);
        when(optionMapping.getAsString()).thenReturn("PL");
        when(competitionService.getCompetition("PL")).thenReturn(comp);
        when(standingService.getStandings(comp)).thenReturn(standings);
        when(event.reply(anyString())).thenReturn(replyAction);
        

        when(event.getHook()).thenReturn(hook);
        when(hook.sendMessage(anyString())).thenReturn(hookAction);

        command.execute(event);
        
        ArgumentCaptor<String> replyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<java.util.function.Consumer<InteractionHook>> lambdaCaptor = ArgumentCaptor.forClass(java.util.function.Consumer.class);
        
        verify(event).reply(replyCaptor.capture());
        
        verify(replyAction).queue(lambdaCaptor.capture()); 
        
        lambdaCaptor.getValue().accept(hook);
        
        
        ArgumentCaptor<String> hookCaptor = ArgumentCaptor.forClass(String.class);
        verify(hook).sendMessage(hookCaptor.capture());
        verify(hookAction).queue();
        
        String firstMessage = replyCaptor.getValue();
        String secondMessage = hookCaptor.getValue();

        assertThat(firstMessage)
            .contains("Real Madrid 1")
            .doesNotContain("null");
            
        assertThat(secondMessage)
            .contains("Real Madrid 21")
            .doesNotContain("null");
    }

    @Test
    void shouldReplyWithListOfCompetitionsWhenStandingsQueryDoesntMatchAnyCompetition() {
        
        Competition comp1 = new Competition();
        comp1.setId(1L);
        comp1.setName("Premier League");
        comp1.setCode("PL");

        Competition comp2 = new Competition();
        comp2.setId(2L);
        comp2.setName("Primera Division");
        comp2.setCode("PD");

        when(event.getOption(anyString())).thenReturn(optionMapping);
        when(optionMapping.getAsString()).thenReturn("BB");
        when(competitionService.getCompetition("BB")).thenThrow(new IllegalArgumentException("League BB not found."));
        when(competitionService.getCompetitions()).thenReturn(List.of(comp1,comp2));
        
        when(event.reply(anyString())).thenReturn(replyAction);
        when(replyAction.setEphemeral(true)).thenReturn(replyAction);
        
        command.execute(event);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(event).reply(captor.capture());
        verify(replyAction).queue();

        String message = captor.getValue();
        
        assertThat(message).contains("Error")
        .contains("Available Competitions: ")
        .contains("BB")
        .contains("Primera Division")
        .contains("PD")
        .contains("Premier League")
        .contains("PL")
        .doesNotContain("null");
    }
}