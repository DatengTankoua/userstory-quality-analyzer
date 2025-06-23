package de.uni_marburg.userstoryanalyzer.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_marburg.userstoryanalyzer.analysis.QualityCriterionReport;
import de.uni_marburg.userstoryanalyzer.model.UserStory;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class QualityReportExporter {

    public void writeToJson(QualityCriterionReport report, String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Mit Pretty-Printer für eine menschenlesbare JSON-Struktur
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), report);
    }
}
