package de.uni_marburg.userstoryanalyzer;

import de.uni_marburg.userstoryanalyzer.export.JsonExporter;
import de.uni_marburg.userstoryanalyzer.model.UserStory;
import de.uni_marburg.userstoryanalyzer.parser.StoryParserOpenNLP;

import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        StoryParserOpenNLP parser = new StoryParserOpenNLP();
        String filePath = "src/main/resources/models/g03-loudoun.txt";
        List<UserStory> userStories;
        userStories = parser.parseFromFile(filePath);
        JsonExporter exporter = new JsonExporter();
        // Datei im Projektverzeichnis erzeugen, damit sie sichtbar ist
        File outputFile = new File("src/main/resources/json-files/userstories.json");
        exporter.writeToJson(userStories, outputFile.getAbsolutePath());

        for (UserStory userStory : userStories ){
            System.out.println(userStory.toString());
        }

    }
}