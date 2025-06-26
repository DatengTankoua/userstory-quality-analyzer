package de.uni_marburg.userstoryanalyzer.gui;

import de.uni_marburg.userstoryanalyzer.analysis.ProblemPair;
import de.uni_marburg.userstoryanalyzer.analysis.ProblemTriple;
import de.uni_marburg.userstoryanalyzer.analysis.QualityAnalyzer;
import de.uni_marburg.userstoryanalyzer.analysis.QualityCriterionReport;
import de.uni_marburg.userstoryanalyzer.export.QualityReportExporter;
import de.uni_marburg.userstoryanalyzer.model.UserStory;
import de.uni_marburg.userstoryanalyzer.parser.StoryParserOpenNLP;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JavaFX application for visualizing and analyzing the quality of User Stories.
 * <p>
 * Features:
 * <ul>
 *     <li>Load and parse User Stories from a text file</li>
 *     <li>Analyze User Stories based on quality criteria</li>
 *     <li>Display stories and issues in tables</li>
 *     <li>Export analysis results as a JSON report</li>
 * </ul>
 */
public class UserStoryViewerApp extends Application {

    /** Table for displaying parsed User Stories */
    private final TableView<UserStory> storyTable = new TableView<>();

    /** List of parsed User Stories */
    private final ObservableList<UserStory> userStories = FXCollections.observableArrayList();

    /** Tabs for displaying quality problems by category */
    private final TabPane qualityTabs = new TabPane();

    /** Stores the analysis report */
    private QualityCriterionReport currentReport;

    /**
     * Application start point for JavaFX.
     *
     * @param stage JavaFX primary stage
     */
    @Override
    public void start(Stage stage) {
        stage.setTitle("User Story Quality Analyzer");

        storyTable.setItems(userStories);
        storyTable.getColumns().addAll(
                createColumn("PID", s -> s.getPid()),
                createColumn("Persona(s)", s -> String.join(", ", s.getPersona())),
                createColumn("Goal Actions", s -> String.join(", ", s.getAction().getGoal())),
                createColumn("Goal Entities", s -> String.join(", ", s.getEntity().getGoal())),
                createColumn("Benefit", s -> s.getBenefit())
        );

        Button loadAnalyzeBtn = new Button("Load & Analyze");
        Button exportBtn = new Button("Export as JSON");
        HBox buttonBar = new HBox(10, loadAnalyzeBtn, exportBtn);

        loadAnalyzeBtn.setOnAction(e -> loadAndAnalyze(stage));
        exportBtn.setOnAction(e -> exportJson(stage));

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.getItems().addAll(new VBox(buttonBar, storyTable), qualityTabs);
        splitPane.setDividerPositions(0.5);

        Scene scene = new Scene(splitPane, 1000, 700);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Creates a table column with a custom string extractor.
     *
     * @param title     Column title
     * @param extractor Function to extract cell content
     * @return Configured TableColumn
     */
    private TableColumn<UserStory, String> createColumn(String title, javafx.util.Callback<UserStory, String> extractor) {
        TableColumn<UserStory, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new SimpleStringProperty(extractor.call(data.getValue())));
        return col;
    }

    /**
     * Opens file dialog, loads, parses and analyzes stories, and updates the UI.
     *
     * @param stage Application window
     */
    private void loadAndAnalyze(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open User Stories File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) return;

        try {
            StoryParserOpenNLP parser = new StoryParserOpenNLP();
            List<String> lines = Files.readAllLines(file.toPath());
            List<UserStory> parsed = lines.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(parser::parse)
                    .collect(Collectors.toList());

            userStories.setAll(parsed);
            currentReport = QualityAnalyzer.analyzeStories(parsed);
            refreshQualityTabs();

            showInfo("Analysis Complete", "User stories loaded and analyzed successfully.");

        } catch (Exception ex) {
            showError("Parsing Error", ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Exports the current analysis result as a JSON file.
     *
     * @param stage Application window
     */
    private void exportJson(Stage stage) {
        if (currentReport == null) {
            showError("Export Error", "No analysis available to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save JSON Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;

        try {
            QualityReportExporter exporter = new QualityReportExporter();
            exporter.writeToJson(currentReport, file.getAbsolutePath());
            showInfo("Export Success", "Report exported successfully to:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            showError("Export Error", "Failed to write file: " + ex.getMessage());
        }
    }

    /**
     * Updates the quality tabs with current analysis results.
     */
    private void refreshQualityTabs() {
        qualityTabs.getTabs().clear();
        addQualityTabWithTable("Wohlgeformtheit", currentReport.Wohlgeformtheit.qualitaetsProbleme);
        addQualityTabWithTable("Atomaritaet", currentReport.Atomaritaet.qualitaetsProbleme);
        addQualityTabWithTable("Uniformitaet", currentReport.Uniformitaet.qualitaetsProbleme);
        addQualityTabWithTable("Minimalitaet", currentReport.Minimalitaet.qualitaetsProbleme);
        addQualityTabWithTable("Vollstaendigkeit", currentReport.Vollstaendigkeit.qualitaetsProbleme);

        if (!currentReport.nicht_analysierbar.isEmpty()) {
            List<String> notAnalyzed = currentReport.nicht_analysierbar;
            TextArea area = new TextArea(String.join("\n", notAnalyzed));
            area.setEditable(false);
            qualityTabs.getTabs().add(new Tab("Nicht analysierbar", area));
        }
    }

    /**
     * Adds a new tab with a table showing stories and corresponding quality problems.
     *
     * @param name     Name of the quality criterion
     * @param problems List of problems for the criterion
     */
    private void addQualityTabWithTable(String name, List<?> problems) {
        TableView<Object> table = new TableView<>();

        TableColumn<Object, String> storyCol = new TableColumn<>("Story");
        storyCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof ProblemPair) {
                return new SimpleStringProperty(((ProblemPair) data.getValue()).story);
            } else if (data.getValue() instanceof ProblemTriple) {
                return new SimpleStringProperty(((ProblemTriple) data.getValue()).story1 + " | " + ((ProblemTriple) data.getValue()).story2);
            }
            return new SimpleStringProperty("");
        });

        TableColumn<Object, String> problemCol = new TableColumn<>("Problem");
        problemCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof ProblemPair) {
                return new SimpleStringProperty(((ProblemPair) data.getValue()).problem);
            } else if (data.getValue() instanceof ProblemTriple) {
                return new SimpleStringProperty(((ProblemTriple) data.getValue()).problem);
            }
            return new SimpleStringProperty("");
        });

        table.getColumns().addAll(storyCol, problemCol);
        table.setItems(FXCollections.observableArrayList(problems));

        qualityTabs.getTabs().add(new Tab(name, table));
    }

    /**
     * Shows an error alert dialog.
     *
     * @param title Dialog title
     * @param msg   Error message
     */
    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Shows an informational alert dialog.
     *
     * @param title Dialog title
     * @param msg   Message to display
     */
    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Main entry point of the JavaFX application.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}