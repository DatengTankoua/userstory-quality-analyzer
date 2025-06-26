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
 * Test class for the {@link JsonExporter}.
 */
class JsonExporterTest {

    /**
     * Checks if {@link JsonExporter#writeToJson(List, String)} creates a valid JSON file
     * and whether the structure of the User Story includes all necessary fields.
     */
    @Test
    void testWriteToJson_createsCorrectJsonStructure() throws Exception {
        // Create a sample user story to work with
        UserStory story = getUserStory();

        // Create the output file in the project directory so we can see it afterwards
        File outputFile = new File("src/test/resources/output/userstories.json");
        outputFile.getParentFile().mkdirs(); // Make sure the directory structure exists

        // Run the export
        JsonExporter exporter = new JsonExporter();
        exporter.writeToJson(List.of(story), outputFile.getAbsolutePath());

        // Read the generated JSON file
        String jsonOutput = Files.readString(outputFile.toPath());

        // Basic checks to see if something was written and key fields are present
        assertNotNull(jsonOutput);
        assertTrue(jsonOutput.contains("\"PID\""));
        assertTrue(jsonOutput.contains("\"Triggers\""));
        assertTrue(jsonOutput.contains("\"Targets\""));
        assertTrue(jsonOutput.contains("\"Contains\""));

        // Use Jackson to parse it back and check the structure
        ObjectMapper mapper = new ObjectMapper();
        List<?> parsedJson = mapper.readValue(outputFile, new TypeReference<>() {});
        assertEquals(1, parsedJson.size());

        // Do some more detailed checks to make sure specific content is correct
        String normalized = jsonOutput.replaceAll("\\s+", "");
        assertTrue(normalized.contains("\"Goal\":[\"Search\"]"));
        assertTrue(normalized.contains("\"BenefitEntity\":[\"publiclyavailableinformation\""));
    }

    /**
     * Helper method to generate a sample User Story with full structure.
     * @return A fully constructed {@link UserStory} instance.
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
                List.of(), // no annotations
                List.of(   // relations between the elements in the user story
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
