package de.uni_marburg.userstoryanalyzer.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.uni_marburg.userstoryanalyzer.analysis.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QualityReportExporter {

    // Inner class for storing the structure of a quality report
    public static class QualityReport {
        public List<String> stories;
        public List<String> nicht_analysiert;
        public Wohlgeformtheit Wohlgeformtheit;
        public Atomaritaet Atomaritaet;
        public Uniformitaet Uniformitaet;
        public Minimalitaet Minimalitaet;
        public Vollstaendigkeit Vollstaendigkeit;
        public Redundanzfreiheit Redundanzfreiheit;
        // More quality criteria can be added here later
    }

    /**
     * Exports the quality report in JSON format
     *
     * @param report The report that should be exported
     * @param filePath The target file path for the JSON file
     * @throws IOException If something goes wrong while writing the file
     */
    public void exportToJson(QualityCriterionReport report, String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // makes the JSON nicely formatted

        // Convert the report into a map that can be serialized to JSON
        Map<String, Object> output = new HashMap<>();
        output.put("stories", report.getStories());
        output.put("nicht_analysiert", report.getNichtAnalysiert());

        // Add each quality criterion to the output
        output.put("Wohlgeformtheit", createCriterionMap(report.Wohlgeformtheit));
        output.put("Atomaritaet", createCriterionMap(report.Atomaritaet));
        output.put("Uniformitaet", createCriterionMap(report.Uniformitaet));
        output.put("Minimalitaet", createCriterionMap(report.Minimalitaet));
        output.put("Vollstaendigkeit", createCriterionMap(report.Vollstaendigkeit));
        output.put("Redundanzfreiheit", createTripleCriterionMap(report.Redundanzfreiheit));
        // You could add more criteria here later on if needed

        // Actually write the JSON to the specified file
        objectMapper.writeValue(new File(filePath), output);
    }

    // Helper method for converting a criterion with a problem pair into a map
    private Map<String, Object> createCriterionMap(QualityCriterionWithProblemPair criterion) {
        Map<String, Object> map = new HashMap<>();
        map.put("AnzahlVonProblemen", criterion.anzahlVonProblemen);       // number of issues
        map.put("AnzahlVonProblemlosen", criterion.anzahlVonProblemlosen); // number of non-issues
        map.put("QualitaetsProbleme", criterion.qualitaetsProbleme);       // list of quality problems
        return map;
    }

    // Similar helper for criteria that use a triple instead of a pair
    private Map<String, Object> createTripleCriterionMap(QualityCriterionWithProblemTriple criterion) {
        Map<String, Object> map = new HashMap<>();
        map.put("AnzahlVonProblemen", criterion.anzahlVonProblemen);       // number of issues
        map.put("AnzahlVonProblemlosen", criterion.anzahlVonProblemlosen); // number of non-issues
        map.put("QualitaetsProbleme", criterion.qualitaetsProbleme);       // list of quality problems
        return map;
    }
}
