package de.uni_marburg.userstoryanalyzer.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Liest User Stories aus einer Textdatei und gibt sie als Liste zurück.
 */
public class FileStoryReader {

    /**
     * Liest User Stories aus einer Textdatei.
     *
     * @param filePath Pfad zur Textdatei
     * @return Liste der User Stories (jede Zeile ist eine separate Story)
     * @throws IOException Wenn die Datei nicht gelesen werden kann
     */
    public List<String> readStoriesFromFile(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        return extractStories(content);
    }

    /**
     * Extrahiert einzelne User Stories aus dem Text.
     *
     * @param content Der gesamte Textinhalt
     * @return Liste der einzelnen User Stories
     */
    private List<String> extractStories(String content) {
        List<String> stories = new ArrayList<>();

        // Pattern für User Stories (beginnt mit "As a" oder "As an")
        Pattern storyPattern = Pattern.compile("(As (a|an) .*?(?=\\n\\s*As (a|an) |$))", Pattern.DOTALL);
        Matcher matcher = storyPattern.matcher(content);

        while (matcher.find()) {
            String story = matcher.group(1).trim();
            // Entferne eventuelle Nummerierungen am Anfang (z.B. "1. ")
            story = story.replaceAll("^\\d+\\.\\s*", "");
            stories.add(story);
        }

        // Falls keine Stories mit dem Pattern gefunden wurden, aber der Text existiert
        if (stories.isEmpty() && !content.trim().isEmpty()) {
            // Versuche, nach Zeilenumbrüchen zu trennen
            String[] lines = content.split("\\r?\\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    stories.add(line.trim());
                }
            }
        }

        return stories;
    }
}