package de.uni_marburg.userstoryanalyzer.parser;

import de.uni_marburg.userstoryanalyzer.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
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
        String input = "#G07# As a User, I want to Edit, so that I can save...";
        UserStory story = parser.parse(input);

        assertAll(
                () -> assertTrue(story.getAnnotations().contains(Annotation.PERSONA), "PERSONA annotation missing"),
                () -> assertTrue(story.getAnnotations().contains(Annotation.GOAL_ACTION), "GOAL_ACTION annotation missing")
        );
    }
}