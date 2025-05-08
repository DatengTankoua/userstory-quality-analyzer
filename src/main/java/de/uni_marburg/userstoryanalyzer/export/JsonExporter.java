package de.uni_marburg.userstoryanalyzer.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_marburg.userstoryanalyzer.model.UserStory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Die Klasse JsonExporter dient zum Exportieren einer Liste von User Stories in eine JSON-Datei.
 *
 * Sie verwendet Jacksons {@link ObjectMapper}, um Java-Objekte im JSON-Format zu serialisieren.
 * Die JSON-Ausgabe ist dabei formatiert (pretty-printed) für bessere Lesbarkeit.
 *
 * Beispielverwendung:
 * <pre>{@code
 *     JsonExporter exporter = new JsonExporter();
 *     exporter.writeToJson(userStoryList, "output/userstories.json");
 * }</pre>
 */
public class JsonExporter {

    /**
     * Schreibt eine Liste von {@link UserStory}-Objekten in eine JSON-Datei am angegebenen Pfad.
     *
     * @param userStories Liste der zu exportierenden User Stories
     * @param filePath    Zielpfad für die Ausgabedatei
     * @throws IOException Falls beim Schreiben der Datei ein Fehler auftritt
     */
    public void writeToJson(List<UserStory> userStories, String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Mit Pretty-Printer für eine menschenlesbare JSON-Struktur
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), userStories);
    }
}
