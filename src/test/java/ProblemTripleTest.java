import de.uni_marburg.userstoryanalyzer.analysis.ProblemTriple;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProblemTripleTest {

    // Tests if the constructor sets story1, story2, and problem correctly
    @Test
    void constructor_shouldSetFieldsCorrectly() {
        ProblemTriple problem = new ProblemTriple("Story1", "Story2", "Conflict");
        assertEquals("Story1", problem.story1);
        assertEquals("Story2", problem.story2);
        assertEquals("Conflict", problem.problem);
    }
}