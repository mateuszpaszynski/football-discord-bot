package pl.mateuszpaszynski.footballtracker.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordConfig {

    @Value("${bot.token}")
    private String botToken;

    @Bean
    public JDA jda(MyListener myListener) throws InterruptedException {
        JDA jda = JDABuilder.createDefault(botToken)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(myListener)
                .build()
                .awaitReady();
        return jda;
    }
}