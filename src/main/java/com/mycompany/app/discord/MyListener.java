package com.mycompany.app.discord;


import java.util.List;

import com.mycompany.app.discord.command.BotCommand;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import org.springframework.stereotype.Component;

@Component
public class MyListener extends ListenerAdapter {
    
    private final List<BotCommand> commands;
    
    public MyListener(List<BotCommand> commands) {
        this.commands = commands;
    }

    @Override
    public void onReady(ReadyEvent event) {
        List<CommandData> commandDataList = commands.stream()
            .map(BotCommand::getCommandData).toList();

        event.getJDA().updateCommands().addCommands(commandDataList).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        String commandName = event.getName(); 

        for (BotCommand command : commands) {
            if (command.getName().equalsIgnoreCase(commandName)) {
                command.execute(event);
                return;
            }
        }
    }
}