package com.mycompany.app.discord.command;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Component
public class HelpCommand implements BotCommand{
    
    @Override
    public boolean supports(String command) {
        return command.equalsIgnoreCase("!help");
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        
    }

}
