package pl.mateuszpaszynski.footballtracker.discord.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import pl.mateuszpaszynski.footballtracker.discord.command.TeamsCommand;
import pl.mateuszpaszynski.footballtracker.model.Competition;
import pl.mateuszpaszynski.footballtracker.model.Team;
import pl.mateuszpaszynski.footballtracker.service.CompetitionService;
import pl.mateuszpaszynski.footballtracker.service.TeamService;

import static org.mockito.Mockito.*;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class TeamsCommandTest {
    

    @Mock
    private CompetitionService competitionService;

    @Mock 
    private TeamService teamService;

    @Mock
    private SlashCommandInteractionEvent event;

    @Mock
    private ReplyCallbackAction replyAction;

    @Mock
    private OptionMapping optionMapping;

    @InjectMocks
    private TeamsCommand command;

    @Test
    void shouldReplyWithFormattedTeamsList() {
        Competition comp = new Competition();
        comp.setId(1L);
        comp.setCode("PL");
        comp.setName("Premier League");

        Team team = new Team();
        team.setId(2L);
        team.setName("Real Madrid CF");
        team.setTla("RMA");

        when(event.getOption(anyString())).thenReturn(optionMapping);
        when(optionMapping.getAsString()).thenReturn("PL");
        when(competitionService.getCompetition("PL")).thenReturn(comp);
        when(teamService.getTeams(comp)).thenReturn(List.of(team));
        when(event.reply(anyString())).thenReturn(replyAction);
        
        command.execute(event);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(event).reply(captor.capture());
        verify(replyAction).queue();
        String message = captor.getValue();
        assertThat(message).contains("Real Madrid CF")
        .contains("Premier League")
        .doesNotContain("null");
    }
    @Test
    void shouldReplyWithListOfCompetitionsWhenTeamsQueryDoesntMatchAnyCompetition() {

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
