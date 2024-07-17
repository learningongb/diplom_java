package ru.netm.obstgbot.bot.fileSystem;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Folder extends FileObject {

    @Getter
    @Setter
    private List<FileObject> files = new ArrayList<>();

    public Folder(String name, String path) {
        super(name, path);
    }

    @Override
    public boolean isFolder() {
        return true;
    }
}
