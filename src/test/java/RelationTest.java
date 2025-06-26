import de.uni_marburg.userstoryanalyzer.model.Relation;
import de.uni_marburg.userstoryanalyzer.model.RelationType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RelationTest {

    // Tests if the relation stores type, source, and target correctly
    @Test
    void getters_shouldReturnCorrectValues() {
        Relation relation = new Relation(RelationType.TRIGGERS, "Admin", "Delete");
        assertEquals(RelationType.TRIGGERS, relation.getType());
        assertEquals("Admin", relation.getSource());
        assertEquals("Delete", relation.getTarget());
    }
}