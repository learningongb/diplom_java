package ru.netm.obstgbot.bot.fileSystem;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.netm.obstgbot.bot.notes.Note;

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
