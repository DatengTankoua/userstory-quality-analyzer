import de.uni_marburg.userstoryanalyzer.model.Entity;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    // Tests if the constructor orders benefit entities in a specific way
    @Test
    void constructor_shouldOrderBenefitEntities() {
        List<String> benefitEntities = List.of(
                "processes",
                "County services",
                "publicly available information"
        );
        Entity entity = new Entity(List.of("Goal"), benefitEntities);

        // Check if benefit entities are ordered correctly
        assertEquals("publicly available information", entity.getBenefit().get(0));
        assertEquals("County services", entity.getBenefit().get(1));
        assertEquals("processes", entity.getBenefit().get(2));
    }

    // Tests if the constructor handles null input by using empty lists
    @Test
    void constructor_shouldHandleNullInput() {
        Entity entity = new Entity(null, null);
        assertTrue(entity.getGoal().isEmpty());
        assertTrue(entity.getBenefit().isEmpty());
    }
}