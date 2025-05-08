import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_marburg.userstoryanalyzer.export.JsonExporter;
import de.uni_marburg.userstoryanalyzer.model.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für die {@link JsonExporter}-Klasse.
 *
 * Stellt sicher, dass User Stories korrekt im JSON-Format exportiert werden
 * und die Struktur der erzeugten Datei dem erwarteten Format entspricht.
 */
class JsonExporterTest {

    /**
     * Testet, ob {@link JsonExporter#writeToJson(List, String)} korrekt eine JSON-Datei erzeugt
     * und die Struktur der User Story mit allen notwendigen Feldern vorhanden ist.
     */
    @Test
    void testWriteToJson_createsCorrectJsonStructure() throws Exception {
        // Eine Beispiel-UserStory erzeugen
        UserStory story = getUserStory();

        // Datei im Projektverzeichnis erzeugen, damit sie sichtbar ist
        File outputFile = new File("src/test/resources/output/userstories.json");
        outputFile.getParentFile().mkdirs(); // Ordnerstruktur erstellen, falls nicht vorhanden

        // JSON-Export durchführen
        JsonExporter exporter = new JsonExporter();
        exporter.writeToJson(List.of(story), outputFile.getAbsolutePath());

        // JSON-Inhalt aus Datei lesen
        String jsonOutput = Files.readString(outputFile.toPath());

        // Basisprüfungen auf Inhalt
        assertNotNull(jsonOutput);
        assertTrue(jsonOutput.contains("\"PID\""));
        assertTrue(jsonOutput.contains("\"Triggers\""));
        assertTrue(jsonOutput.contains("\"Targets\""));
        assertTrue(jsonOutput.contains("\"Contains\""));

        // Strukturprüfung durch erneutes Parsen mit Jackson
        ObjectMapper mapper = new ObjectMapper();
        List<?> parsedJson = mapper.readValue(outputFile, new TypeReference<>() {});
        assertEquals(1, parsedJson.size());

        // Detailprüfung auf konkrete Inhalte (strukturell)
        String normalized = jsonOutput.replaceAll("\\s+", "");
        assertTrue(normalized.contains("\"Goal\":[\"Search\"]"));
        assertTrue(normalized.contains("\"BenefitEntity\":[\"properties\""));
    }

    /**
     * Hilfsmethode zum Erzeugen eines Beispiels einer User Story.
     * @return Eine vollständig strukturierte {@link UserStory}-Instanz.
     */
    private static UserStory getUserStory() {
        Action action = new Action(
                List.of("Search"),
                List.of("obtain")
        );

        Entity entity = new Entity(
                List.of("Information"),
                List.of(
                        "properties",
                        "processes",
                        "publicly available information",
                        "County services",
                        "other general information"
                )
        );

        return new UserStory(
                "#G03#",
                "#G03# As a Public User, I want to Search for Information, so that I can obtain publicly available information concerning properties, County services, processes and other general information.",
                List.of("Public User"),
                action,
                entity,
                "I can obtain publicly available information concerning properties, County services, processes and other general information",
                List.of(), // keine Annotationen
                List.of(   // Relationen der User Story
                        new Relation(RelationType.TRIGGERS, "Public User", "Search"),
                        new Relation(RelationType.TARGETS, "Search", "Information"),
                        new Relation(RelationType.TARGETS, "obtain", "publicly available information"),
                        new Relation(RelationType.CONTAINS, "publicly available information", "properties"),
                        new Relation(RelationType.CONTAINS, "publicly available information", "County services"),
                        new Relation(RelationType.CONTAINS, "publicly available information", "processes"),
                        new Relation(RelationType.CONTAINS, "publicly available information", "other general information"),
                        new Relation(RelationType.CONTAINS, "Information", "publicly available information")
                )
        );
    }
}
