import de.uni_marburg.userstoryanalyzer.analysis.QualityCriterionReport;
import de.uni_marburg.userstoryanalyzer.export.QualityReportExporter;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

class QualityReportExporterTest {

    // Tests if the exporter creates a valid JSON file
    @Test
    void writeToJson_shouldCreateValidJsonFile() throws IOException {
        QualityReportExporter exporter = new QualityReportExporter();
        QualityCriterionReport report = new QualityCriterionReport();
        report.stories.add("Test Story");

        File tempFile = File.createTempFile("quality-report", ".json");
        exporter.writeToJson(report, tempFile.getAbsolutePath());

        // Check if the JSON contains expected content
        String jsonContent = Files.readString(tempFile.toPath());
        assertTrue(jsonContent.contains("Test Story"));
        assertTrue(jsonContent.contains("Wohlgeformtheit"));
    }

    // Tests if the exporter throws an exception for invalid paths
    @Test
    void writeToJson_shouldThrowIOExceptionForInvalidPath() {
        QualityReportExporter exporter = new QualityReportExporter();
        assertThrows(IOException.class, () -> {
            exporter.writeToJson(new QualityCriterionReport(), "/invalid/path/report.json");
        });
    }
}