package com.mycompany.app.discord.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


public interface BotCommand {
    
    boolean supports(String command); 

    void execute(MessageReceivedEvent event); 
}