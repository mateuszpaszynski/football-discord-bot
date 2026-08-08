package com.mycompany.app;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import net.dv8tion.jda.api.requests.GatewayIntent;

@Component
public class BotStarter implements CommandLineRunner {

    @Value("${bot.token}")
    private String botToken;

    private final MyListener myListener;

    public BotStarter(MyListener myListener) {
        this.myListener = myListener;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Odpalam Hermesa...");
        
        JDA api = JDABuilder.createDefault(botToken)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(myListener)
                .build();
                
        System.out.println("Hermes połączony z Discordem!");
    }
}