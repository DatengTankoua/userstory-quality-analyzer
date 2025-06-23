import de.uni_marburg.userstoryanalyzer.analysis.QualityAnalyzer;
import de.uni_marburg.userstoryanalyzer.analysis.QualityCriterionReport;
import de.uni_marburg.userstoryanalyzer.model.*;
import de.uni_marburg.userstoryanalyzer.parser.StoryParserOpenNLP;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QualityAnalyzerTest {

    StoryParserOpenNLP storyParserOpenNLP = new StoryParserOpenNLP();

    QualityAnalyzerTest() throws IOException {
    }

    private UserStory buildStory(String text) {
        return storyParserOpenNLP.parse(text);
    }

    @Test
    void testIsWellFormed() {
        UserStory wellFormed = buildStory("As a user, I want to reset my password so that I can regain access.");

        assertTrue(QualityAnalyzer.isWellFormed(wellFormed));
    }

    @Test
    void testIsAtomic() {
        UserStory atomic = buildStory(
                "As a user, I want to login so that I can access the dashboard."
        );

        assertTrue(QualityAnalyzer.isAtomic(atomic));

        UserStory notAtomic = buildStory(
                "As a user, I want to login and view dashboard so that I can access data."
        );

        assertFalse(QualityAnalyzer.isAtomic(notAtomic));
    }

    @Test
    void testIsUniform() {
        UserStory uniform = buildStory(
                "As a customer, I want to track my order so that I know the delivery time."
        );

        assertTrue(QualityAnalyzer.isUniform(uniform));

        UserStory notUniform = buildStory(
                "The user needs to reset password quickly."
        );

        assertFalse(QualityAnalyzer.isUniform(notUniform));
    }

    @Test
    void testIsMinimal() {
        String minimalStory = "As a user, I want to logout so that I can secure my session.";
        assertTrue(QualityAnalyzer.isMinimal(minimalStory));

        String storyWithNote = "As a user, I want to logout (see example on page 3).";
        assertFalse(QualityAnalyzer.isMinimal(storyWithNote));

        String storyWithMultipleSentences = "As a user, I want to logout so that I can secure my session. Then I want to exit.";
        assertFalse(QualityAnalyzer.isMinimal(storyWithMultipleSentences));
    }

    @Test
    void testIsAnalysable() {
        UserStory analysable = buildStory(
                "As a user, I want to update my profile."
        );

        assertTrue(QualityAnalyzer.isAnalysable(analysable));

        UserStory notAnalysable = buildStory(
                "Random sentence without structure."
        );

        assertFalse(QualityAnalyzer.isAnalysable(notAnalysable));
    }

    @Test
    void testCheckVollstaendigkeit() {
        UserStory readStory = buildStory(
                "As an admin, I want to view user accounts."
        );

        UserStory createStory = buildStory(
                "As an admin, I want to create a user account."
        );

        List<UserStory> stories = List.of(readStory, createStory);
        QualityCriterionReport report = new QualityCriterionReport();

        QualityAnalyzer.checkVollstaendigkeit(stories, report);
        assertEquals(2, report.Vollstaendigkeit.anzahlVonProblemlosen);
        assertEquals(0, report.Vollstaendigkeit.anzahlVonProblemen);
    }

    @Test
    void testAnalyzeStories_Integration() {
        UserStory s1 = buildStory(
                "As a student, I want to view grades so that I can track performance."
        );

        UserStory s2 = buildStory(
                "As an admin, I want to create grades."
        );

        QualityCriterionReport report = QualityAnalyzer.analyzeStories(List.of(s1, s2));

        assertEquals(2, report.stories.size());
        assertEquals(2, report.Wohlgeformtheit.anzahlVonProblemlosen);
        assertEquals(1, report.Atomaritaet.anzahlVonProblemlosen); // s2 has no benefit
        assertEquals(2, report.Uniformitaet.anzahlVonProblemlosen);
        assertEquals(2, report.Minimalitaet.anzahlVonProblemlosen);
        assertEquals(2, report.Vollstaendigkeit.anzahlVonProblemlosen);
        assertEquals(0, report.Vollstaendigkeit.anzahlVonProblemen);
    }
}
