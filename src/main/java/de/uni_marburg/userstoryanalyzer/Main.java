package de.uni_marburg.userstoryanalyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_marburg.userstoryanalyzer.model.FileStoryReader;
import de.uni_marburg.userstoryanalyzer.model.UserStory;
import de.uni_marburg.userstoryanalyzer.parser.StoryParserOpenNLP;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        StoryParserOpenNLP parser = new StoryParserOpenNLP();
        FileStoryReader fileReader = new FileStoryReader();
        ObjectMapper mapper = new ObjectMapper();

        // Beispiel 1: Einzelne User Story von der Kommandozeile
        if (args.length == 0) {
            processSingleExample(parser, mapper);
        }
        // Beispiel 2: Datei mit mehreren User Stories verarbeiten
        else {
            String filePath = args[0];
            processStoryFile(parser, fileReader, mapper, filePath);
        }
    }

    private static void processSingleExample(StoryParserOpenNLP parser, ObjectMapper mapper) throws Exception {
        String input = "As a Plan Review Staff member, I want to Assign Plans for Review, so that I can ensure plans have been assigned to the appropriate Plan Reviewer.\n";

        UserStory userStory = parser.parse(input);

        String json = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(List.of(userStory));
        System.out.println(json);
    }

    private static void processStoryFile(StoryParserOpenNLP parser, FileStoryReader fileReader,
                                         ObjectMapper mapper, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        List<String> storyTexts = fileReader.readStoriesFromFile(path);

        List<UserStory> userStories = new ArrayList<>();
        for (String storyText : storyTexts) {
            try {
                UserStory userStory = parser.parse(storyText);
                userStories.add(userStory);
            } catch (Exception e) {
                System.err.println("Fehler beim Parsen der Story: " + storyText);
                e.printStackTrace();
            }
        }

        // Ausgabe als JSON
        String json = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(userStories);
        System.out.println(json);

        // Optional: In Datei speichern
        String outputPath = filePath.replace(".txt", ".json");
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(Paths.get(outputPath).toFile(), userStories);
        System.out.println("\nErgebnisse wurden gespeichert in: " + outputPath);
    }
}