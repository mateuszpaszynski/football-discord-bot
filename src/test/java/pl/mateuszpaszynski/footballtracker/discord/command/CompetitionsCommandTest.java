package pl.mateuszpaszynski.footballtracker.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import pl.mateuszpaszynski.footballtracker.model.Competition;
import pl.mateuszpaszynski.footballtracker.service.CompetitionService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompetitionsCommandTest {

    @Mock
    private CompetitionService competitionService;

    @Mock
    private SlashCommandInteractionEvent event;

    @Mock
    private ReplyCallbackAction replyAction;

    @InjectMocks
    private CompetitionsCommand command;


    @Test
    void shouldReplyWithFormattedCompetitionsList() {

        Competition comp1 = new Competition();
        comp1.setId(1L);
        comp1.setCode("PL");
        comp1.setName("Premier League");
        when(competitionService.getCompetitions()).thenReturn(List.of(comp1));
        
        when(event.reply(anyString())).thenReturn(replyAction);

        ArgumentCaptor<String> replyCaptor = ArgumentCaptor.forClass(String.class);
        command.execute(event);

        verify(competitionService).getCompetitions();

        verify(event).reply(replyCaptor.capture()); 
        verify(replyAction).queue();

        String actualReply = replyCaptor.getValue();

        assertTrue(actualReply.contains("Premier League")); 
    }
}