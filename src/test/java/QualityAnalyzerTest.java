import de.uni_marburg.userstoryanalyzer.analysis.QualityAnalyzer;
import de.uni_marburg.userstoryanalyzer.analysis.QualityCriterionReport;
import de.uni_marburg.userstoryanalyzer.model.*;
import de.uni_marburg.userstoryanalyzer.parser.StoryParserOpenNLP;
import net.didion.jwnl.JWNLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class QualityAnalyzerTest {

    private static StoryParserOpenNLP storyParser;

    @BeforeAll
    static void setup() throws IOException {
        storyParser = new StoryParserOpenNLP();
    }

    private UserStory buildStory(String text) {
        return storyParser.parse(text);
    }

    @Test
    void testIsWellFormed() {
        UserStory wellFormed = buildStory("As a user, I want to reset my password so that I can regain access.");
        assertTrue(QualityAnalyzer.isWellFormed(wellFormed));

        UserStory missingPersona = buildStory("I want to reset my password so that I can regain access.");
        assertFalse(QualityAnalyzer.isWellFormed(missingPersona));

        UserStory missingAction = buildStory("As a user, so that I can regain access.");
        assertFalse(QualityAnalyzer.isWellFormed(missingAction));

        UserStory missingEntity = buildStory("As a user, I want to reset.");
        assertFalse(QualityAnalyzer.isWellFormed(missingEntity));
    }

    @Test
    void testIsAtomic() {
        UserStory atomic = buildStory("As a user, I want to login so that I can access the dashboard.");
        assertTrue(QualityAnalyzer.isAtomic(atomic));

        UserStory notAtomicMultipleGoals = buildStory("As a user, I want to login and view dashboard so that I can access data.");
        assertFalse(QualityAnalyzer.isAtomic(notAtomicMultipleGoals));

        UserStory notAtomicNoBenefit = buildStory("As a user, I want to login.");
        assertFalse(QualityAnalyzer.isAtomic(notAtomicNoBenefit));
    }

    @Test
    void testIsUniform() {
        UserStory uniform = buildStory("As a customer, I want to track my order so that I know the delivery time.");
        assertTrue(QualityAnalyzer.isUniform(uniform));

        UserStory uniformWithAn = buildStory("As an admin, I want to manage users so that the system stays secure.");
        assertTrue(QualityAnalyzer.isUniform(uniformWithAn));

        UserStory notUniformWrongOrder = buildStory("So that I know the delivery time, as a customer I want to track my order.");
        assertFalse(QualityAnalyzer.isUniform(notUniformWrongOrder));

        UserStory notUniformMissingParts = buildStory("Track my order to know delivery time.");
        assertFalse(QualityAnalyzer.isUniform(notUniformMissingParts));
    }

    @Test
    void testIsMinimal() {
        String minimalStory = "As a user, I want to logout so that I can secure my session.";
        assertTrue(QualityAnalyzer.isMinimal(minimalStory));

        String storyWithNote = "As a user, I want to logout (see example on page 3).";
        assertFalse(QualityAnalyzer.isMinimal(storyWithNote));

        String storyWithExample = "As a user, I want to logout e.g. when inactive.";
        assertFalse(QualityAnalyzer.isMinimal(storyWithExample));

        String storyWithMultipleSentences = "As a user, I want to logout. This is important for security.";
        assertFalse(QualityAnalyzer.isMinimal(storyWithMultipleSentences));
    }

    @Test
    void testIsAnalysable() {
        UserStory analysable = buildStory("As a user, I want to update my profile.");
        assertTrue(QualityAnalyzer.isAnalysable(analysable));

        UserStory analysableWithBenefit = buildStory("As a user, I want to update my profile so that my info stays current.");
        assertTrue(QualityAnalyzer.isAnalysable(analysableWithBenefit));

        UserStory notAnalysable = buildStory("Random sentence without structure.");
        assertFalse(QualityAnalyzer.isAnalysable(notAnalysable));

        UserStory emptyStory = buildStory("");
        assertFalse(QualityAnalyzer.isAnalysable(emptyStory));
    }

    @Test
    void testCheckVollstaendigkeit() {
        UserStory readStory = buildStory("As an admin, I want to view user accounts.");
        UserStory createStory = buildStory("As an admin, I want to create a user account.");
        UserStory updateStory = buildStory("As an admin, I want to edit user accounts.");
        UserStory deleteStory = buildStory("As an admin, I want to delete user accounts.");
        UserStory unrelatedStory = buildStory("As a user, I want to view my profile.");

        List<UserStory> completeStories = List.of(readStory, createStory, updateStory, deleteStory);
        QualityCriterionReport completeReport = new QualityCriterionReport();
        QualityAnalyzer.checkVollstaendigkeit(completeStories, completeReport);
        assertEquals(4, completeReport.Vollstaendigkeit.anzahlVonProblemlosen);

        List<UserStory> incompleteStories = List.of(readStory, updateStory, deleteStory, unrelatedStory);
        QualityCriterionReport incompleteReport = new QualityCriterionReport();
        QualityAnalyzer.checkVollstaendigkeit(incompleteStories, incompleteReport);
        // All 4 stories use USAGE_KEYWORDS actions but have no matching create story
        assertEquals(4, incompleteReport.Vollstaendigkeit.anzahlVonProblemen);
    }

    @Test
    void testCheckRedundanzfreiheit() {
        assumeTrue(QualityAnalyzer.isWordNetAvailable(), "WordNet dictionary not available — skipping semantic similarity test");
        UserStory story1 = buildStory("As a user, I want to view my profile so that I can check my details.");
        UserStory story2 = buildStory("As a user, I want to see my profile so that I can verify my information.");
        UserStory story3 = buildStory("As an admin, I want to manage users so that I can control access.");

        List<UserStory> stories = List.of(story1, story2, story3);
        QualityCriterionReport report = new QualityCriterionReport();
        QualityAnalyzer.checkRedundanzfreiheit(stories, report);

        assertEquals(1, report.Redundanzfreiheit.anzahlVonProblemen);
        assertEquals(2, report.Redundanzfreiheit.anzahlVonProblemlosen);
    }

    @Test
    void testCheckUnabhaengigkeit() throws JWNLException {
        UserStory viewStory = buildStory("As a user, I want to view my orders.");
        UserStory createStory = buildStory("As a user, I want to create an order.");
        UserStory unrelatedStory = buildStory("As an admin, I want to manage users.");

        List<UserStory> stories = List.of(viewStory, createStory, unrelatedStory);
        QualityCriterionReport report = new QualityCriterionReport();
        QualityAnalyzer.checkUnabhaengigkeit(stories, report);

        assertEquals(1, report.Unabhaengigkeit.anzahlVonProblemen);
        assertEquals(2, report.Unabhaengigkeit.anzahlVonProblemlosen);
    }

    @Test
    void testCheckKonfliktfreiheit() {
        UserStory deleteAnyStory = buildStory("As an admin, I want to delete any user.");
        UserStory deleteOnlyStory = buildStory("As an admin, I want to delete only inactive users.");
        UserStory unrelatedStory = buildStory("As a user, I want to view my profile.");

        List<UserStory> stories = List.of(deleteAnyStory, deleteOnlyStory, unrelatedStory);
        QualityCriterionReport report = new QualityCriterionReport();
        QualityAnalyzer.checkKonfliktfreiheit(stories, report);

        assertEquals(1, report.Konfliktfreiheit.anzahlVonProblemen);
        assertEquals(2, report.Konfliktfreiheit.anzahlVonProblemlosen);
    }

    @Test
    void testAnalyzeStories_Integration() throws JWNLException {
        UserStory wellFormed = buildStory("As a student, I want to view grades so that I can track performance.");
        UserStory notWellFormed = buildStory("View grades to track performance.");
        UserStory atomic = buildStory("As a teacher, I want to upload grades so that students can see them.");
        UserStory notAtomic = buildStory("As a teacher, I want to upload and edit grades.");
        UserStory uniform = buildStory("As an admin, I want to manage users so that the system stays secure.");
        UserStory notUniform = buildStory("Manage users to keep system secure as an admin.");
        UserStory minimal = buildStory("As a user, I want to logout so that I can secure my session.");
        UserStory notMinimal = buildStory("As a user, I want to logout (when inactive) so that I can secure my session.");

        List<UserStory> stories = List.of(wellFormed, notWellFormed, atomic, notAtomic, uniform, notUniform, minimal, notMinimal);
        QualityCriterionReport report = QualityAnalyzer.analyzeStories(stories);

        // Basic checks
        assertEquals(8, report.stories.size() + report.nicht_analysierbar.size());
        assertTrue(report.nicht_analysierbar.contains(notWellFormed.getText()));

        // Only analysable stories (those with annotations) are counted per criterion
        int analysableCount = report.stories.size();
        assertEquals(analysableCount, report.Wohlgeformtheit.anzahlVonProblemlosen + report.Wohlgeformtheit.anzahlVonProblemen);
        assertEquals(analysableCount, report.Atomaritaet.anzahlVonProblemlosen + report.Atomaritaet.anzahlVonProblemen);
        assertEquals(analysableCount, report.Uniformitaet.anzahlVonProblemlosen + report.Uniformitaet.anzahlVonProblemen);
        assertEquals(analysableCount, report.Minimalitaet.anzahlVonProblemlosen + report.Minimalitaet.anzahlVonProblemen);
    }
}