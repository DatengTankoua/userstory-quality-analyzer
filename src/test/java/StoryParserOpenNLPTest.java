import de.uni_marburg.userstoryanalyzer.model.UserStory;
import de.uni_marburg.userstoryanalyzer.parser.StoryParserOpenNLP;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StoryParserOpenNLPTest {

    private StoryParserOpenNLP parser;

    @BeforeEach
    void setUp() throws Exception {
        parser = new StoryParserOpenNLP();
    }

    @Test
    void testParseWithPidAndBenefit() {
        String input = "#U123# As a registered user, I want to submit feedback, so that the support team can improve the service.";

        UserStory story = parser.parse(input);

        //Check if the PID is correctly extracted
        assertEquals("#U123#", story.getPid());

        //Make sure the persona includes "registered user"
        assertTrue(story.getPersona().contains("registered user"));

        //Verify that goal and benefit actions are detected correctly
        List<String> goalActions = story.getAction().getGoal();
        List<String> benefitActions = story.getAction().getBenefit();
        assertTrue(goalActions.contains("submit"), "Goal action 'submit' not found");
        assertTrue(benefitActions.contains("improve"), "Benefit action 'improve' not found");

        //Check that entities for goal and benefit are present
        List<String> goalEntities = story.getEntity().getGoal();
        List<String> benefitEntities = story.getEntity().getBenefit();
        assertTrue(goalEntities.contains("feedback"), "Goal entity 'feedback' not found");
        assertTrue(benefitEntities.contains("service"), "Benefit entity 'service' not found");

        //Confirm that the benefit text is captured properly
        assertTrue(story.getBenefit().toLowerCase().contains("improve"), "Benefit text not recognized properly");

        //Ensure relations and their types are created
        assertFalse(story.getRelations().isEmpty(), "Relations should not be empty");
        assertFalse(story.getTriggers().isEmpty(), "Trigger relations missing");
        assertFalse(story.getTargets().isEmpty(), "Target relations missing");
    }

    @Test
    void testParserWithoutPidOrBenefit() {
        String input = "As an admin, I want to create a user account.";

        UserStory story = parser.parse(input);

        //Print extracted persona, goal actions, and entities for debugging
        System.out.println("Persona: " + story.getPersona());
        System.out.println("Goal Actions: " + story.getAction().getGoal());
        System.out.println("Goal Entities: " + story.getEntity().getGoal());

        //Check that persona includes "admin"
        assertTrue(
                story.getPersona().stream().anyMatch(p -> p.toLowerCase().contains("admin")),
                "Persona should contain 'admin'"
        );

        //Confirm that the goal action "create" was detected
        assertTrue(
                story.getAction().getGoal().contains("create"),
                "Action should contain 'create'"
        );

        //Verify the goal entities mention "account"
        assertTrue(
                story.getEntity().getGoal().stream().anyMatch(e -> e.toLowerCase().contains("account")),
                "Entity should mention 'account'"
        );

        //The benefit should be empty in this case
        assertEquals("", story.getBenefit(), "Benefit should be empty");
    }

    @Test
    void testParseInvalidInput() {
        String input = "Just some random text without structure.";

        UserStory story = parser.parse(input);

        //Print persona for debugging purposes
        System.out.println("Persona: " + story.getPersona());

        //Make sure no persona is detected
        assertTrue(story.getPersona().isEmpty() || story.getPersona().get(0).isBlank(), "No persona should be detected");

        //There should be no goal actions or goal entities
        assertTrue(story.getAction().getGoal().isEmpty());
        assertTrue(story.getEntity().getGoal().isEmpty());

        //Benefit text should be empty
        assertEquals("", story.getBenefit());
    }
}
