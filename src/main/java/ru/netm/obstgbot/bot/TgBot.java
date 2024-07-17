package ru.netm.obstgbot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.netm.obstgbot.bot.core.BotCommand;
import ru.netm.obstgbot.bot.core.BotEvent;
import ru.netm.obstgbot.bot.core.BotState;
import ru.netm.obstgbot.bot.fileSystem.File;
import ru.netm.obstgbot.bot.fileSystem.FileObject;
import ru.netm.obstgbot.bot.fileSystem.Folder;
import ru.netm.obstgbot.notes.Note;
import ru.netm.obstgbot.bot.users.User;
import ru.netm.obstgbot.bot.users.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TgBot extends TelegramLongPollingBot {

    @Autowired
    private TgBotConfiguration configuration;
    @Autowired
    private UserRepository userRepository;

    private static final String HELPTEXT =
            """
            Описание команд:
            /ln - список заметок,
            /lt - список тэгов,
            /f - найти заметку,
            /v - выбор директории,
            /t - найти по тэгу
            """;

    public TgBot(TgBotConfiguration configuration, UserRepository userRepository) {
        super(configuration.getToken());
        this.configuration = configuration;
        this.userRepository = userRepository;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        Message message = update.getMessage();
        String text = message.getText();
        Long chatId = message.getChatId();
        Optional<User> currentUser = userRepository.getUser(message, this);
        if (currentUser.isEmpty()) {
            sendMessage(chatId, "Доступ запрещен");
            return;
        }
        StateMachine<BotState, BotEvent> stateMachine = currentUser.get().getStateMachine();
        State<BotState, BotEvent> currentState = stateMachine.getState();
        BotCommand command;
        if (currentState == null) {
            command = BotCommand.START;
        } else {
            var stateId = currentState.getId();
            if (stateId == BotState.START) {
                stateMachine.getExtendedState().getVariables().put("searchString", text);
                command = BotCommand.START;
            } else if (stateId == BotState.FIND_NOTES) {
                stateMachine.getExtendedState().getVariables().put("searchString", text);
                command = BotCommand.ENTER_SEARCH_STRING;
            } else if (stateId == BotState.FIND_TAGS) {
                stateMachine.getExtendedState().getVariables().put("searchString", text);
                command = BotCommand.ENTER_SEARCH_STRING_TAG;
            } else if (stateId == BotState.MAIN_MENU) {
                command = getCommandFromText(text);
                if (command == BotCommand.UNKNOWN) {
                    try {
                        stateMachine.getExtendedState().getVariables().put("noteId", Integer.parseInt(text));
                        command = BotCommand.PRINT_NOTE;
                    } catch (NumberFormatException e) {
                        command = BotCommand.UNKNOWN;
                    }
                    if (command == BotCommand.UNKNOWN) {

                    }
                }
            } else {
                command = getCommandFromText(text);
            }
        }

        switch (command) {
            case START -> stateMachine.sendEvent(BotEvent.START);
            case PRINT_NOTE -> stateMachine.sendEvent(BotEvent.OPEN_NOTE);
            case LIST_NOTES -> stateMachine.sendEvent(BotEvent.LIST_NOTES);
            case LIST_TAGS -> stateMachine.sendEvent(BotEvent.LIST_TAGS);
            case BACK -> stateMachine.sendEvent(BotEvent.BACK);
            case FIND_NOTES -> stateMachine.sendEvent(BotEvent.FIND_NOTES);
            case FIND_TAGS -> stateMachine.sendEvent(BotEvent.FIND_TAGS);
            case ENTER_SEARCH_STRING -> stateMachine.sendEvent(BotEvent.ENTER_SEARCH_STRING);
            case ENTER_SEARCH_STRING_TAG -> stateMachine.sendEvent(BotEvent.ENTER_SEARCH_STRING_TAG);
            case HELP -> stateMachine.sendEvent(BotEvent.HELP);
            case UNKNOWN -> sendMessage(chatId, "Unknown command");
        }


    }

    @Override
    public String getBotUsername() {
        return configuration.getName();
    }

    private BotCommand getCommandFromText(String textCommand) {
        if ("/ln".equalsIgnoreCase(textCommand)) {
            return BotCommand.LIST_NOTES;
        } else if ("/lt".equalsIgnoreCase(textCommand)) {
            return BotCommand.LIST_TAGS;
        } else if ("/b".equalsIgnoreCase(textCommand)) {
            return BotCommand.BACK;
        } else if ("/f".equalsIgnoreCase(textCommand)) {
            return BotCommand.FIND_NOTES;
        } else if ("/t".equalsIgnoreCase(textCommand)) {
            return BotCommand.FIND_TAGS;
        } else if ("/h".equalsIgnoreCase(textCommand)) {
            return BotCommand.HELP;
        } else {
            return BotCommand.UNKNOWN;
        }
    }

    public void printWelcome(Long chatId, User user) {
        sendMessage(chatId, "Привет, " + user.getUserName() + "!");
    }

    public void printMainMenu(Long chatId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("/ln");
        keyboardRow.add("/lt");
        keyboardRow.add("/f");
        keyboardRow.add("/t");

        replyKeyboardMarkup.setKeyboard(List.of(keyboardRow));
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        sendMessage(chatId, HELPTEXT, replyKeyboardMarkup, null);
    }

    public void printNoteList(Long chatId, List<FileObject> fileList) {
        StringBuilder sb = new StringBuilder();
        addFiles(sb, fileList, 0);
        sendMessage(chatId, sb.toString());
    }

    public void printNote(Long chatId, Optional<Note> noteOptional) {
        if (noteOptional.isEmpty()) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        Note note = noteOptional.get();
        builder.append(note.getTitle());
        builder.append("\n");
        builder.append("\n");
        builder.append(note.getContent());
        if (!sendMessage(chatId, builder.toString(), null, ParseMode.MARKDOWN)) {
            sendMessage(chatId, builder.toString());
        }
        ;
        sendMessage(chatId, note.getTags().toString());
    }

    public void pringTags(Long chatId, List<String> tags) {
        StringBuilder sb = new StringBuilder();
        for (String tag : tags) {
            sb.append(tag).append("\n");
        }
        sendMessage(chatId, sb.toString());
    }

    public void pringTagsButton(Long chatId, List<String> tags) {
        StringBuilder sb = new StringBuilder();
        for (String tag : tags) {
            sb.append("/").append(tag).append("\n");
        }
        sendMessage(chatId, sb.toString());
    }

    public void printSearchPrompt(Long chatId) {
        sendMessage(chatId, "Введите строку поиска");
    }

    private void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, null, null);
    }

    private void addFiles(StringBuilder sb, List<FileObject> fileList, int level) {
        String emptyNumber = "  ";
        String prefix = getPrefixForFilesString(level);
        for (FileObject fileObject : fileList) {
            if (fileObject.isFolder()) {
                sb.append(emptyNumber)
                        .append(". ").append(prefix)
                        .append(fileObject.getName());
            } else {
                sb.append(String.format("%d", ((File)fileObject).getNote().getNumber()))
                        .append(" ").append(prefix)
                        .append(fileObject.getName());
            }
            sb.append("\n");
            if (fileObject.isFolder()) {
                addFiles(sb, ((Folder)fileObject).getFiles(), level + 1);
            }
        }
    }

    private String getPrefixForFilesString(int level) {
        if (level == 0) {
            return "";
        }
        String result = "";
        for (int i = 2; i < level; i++) {
            result = result.concat(" ");
        }
        String symbol1 = "└";
        result = result + symbol1 + " ";
        return result;
    }

    private boolean sendMessage(Long chatId, String text, ReplyKeyboardMarkup keyboardMarkup, String parseMode) {

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        if (parseMode != null) {
            message.setParseMode(parseMode);
        }
        if (keyboardMarkup != null) {
            message.setReplyMarkup(keyboardMarkup);
        }
        try {
            execute(message);
            return true;
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения: " + e.getMessage());
            return false;
        }
    }

}
