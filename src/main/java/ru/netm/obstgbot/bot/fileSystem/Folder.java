package ru.netm.obstgbot.bot.fileSystem;

import lombok.AllArgsConstructor;
import lombok.Data;
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

    public void addFile(File file) {
        files.add(file);
    }

    @Override
    public boolean isFolder() {
        return true;
    }
}