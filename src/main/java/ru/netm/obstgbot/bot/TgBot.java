package ru.netm.obstgbot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class TgBot extends TelegramLongPollingBot {

    @Autowired
    private TgBotConfiguration configuration;

    public TgBot(TgBotConfiguration configuration) {
        super(configuration.getToken());
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        Message message = update.getMessage();
        String text = message.getText();
        Long chatId = message.getChatId();

    }

    @Override
    public String getBotUsername() {
        return configuration.getName();
    }
}
