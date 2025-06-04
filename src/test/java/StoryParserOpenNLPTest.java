

import de.uni_marburg.userstoryanalyzer.model.*;
import de.uni_marburg.userstoryanalyzer.parser.StoryParserOpenNLP;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class StoryParserOpenNLPTest {

    private StoryParserOpenNLP parser;

    @BeforeEach
    void setUp() throws Exception {
        parser = new StoryParserOpenNLP();
    }

    @Test
    void parse_shouldExtractPid() {
        String input = "#G03# As a User, I want to X, so that Y.";
        UserStory story = parser.parse(input);
        assertEquals("#G03#", story.getPid());
    }

    @Test
    void parse_shouldExtractPersona() {
        String input = "#G01# As a Public User, I want to Search, so that...";
        UserStory story = parser.parse(input);
        assertEquals(List.of("Public User"), story.getPersona());
    }

    @Test
    void parse_shouldExtractGoalAction() {
        String input = "#G02# As a User, I want to Search, so that...";
        UserStory story = parser.parse(input);
        assertTrue(story.getAction().getGoal().contains("Search"));
    }

    @Test
    void parse_shouldExtractBenefitAction() {
        String input = "#G04# As a User, I want to X, so that I can obtain...";
        UserStory story = parser.parse(input);
        assertTrue(story.getAction().getBenefit().contains("obtain"));
    }

    @Test
    void parse_shouldBuildTriggersRelation() {
        String input = "#G05# As a Admin, I want to Delete, so that...";
        UserStory story = parser.parse(input);

        List<List<String>> triggers = story.getTriggers();
        assertTrue(
                triggers.stream().anyMatch(pair ->
                        pair.get(0).equals("Admin") &&
                                pair.get(1).equals("Delete")),
                "Expected TRIGGERS relation between 'Admin' and 'Delete'"
        );
    }

    @Test
    void parse_shouldHandleEmptyInputGracefully() {
        UserStory story = parser.parse("");


        assertAll(
                () -> assertNull(story.getPid(), "PID should be null for empty input"),
                () -> assertTrue(story.getPersona().isEmpty(), "Persona list should be empty"),
                () -> assertTrue(story.getAction().getGoal().isEmpty(), "Goal actions should be empty"),
                () -> assertTrue(story.getAnnotations().isEmpty(), "Annotations should be empty")
        );
    }

    @Test
    void parse_shouldExtractComplexEntities() {
        String input = "#G06# As a User, I want to Find, so that I can use publicly available information...";
        UserStory story = parser.parse(input);
        assertTrue(story.getEntity().getBenefit().contains("publicly available information"));
    }

    @Test
    void parse_shouldGenerateAnnotations() {
        String input = "#G07# As a User, I want to edit or Post Citations and Fines, so that I can save...";
        UserStory story = parser.parse(input);


        assertAll(
                () -> assertTrue(story.getAnnotations().contains(Annotation.PERSONA), "PERSONA annotation missing"),
                () -> assertTrue(story.getAnnotations().contains(Annotation.GOAL_ACTION), "GOAL_ACTION annotation missing")
        );
    }

    @Test
    void parse_shouldExtractMultipleGoalActions() {
        String input = "#G08# As a User, I want to Search and Download data, so that I can analyze it.";
        UserStory story = parser.parse(input);
        assertTrue(story.getAction().getGoal().containsAll(List.of("Search", "Download")));
    }

    @Test
    void parse_shouldExtractMultipleBenefitEntities() {
        String input = "#G09# As a User, I want to Submit, so that I can store and share documents and reports.";
        UserStory story = parser.parse(input);
        List<String> entities = story.getEntity().getBenefit();
        assertTrue(entities.contains("documents"));
        assertTrue(entities.contains("reports"));
    }
    @Test
    void parse_shouldHandleAlternativeBenefitPhrase() {
        String input = "#G10# As a User, I want to register to get faster results.";
        UserStory story = parser.parse(input);
        assertTrue(story.getEntity().getGoal().stream().anyMatch(e -> e.contains("faster results")));
    }

    @Test
    void parse_shouldBuildContainsRelation() {
        String input = "#G11# As a User, I want to send data, so that I can create reports.";
        UserStory story = parser.parse(input);

        List<List<String>> contains = story.getContains();
        assertTrue(
                contains.stream().anyMatch(pair ->
                        pair.get(0).contains("data") &&
                                pair.get(1).contains("reports")),
                "Expected CONTAINS relation between 'data' and 'reports'"
        );
    }

    @Test
    void parseFromFile_shouldGeneratePidIfMissing() throws IOException {
        Path tempFile = Files.createTempFile("test-story", ".txt");
        Files.writeString(tempFile, "As a Guest, I want to View content, so that I stay informed.");

        List<UserStory> stories = parser.parseFromFile(tempFile.toString());

        assertNotNull(stories.get(0).getPid());
        assertTrue(stories.get(0).getPid().startsWith("#GO"));
    }
}