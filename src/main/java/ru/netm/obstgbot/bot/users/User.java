package ru.netm.obstgbot.bot.users;

import lombok.Data;
import org.springframework.statemachine.StateMachine;
import ru.netm.obstgbot.bot.core.BotEvent;
import ru.netm.obstgbot.bot.core.BotState;
import ru.netm.obstgbot.bot.notes.NoteRepository;

import java.util.List;

@Data
public class User {

    private String userName;
    private String firstName;
    private StateMachine<BotState, BotEvent> stateMachine;
//    private List<String> paths;
    private NoteRepository noteRepository;

    User(org.telegram.telegrambots.meta.api.objects.User tgUser) {
        this.userName = tgUser.getUserName();
        this.firstName = tgUser.getFirstName();
    }
}
