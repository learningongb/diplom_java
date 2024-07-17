package ru.netm.obstgbot.bot.fileSystem;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
abstract public class FileObject {
    private String name;
    private String path;
    abstract public boolean isFolder();

    public static List<FileObject> getRoot() {
        return new ArrayList<>();
    };
}
