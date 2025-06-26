import de.uni_marburg.userstoryanalyzer.analysis.ProblemPair;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProblemPairTest {

    // Tests if the constructor sets the story and problem fields correctly
    @Test
    void constructor_shouldSetFieldsCorrectly() {
        ProblemPair problem = new ProblemPair("Story1", "No goal");
        assertEquals("Story1", problem.story);
        assertEquals("No goal", problem.problem);
    }
}