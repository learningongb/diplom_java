package ru.netm.obstgbot.bot.fileSystem;

import lombok.Data;
import ru.netm.obstgbot.notes.Note;

@Data
public class File extends FileObject {
    private Note note;
    @Override
    public boolean isFolder() {
        return false;
    }

    public File(String name, String path) {
        super(name, path);
    }
}
