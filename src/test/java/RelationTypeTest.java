import de.uni_marburg.userstoryanalyzer.model.RelationType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RelationTypeTest {

    // Tests if the enum values are correctly defined
    @Test
    void enum_shouldHaveCorrectValues() {
        assertEquals("TRIGGERS", RelationType.TRIGGERS.name());
        assertEquals("TARGETS", RelationType.TARGETS.name());
        assertEquals("CONTAINS", RelationType.CONTAINS.name());
    }
}