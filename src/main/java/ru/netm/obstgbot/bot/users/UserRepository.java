package ru.netm.obstgbot.bot.users;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.netm.obstgbot.bot.TgBot;
import ru.netm.obstgbot.bot.core.BotEvent;
import ru.netm.obstgbot.bot.core.BotState;
import ru.netm.obstgbot.notes.NoteRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Data
@Component
public class UserRepository {

    @Autowired
    private StateMachineFactory<BotState, BotEvent> stateMachineFactory;
    @Autowired
    private UserConfigurationSpring userConfigurationSpring;

    private Map<Long, User> users = new HashMap<>();

    public Optional<User> getUser(Message message, TgBot tgBot) {
        var tgUser = message.getFrom();
        User user = users.get(tgUser.getId());
        if (user != null) {
            return Optional.of(user);
        }
        Long userId = tgUser.getId();
        List<String> currentUserPath = userPath(userId.toString());
        if (currentUserPath == null) {
            log.info(String.format("Запрещен доступ пользователя %s %s", userId.toString(), tgUser.getUserName()));
            return Optional.empty();
        }

        NoteRepository noteRepository = new NoteRepository(currentUserPath);

        user = new User(tgUser);
        var stateMachine = stateMachineFactory.getStateMachine();
        var machineVariables = stateMachine.getExtendedState().getVariables();
        machineVariables.put("chatId", message.getChatId());
        machineVariables.put("tgBot", tgBot);
        machineVariables.put("user", user);
        user.setStateMachine(stateMachine);
        user.setNoteRepository(noteRepository);
        users.put(tgUser.getId(), user);
        stateMachine.start();
        log.info("Подключился пользователь id=" + tgUser.getId());
        return Optional.of(user);
    }

    private List<String> userPath(String userId) {

        Map<String, List<String>> users = userConfigurationSpring.getUsers();

        if (users != null) {
            return users.get(userId);
        }

        String envUsersFile = System.getenv("bot_user_" + userId);
        if (envUsersFile != null && !envUsersFile.isEmpty()) {
            return List.of(envUsersFile.split(";"));
        }

        envUsersFile = System.getenv("bot_user_default");
        if (envUsersFile != null && !envUsersFile.isEmpty()) {
            return List.of(envUsersFile.split(";"));
        }


        return null;
    }

}
