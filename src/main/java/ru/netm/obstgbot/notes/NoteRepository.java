package ru.netm.obstgbot.notes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.netm.obstgbot.bot.fileSystem.File;
import ru.netm.obstgbot.bot.fileSystem.FileObject;
import ru.netm.obstgbot.bot.fileSystem.Folder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class NoteRepository {

    private final List<String> paths;
    private List<FileObject> fileObjectList;
    private List<String> tagList;
    private List<FileObject> noteList;

    public void updateNoteList() {
        Note.eraseNoteCounter();
        fileObjectList = FileObject.getRoot();
        noteList = new ArrayList<>();
        tagList = new ArrayList<>();
        java.io.File file;
        for (String path : paths) {
            file = new java.io.File(path);
            filesInFolder(file, fileObjectList);
        }
    }

    private void filesInFolder(java.io.File file, List<FileObject> result) {
        if (file.isDirectory()) {
            Folder newFolder = new Folder(file.getName(), file.getPath());
            result.add(newFolder);
            List<FileObject> filesInFolder = FileObject.getRoot();
            for (java.io.File includedFile : file.listFiles()) {
                filesInFolder(includedFile, filesInFolder);
            }
            newFolder.setFiles(filesInFolder);
        } else {
            String fileName = file.getName();
            Note note = new Note(fileName, getTextNoteFromFile(file.getPath()));
            if (fileName.endsWith(".md") || fileName.endsWith(".MD")) {
                fileName = fileName.substring(1, fileName.length() - 3);
            }
            File newFile = new File(fileName, file.getPath());
            newFile.setNote(note);
            for (String tag : note.getTags()) {
                if (!tagList.contains(tag)) {
                    tagList.add(tag);
                }
            }
            result.add(newFile);
            noteList.add(newFile);
        }
    }

    public List<FileObject> listNotes() {
        return List.copyOf(fileObjectList);
    }

    public Optional<Note> getNoteByNumber(int number) {
        return Optional.of(((File)noteList.get(number - 1)).getNote());
    }

    public List<String> listTags() {
        return List.copyOf(tagList);
    }

    public List<FileObject> findNotesByTag(String tag) {
        if (tag.startsWith("/")) {
            tag = tag.substring(1);
        }
        log.info(tag);
        List<FileObject> result = new ArrayList<>();
        for (FileObject fileObject : noteList) {
            Note note = ((File)fileObject).getNote();
            if (note.getTags().contains(tag)) {
                result.add(fileObject);
            }
        }
        return result;
    }

    public List<FileObject> findNotesByString(String pattern) {
        List<FileObject> result = new ArrayList<>();
        for (FileObject fileObject : noteList) {
            Note note = ((File)fileObject).getNote();
            if (note.getTitle().contains(pattern) || note.getContent().contains(pattern)) {
                result.add(fileObject);
            }
        }
        return result;
    }

    private String getTextNoteFromFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName)))  {
            StringBuilder sb = new StringBuilder();

            String value;
            while ((value = reader.readLine()) != null) {
                sb.append(value).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            return "";
        }
    }

}
