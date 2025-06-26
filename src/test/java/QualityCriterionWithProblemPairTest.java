import de.uni_marburg.userstoryanalyzer.analysis.ProblemPair;
import de.uni_marburg.userstoryanalyzer.analysis.QualityCriterionWithProblemPair;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QualityCriterionWithProblemPairTest {

    // Tests if adding a problem increments the problem count
    @Test
    void addProblem_shouldIncrementProblemCount() {
        QualityCriterionWithProblemPair criterion = new QualityCriterionWithProblemPair();
        criterion.addProblem("Story1", "Missing persona");
        assertEquals(1, criterion.anzahlVonProblemen);
    }

    // Tests if the problem is stored with the correct story and description
    @Test
    void addProblem_shouldStoreCorrectProblemPair() {
        QualityCriterionWithProblemPair criterion = new QualityCriterionWithProblemPair();
        criterion.addProblem("Story2", "No benefit defined");
        ProblemPair problem = (ProblemPair) criterion.qualitaetsProbleme.get(0);
        assertEquals("Story2", problem.story);
        assertEquals("No benefit defined", problem.problem);
    }
}