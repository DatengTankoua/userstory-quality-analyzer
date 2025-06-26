import de.uni_marburg.userstoryanalyzer.model.Action;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ActionTest {

    // Tests if the constructor correctly handles empty lists for goal and benefit actions
    @Test
    void constructor_shouldHandleEmptyLists() {
        Action action = new Action(List.of(), List.of());
        assertTrue(action.getGoal().isEmpty());
        assertTrue(action.getBenefit().isEmpty());
    }

    // Tests if the constructor stores goal and benefit actions correctly
    @Test
    void constructor_shouldStoreActionsCorrectly() {
        Action action = new Action(List.of("Search"), List.of("obtain"));
        assertEquals("Search", action.getGoal().get(0));
        assertEquals("obtain", action.getBenefit().get(0));
    }
}