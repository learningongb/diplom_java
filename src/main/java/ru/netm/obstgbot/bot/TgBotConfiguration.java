package ru.netm.obstgbot.bot;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@ConfigurationProperties(prefix = "bot")
public class TgBotConfiguration {
    @Getter
    private String token;
    @Getter
    @Setter
    private String name;

    public void setToken(String token) {
        if (!token.isEmpty()) {
            this.token = token;
        } else {
            String envToken = System.getenv("bot_token");
            if (!envToken.isEmpty()) {
                this.token = envToken;
            } else {
                throw new RuntimeException("Bot token not found in environment");
            }
        }



    }

    @Bean
    public TelegramBotsApi telegramBotsApi(TgBot bot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        return api;
    }

}
