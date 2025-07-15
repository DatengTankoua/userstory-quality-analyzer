/**
 * UserStoryViewerApp.java
 *
 * Dieses JavaFX-Programm dient zur Anzeige, Analyse und zum Exportieren der Qualität von User Stories.
 * Es erlaubt dem Benutzer, User Stories aus einer Datei zu laden, bestimmte Qualitätskriterien auszuwählen,
 * ausgewählte Stories zu analysieren und die Ergebnisse im JSON-Format zu exportieren. Die Analyse basiert
 * auf verschiedenen Kriterien wie Wohlgeformtheit, Atomarität, Uniformität, etc. und verwendet OpenNLP zur
 * Sprachverarbeitung.
 *
 * Features:
 * - Laden von User Stories aus Textdateien
 * - Auswahl und Analyse von Qualitätskriterien
 * - Darstellung der Analyseergebnisse in Tabs
 * - Export der Ergebnisse als JSON
 * - Anzeige guter User Stories (optional)
 *
 * @author Ayham Alshaabi
 * @version 1.0
 * @since 2025-07-14
 */
package de.uni_marburg.userstoryanalyzer.gui;
// Alle notwendigen Imports hier
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

/**
 * Hauptklasse der JavaFX-Anwendung zur Qualitätsanalyse von User Stories.
 * Diese Klasse stellt die Benutzeroberfläche bereit, ermöglicht das Laden von User Stories,
 * die Auswahl von Qualitätskriterien, die Analyse sowie den Export der Ergebnisse.
 */
public class UserStoryViewerApp extends Application {

    /** Tabelle zur Anzeige aller geladenen User Stories */
    private final TableView<UserStory> storyTable = new TableView<>();
    /** Observable-Liste für alle User Stories */
    private final ObservableList<UserStory> userStories = FXCollections.observableArrayList();
    /** TabPane für Analyseergebnisse */
    private final TabPane qualityTabs = new TabPane();
    /** Menge der vom Benutzer gewählten Qualitätskriterien */
    private final Set<String> selectedCriteria = new HashSet<>();
    /** Aktueller Analysebericht */
    private QualityCriterionReport currentReport;
    /** Checkbox zum Anzeigen der "guten" User Stories */
    private final CheckBox showGoodStoriesCheck = new CheckBox("Show good stories");

    /**
     * Einstiegspunkt der JavaFX-Anwendung. Initialisiert alle GUI-Komponenten,
     * richtet das Layout ein und zeigt das Hauptfenster an.
     *
     * @param stage Das primäre Stage-Objekt der Anwendung
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
        VBox topControls = new VBox(10, buttonBar, new Label("Select Criteria:"), criteriaBox, showGoodStoriesCheck);

        SplitPane leftSplitPane = new SplitPane();
        leftSplitPane.setOrientation(Orientation.VERTICAL);
        leftSplitPane.getItems().addAll(topControls, storyTable);
        leftSplitPane.setDividerPositions(0.38);

        VBox leftPane = new VBox(leftSplitPane);
        VBox.setVgrow(leftSplitPane, Priority.ALWAYS);


        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().addAll(leftPane, qualityTabs);
        splitPane.setDividerPositions(0.5);

        Scene scene = new Scene(splitPane, 1200, 800);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Erstellt eine neue Spalte für die User Story Tabelle basierend auf dem gegebenen Titel und Extraktor.
     *
     * @param title     Titel der Spalte
     * @param extractor Funktion zur Extraktion der Werte aus einem UserStory-Objekt
     * @return TableColumn für die Tabelle
     */
    private TableColumn<UserStory, String> createColumn(String title, javafx.util.Callback<UserStory, String> extractor) {
        TableColumn<UserStory, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new SimpleStringProperty(extractor.call(data.getValue())));
        return col;
    }

    /**
     * Lädt User Stories aus einer Datei über einen FileChooser und parst sie mit StoryParserOpenNLP.
     *
     * @param stage Stage zur Anzeige des Datei-Dialogs
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
     * Analysiert die vom Benutzer ausgewählten User Stories anhand der ausgewählten Kriterien.
     * Zeigt Warnungen, falls keine Auswahl getroffen wurde.
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

        try {
            currentReport = QualityAnalyzer.analyzeStories(selected, selectedCriteria);
            refreshQualityTabs();
            showInfo("Analysis Complete", "Quality analysis finished successfully.");
        } catch (net.didion.jwnl.JWNLException e) {
            e.printStackTrace();
            showError("Analysis Error", "A JWNL error occurred during analysis:\n" + e.getMessage());
        }
    }


    /**
     * Exportiert die Ergebnisse der Analyse im JSON-Format.
     *
     * @param stage Stage zur Anzeige des Datei-Speichern-Dialogs
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
     * Aktualisiert die Analyse-Tabs basierend auf dem aktuellen Bericht und den gewählten Kriterien.
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
     * Erstellt einen neuen Tab zur Anzeige der Probleme und optional der guten Stories für ein bestimmtes Kriterium.
     *
     * @param name Name des Kriteriums/Tabs
     * @param criterion Die Analyseergebnisse zum Kriterium
     */
    private void addQualityTabWithTable(String name, QualityCriterion criterion) {
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

        SplitPane verticalSplit = new SplitPane();
        verticalSplit.setOrientation(Orientation.VERTICAL);

        VBox topPart = new VBox(5, new Label("Problems:"), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        verticalSplit.getItems().add(topPart);

        if (showGoodStoriesCheck.isSelected()) {
            List<String> goodStories = currentReport.stories.stream()
                    .filter(s -> criterion.qualitaetsProbleme.stream().noneMatch(p ->
                            (p instanceof ProblemPair && ((ProblemPair) p).story.equals(s)) ||
                                    (p instanceof ProblemTriple && (((ProblemTriple) p).story1.equals(s) || ((ProblemTriple) p).story2.equals(s)))))
                    .collect(Collectors.toList());

            if (!goodStories.isEmpty()) {
                TableView<String> goodTable = new TableView<>();
                TableColumn<String, String> goodCol = new TableColumn<>("Good Story");
                goodCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
                goodCol.setPrefWidth(1000); // أو يمكنك ضبط الحجم تلقائيًا

                goodTable.getColumns().add(goodCol);
                goodTable.setItems(FXCollections.observableArrayList(goodStories));
                VBox bottomPart = new VBox(5, new Label("Good Stories:"), goodTable);
                VBox.setVgrow(goodTable, Priority.ALWAYS);
                verticalSplit.getItems().add(bottomPart);
            }
        }

        verticalSplit.setDividerPositions(0.5);
        ScrollPane scrollPane = new ScrollPane(verticalSplit);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        qualityTabs.getTabs().add(new Tab(name, scrollPane));
    }


    /**
     * Zeigt einen Fehlerdialog mit der gegebenen Nachricht.
     *
     * @param title Titel des Dialogs
     * @param msg Nachricht/Fehlerbeschreibung
     */
    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Zeigt einen Informationsdialog mit der gegebenen Nachricht.
     *
     * @param title Titel des Dialogs
     * @param msg Nachricht/Information
     */
    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Startet die JavaFX-Anwendung.
     *
     * @param args Kommandozeilenargumente
     */
    public static void main(String[] args) {
        launch();
    }
}
