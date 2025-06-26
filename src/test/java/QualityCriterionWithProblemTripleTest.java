import de.uni_marburg.userstoryanalyzer.analysis.ProblemTriple;
import de.uni_marburg.userstoryanalyzer.analysis.QualityCriterionWithProblemTriple;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QualityCriterionWithProblemTripleTest {

    // Tests if adding a problem increments the problem count
    @Test
    void addProblem_shouldIncrementProblemCount() {
        QualityCriterionWithProblemTriple criterion = new QualityCriterionWithProblemTriple();
        criterion.addProblem("Story1", "Story2", "Inconsistent entities");
        assertEquals(1, criterion.anzahlVonProblemen);
    }

    // Tests if the problem is stored with the correct stories and description
    @Test
    void addProblem_shouldStoreCorrectProblemTriple() {
        QualityCriterionWithProblemTriple criterion = new QualityCriterionWithProblemTriple();
        criterion.addProblem("StoryA", "StoryB", "Missing CRUD pair");
        ProblemTriple problem = (ProblemTriple) criterion.qualitaetsProbleme.get(0);
        assertEquals("StoryA", problem.story1);
        assertEquals("StoryB", problem.story2);
        assertEquals("Missing CRUD pair", problem.problem);
    }
}