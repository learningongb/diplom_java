package ru.netm.obstgbot.bot.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import ru.netm.obstgbot.bot.TgBot;
import ru.netm.obstgbot.notes.Note;
import ru.netm.obstgbot.notes.NoteRepository;
import ru.netm.obstgbot.bot.users.User;

import java.util.EnumSet;
import java.util.Optional;

@Slf4j
@EnableStateMachineFactory
public class BotStateMachine extends EnumStateMachineConfigurerAdapter<BotState, BotEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<BotState, BotEvent> states) throws Exception {
        states
                .withStates()
                .initial(BotState.START, this::botAction).end(BotState.END).states(EnumSet.allOf(BotState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<BotState, BotEvent> transitions) throws Exception {
        transitions
                .withExternal()
                .source(BotState.START).target(BotState.MAIN_MENU).event(BotEvent.START).action(this::botAction)
                /**
                 * Помощь
                 */
                .and().withExternal()
                .source(BotState.MAIN_MENU).target(BotState.MAIN_MENU).event(BotEvent.HELP).action(this::botAction)
                /**
                 * Список заметок
                 */
                .and().withExternal()
                .source(BotState.MAIN_MENU).target(BotState.MAIN_MENU).event(BotEvent.LIST_NOTES).action(this::botAction)
                /**
                 * Открыть заметку
                 */
                .and().withExternal()
                .source(BotState.MAIN_MENU).target(BotState.MAIN_MENU).event(BotEvent.OPEN_NOTE)
                .action(this::botAction)
                /**
                 * Поиск заметок
                 */
                .and().withExternal()
                .source(BotState.MAIN_MENU).target(BotState.FIND_NOTES).event(BotEvent.FIND_NOTES).action(this::botAction)
                .and().withExternal()
                .source(BotState.FIND_NOTES).target(BotState.MAIN_MENU).event(BotEvent.BACK).action(this::botAction)
                .and().withExternal()
                .source(BotState.FIND_NOTES).target(BotState.MAIN_MENU).event(BotEvent.ENTER_SEARCH_STRING).action(this::botAction)
                /**
                 * Список тэгов
                 */
                .and().withExternal()
                .source(BotState.MAIN_MENU).target(BotState.MAIN_MENU).event(BotEvent.LIST_TAGS).action(this::botAction)
                /**
                 * Поиск по тэгу
                 */
                .and().withExternal()
                .source(BotState.MAIN_MENU).target(BotState.FIND_TAGS).event(BotEvent.FIND_TAGS).action(this::botAction)
                .and().withExternal()
                .source(BotState.FIND_TAGS).target(BotState.MAIN_MENU).event(BotEvent.BACK).action(this::botAction)
                .and().withExternal()
                .source(BotState.FIND_TAGS).target(BotState.MAIN_MENU).event(BotEvent.ENTER_SEARCH_STRING_TAG)
                .action(this::botAction)
        ;

    }

    public void botAction(StateContext<BotState, BotEvent> context) {
        ExtendedState extendedState = context.getExtendedState();
        TgBot tgBot = extendedState.get("tgBot", TgBot.class);
        Long chatId = extendedState.get("chatId", Long.class);
        User user = extendedState.get("user", User.class);
        if (context.getSource() == null) {
            log.info("Стартует конечный автомат");
            tgBot.printWelcome(chatId, user);
        } else {
            BotEvent payLoad = context.getMessage().getPayload();
            log.info("Событие: " + payLoad);
            log.info("Новое состояние " + context.getTarget().toString());
            NoteRepository noteRepository = user.getNoteRepository();
            switch (payLoad) {
                case START:
                    noteRepository.updateNoteList();
                    tgBot.printMainMenu(chatId);
                    break;
                case LIST_NOTES:
                    noteRepository.updateNoteList();
                    tgBot.printNoteList(chatId, noteRepository.listNotes());
                    break;
                case OPEN_NOTE:
                    Optional<Note> note = noteRepository.getNoteByNumber(extendedState.get("noteId", Integer.class));
                    tgBot.printNote(chatId, note);
                    break;
                case LIST_TAGS:
                    tgBot.pringTags(chatId, noteRepository.listTags());
                    break;
                case FIND_TAGS:
                    tgBot.pringTagsButton(chatId, noteRepository.listTags());
                    break;
                case ENTER_SEARCH_STRING_TAG:
                    tgBot.printNoteList(chatId, noteRepository.findNotesByTag(extendedState.get("searchString", String.class)));
                    break;
                case FIND_NOTES:
                    tgBot.printSearchPrompt(chatId);
                    break;
                case ENTER_SEARCH_STRING:
                    tgBot.printNoteList(chatId, noteRepository.findNotesByString(extendedState.get("searchString", String.class)));
                    break;
            }

        }
    }

}
