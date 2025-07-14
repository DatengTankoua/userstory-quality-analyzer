/**
 * UserStoryViewerApp.java
 *
 * Dieses JavaFX-Programm dient zur Anzeige, Analyse und zum Exportieren der Qualität von User Stories.
 * Es erlaubt dem Benutzer, User Stories aus einer Datei zu laden, bestimmte Qualitätskriterien auszuwählen,
 * ausgewählte Stories zu analysieren und die Ergebnisse im JSON-Format zu exportieren. Die Analyse basiert
 * auf verschiedenen Kriterien wie Wohlgeformtheit, Atomarität, Uniformität, etc. und verwendet OpenNLP zur
 * Sprachverarbeitung.
 *
 * @author
 * @version 1.0
 * @since 2025-07-14
 */
package de.uni_marburg.userstoryanalyzer.gui;

import de.uni_marburg.userstoryanalyzer.analysis.*;
import de.uni_marburg.userstoryanalyzer.export.QualityReportExporter;
import de.uni_marburg.userstoryanalyzer.model.UserStory;
import de.uni_marburg.userstoryanalyzer.parser.StoryParserOpenNLP;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class UserStoryViewerApp extends Application {

    private final TableView<UserStory> storyTable = new TableView<>();
    private final ObservableList<UserStory> userStories = FXCollections.observableArrayList();
    private final TabPane qualityTabs = new TabPane();
    private final Set<String> selectedCriteria = new HashSet<>();
    private QualityCriterionReport currentReport;
    private final CheckBox showGoodStoriesCheck = new CheckBox("Show good stories");

    /**
     * Einstiegspunkt der JavaFX-Anwendung. Initialisiert die GUI-Komponenten und zeigt das Hauptfenster an.
     *
     * @param stage das Hauptfenster der JavaFX-Anwendung
     */
    @Override
    public void start(Stage stage) {
        stage.setTitle("User Story Quality Analyzer");
        storyTable.setEditable(true);

        CheckBox selectAllCheckbox = new CheckBox();
        selectAllCheckbox.setOnAction(e -> {
            boolean selected = selectAllCheckbox.isSelected();
            userStories.forEach(us -> us.setSelected(selected));
        });

        TableColumn<UserStory, Boolean> selectCol = new TableColumn<>("Select All");
        selectCol.setGraphic(selectAllCheckbox);
        selectCol.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectCol));
        selectCol.setEditable(true);

        TableColumn<UserStory, String> pidCol = createColumn("PID", UserStory::getPid);
        TableColumn<UserStory, String> personaCol = createColumn("Persona(s)", s -> String.join(", ", s.getPersona()));
        TableColumn<UserStory, String> actionCol = createColumn("Goal Actions", s -> String.join(", ", s.getAction().getGoal()));
        TableColumn<UserStory, String> entityCol = createColumn("Goal Entities", s -> String.join(", ", s.getEntity().getGoal()));
        TableColumn<UserStory, String> benefitCol = createColumn("Benefit", UserStory::getBenefit);

        storyTable.getColumns().addAll(selectCol, pidCol, personaCol, actionCol, entityCol, benefitCol);
        storyTable.setItems(userStories);

        Button loadBtn = new Button("Load Stories");
        Button analyzeBtn = new Button("Analyze Selected");
        Button exportBtn = new Button("Export as JSON");

        VBox criteriaBox = new VBox(5);
        criteriaBox.setPadding(new Insets(10));
        String[] criteria = {"Wohlgeformtheit", "Atomaritaet", "Uniformitaet", "Minimalitaet",
                "Redundanzfreiheit", "Unabhaengigkeit", "Vollstaendigkeit", "Konfliktfreiheit"};

        for (String criterion : criteria) {
            CheckBox cb = new CheckBox(criterion);
            cb.setOnAction(e -> {
                if (cb.isSelected()) selectedCriteria.add(criterion);
                else selectedCriteria.remove(criterion);
            });
            criteriaBox.getChildren().add(cb);
        }

        loadBtn.setOnAction(e -> loadStories(stage));
        analyzeBtn.setOnAction(e -> analyzeSelectedStories());
        exportBtn.setOnAction(e -> exportJson(stage));

        HBox buttonBar = new HBox(10, loadBtn, analyzeBtn, exportBtn);
        VBox leftPane = new VBox(buttonBar, new Label("Select Criteria:"), criteriaBox, showGoodStoriesCheck, storyTable);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().addAll(leftPane, qualityTabs);
        splitPane.setDividerPositions(0.5);

        Scene scene = new Scene(splitPane, 1200, 800);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Hilfsmethode zum Erstellen einer Tabellen-Spalte mit einem bestimmten Titel und einem Wert-Extraktor.
     *
     * @param title     Titel der Spalte
     * @param extractor Funktion zum Extrahieren des Zellinhalts aus einem UserStory-Objekt
     * @return die konfigurierte TableColumn
     */
    private TableColumn<UserStory, String> createColumn(String title, javafx.util.Callback<UserStory, String> extractor) {
        TableColumn<UserStory, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new SimpleStringProperty(extractor.call(data.getValue())));
        return col;
    }

    /**
     * Lädt User Stories aus einer Textdatei über einen FileChooser und analysiert sie mit dem OpenNLP Parser.
     *
     * @param stage das JavaFX-Stage-Objekt zum Anzeigen des Datei-Auswahldialogs
     */
    private void loadStories(Stage stage) {
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

            parsed.forEach(us -> us.setSelected(false));
            userStories.setAll(parsed);

            showInfo("Load Complete", "User stories loaded successfully.");

        } catch (Exception ex) {
            showError("Parsing Error", ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Analysiert die aktuell ausgewählten User Stories anhand der ausgewählten Qualitätskriterien.
     */
    private void analyzeSelectedStories() {
        if (selectedCriteria.isEmpty()) {
            showError("Missing Selection", "Please select at least one quality criterion.");
            return;
        }

        List<UserStory> selected = userStories.stream()
                .filter(UserStory::isSelected)
                .collect(Collectors.toList());

        if (selected.isEmpty()) {
            showError("Missing Selection", "Please select at least one User Story.");
            return;
        }

        currentReport = QualityAnalyzer.analyzeStories(selected, selectedCriteria);
        refreshQualityTabs();
        showInfo("Analysis Complete", "Quality analysis finished successfully.");
    }

    /**
     * Exportiert das aktuelle Analyseergebnis als JSON-Datei.
     *
     * @param stage das JavaFX-Stage-Objekt zum Anzeigen des Datei-Speichern-Dialogs
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
     * Erstellt Tabs für jedes ausgewählte Qualitätskriterium mit den zugehörigen Problemen und ggf. den guten Stories.
     */
    private void refreshQualityTabs() {
        qualityTabs.getTabs().clear();

        if (selectedCriteria.contains("Wohlgeformtheit"))
            addQualityTabWithTable("Wohlgeformtheit", currentReport.Wohlgeformtheit);
        if (selectedCriteria.contains("Atomaritaet"))
            addQualityTabWithTable("Atomaritaet", currentReport.Atomaritaet);
        if (selectedCriteria.contains("Uniformitaet"))
            addQualityTabWithTable("Uniformitaet", currentReport.Uniformitaet);
        if (selectedCriteria.contains("Minimalitaet"))
            addQualityTabWithTable("Minimalitaet", currentReport.Minimalitaet);
        if (selectedCriteria.contains("Redundanzfreiheit"))
            addQualityTabWithTable("Redundanzfreiheit", currentReport.Redundanzfreiheit);
        if (selectedCriteria.contains("Unabhaengigkeit"))
            addQualityTabWithTable("Unabhaengigkeit", currentReport.Unabhaengigkeit);
        if (selectedCriteria.contains("Vollstaendigkeit"))
            addQualityTabWithTable("Vollstaendigkeit", currentReport.Vollstaendigkeit);
        if (selectedCriteria.contains("Konfliktfreiheit"))
            addQualityTabWithTable("Konfliktfreiheit", currentReport.Konfliktfreiheit);

        if (!currentReport.nicht_analysierbar.isEmpty()) {
            TextArea area = new TextArea(String.join("\n", currentReport.nicht_analysierbar));
            area.setEditable(false);
            qualityTabs.getTabs().add(new Tab("Nicht analysierbar", area));
        }
    }

    /**
     * Erstellt einen neuen Tab für ein bestimmtes Qualitätskriterium und zeigt die zugehörigen Probleme an.
     *
     * @param name     Name des Tabs (Kriterium)
     * @param criterion Objekt mit den Qualitätsproblemen
     */
    private void addQualityTabWithTable(String name, QualityCriterion criterion) {
        VBox container = new VBox(5);
        TableView<Object> table = new TableView<>();

        TableColumn<Object, String> storyCol = new TableColumn<>("Story");
        storyCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof ProblemPair)
                return new SimpleStringProperty(((ProblemPair) data.getValue()).story);
            if (data.getValue() instanceof ProblemTriple)
                return new SimpleStringProperty(((ProblemTriple) data.getValue()).story1 + " | " + ((ProblemTriple) data.getValue()).story2);
            return new SimpleStringProperty("");
        });

        TableColumn<Object, String> problemCol = new TableColumn<>("Problem");
        problemCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof ProblemPair)
                return new SimpleStringProperty(((ProblemPair) data.getValue()).problem);
            if (data.getValue() instanceof ProblemTriple)
                return new SimpleStringProperty(((ProblemTriple) data.getValue()).problem);
            return new SimpleStringProperty("");
        });

        table.getColumns().addAll(storyCol, problemCol);
        table.setItems(FXCollections.observableArrayList(criterion.qualitaetsProbleme));
        container.getChildren().add(new Label("Problems:"));
        container.getChildren().add(table);

        if (showGoodStoriesCheck.isSelected()) {
            List<String> goodStories = currentReport.stories.stream()
                    .filter(s -> criterion.qualitaetsProbleme.stream().noneMatch(p ->
                            (p instanceof ProblemPair && ((ProblemPair) p).story.equals(s)) ||
                                    (p instanceof ProblemTriple && (((ProblemTriple) p).story1.equals(s) || ((ProblemTriple) p).story2.equals(s)))))
                    .collect(Collectors.toList());

            if (!goodStories.isEmpty()) {
                TextArea goodArea = new TextArea(String.join("\n", goodStories));
                goodArea.setEditable(false);
                container.getChildren().add(new Label("Good Stories:"));
                container.getChildren().add(goodArea);
            }
        }

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        qualityTabs.getTabs().add(new Tab(name, scrollPane));
    }

    /**
     * Zeigt eine Fehlermeldung in einem JavaFX-Dialog an.
     *
     * @param title Titel des Dialogs
     * @param msg   Fehlermeldung
     */
    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Zeigt eine Information in einem JavaFX-Dialog an.
     *
     * @param title Titel des Dialogs
     * @param msg   Nachricht
     */
    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Startmethode der Anwendung.
     *
     * @param args Kommandozeilenargumente
     */
    public static void main(String[] args) {
        launch();
    }
}
