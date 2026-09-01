package pl.mateuszpaszynski.footballtracker.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;


public interface BotCommand {
    
    String getName(); 
    void execute(SlashCommandInteractionEvent event); 
    CommandData getCommandData();
}