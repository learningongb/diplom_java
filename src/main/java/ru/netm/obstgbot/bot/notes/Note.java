package ru.netm.obstgbot.bot.notes;

import lombok.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note {
    private String title;
    private List<String> tags;
    private String content;
    private int number;
    static private int currentNumber;

    Note(String title, String text) {
        this.title = title;
        this.content = text;
        this.tags = new ArrayList<>();
        this.number = ++currentNumber;

        Pattern pattern2 = Pattern.compile("\\s*-\\s*(\\S*)");
        Pattern pattern = Pattern.compile("(?s)---.*tags:(.*?)---");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String stringWithTags = matcher.group(1).toString();
            String[] lines = stringWithTags.split("\n");
            int index = 0;
            while (index < lines.length) {
                Matcher matcher1 = pattern2.matcher(lines[index++]);
                if (matcher1.find()) {
                    tags.add(matcher1.group(1).toString());
                }
            }
        }
    }

    static public void eraseNoteCounter() {
        currentNumber = 0;
    }

    static public String getTextNoteFromFile(String fileName) {
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
