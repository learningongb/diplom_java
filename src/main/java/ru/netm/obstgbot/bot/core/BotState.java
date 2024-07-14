package ru.netm.obstgbot.bot.core;

public enum BotState {
    START,
    MAIN_MENU,
    FIND_NOTES,
    FIND_TAGS,
    WAIT_SEARCH_STRING,
    WAIT_SEARCH_STRING_TAG,
    WAIT_NOTE_NUMBER,
    WAIT_TAG_NUMBER,
    END
}
