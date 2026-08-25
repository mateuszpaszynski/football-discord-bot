package com.mycompany.app.discord.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import com.mycompany.app.repository.StandingRepository;

import com.mycompany.app.discord.command.StandingsCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StandingsCommandTest {

    @Mock
    private StandingRepository standingRepository;

    @Mock
    private SlashCommandInteractionEvent event; 

    @Mock
    private ReplyCallbackAction replyCallbackAction;

    @InjectMocks
    private StandingsCommand standingsCommand;
    // @Test
    // void shouldReplyWithFormattedStandings() {
    //     when(event.reply(anyString())).thenReturn(replyCallbackAction);
        


    //     standingsCommand.execute(event);

    //     ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    //     verify(event).reply(messageCaptor.capture());
        
    //     // Sprawdzamy, czy bot na pewno kliknął "wyślij" (czyli .queue())
    //     verify(replyCallbackAction).queue();

    //     // Oceniamy, czy tekst wiadomości ma sens
    //     String sentMessage = messageCaptor.getValue();
    //     assertThat(sentMessage).contains("Manchester City");
    //     assertThat(sentMessage).contains("Punkty:");
    // }
}