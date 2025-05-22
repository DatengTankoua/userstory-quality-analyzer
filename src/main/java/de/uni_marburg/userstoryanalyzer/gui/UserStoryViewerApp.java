package de.uni_marburg.userstoryanalyzer.gui;

import de.uni_marburg.userstoryanalyzer.model.UserStory;
import de.uni_marburg.userstoryanalyzer.model.Action;
import de.uni_marburg.userstoryanalyzer.model.Entity;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/**
 * JavaFX-based GUI to display a list of User Stories in a table format.
 * This is a temporary mock-up using dummy data until the parser is fully functional.
 */
public class UserStoryViewerApp extends Application {

    private final TableView<UserStory> tableView = new TableView<>();
    private final ObservableList<UserStory> userStories = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        stage.setTitle("User Story Viewer");

        // Create table columns
        TableColumn<UserStory, String> pidColumn = new TableColumn<>("PID");
        pidColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPid()));

        TableColumn<UserStory, String> personaColumn = new TableColumn<>("Persona(s)");
        personaColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.join(", ", data.getValue().getPersona())));

        TableColumn<UserStory, String> goalActionsColumn = new TableColumn<>("Goal Actions");
        goalActionsColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.join(", ", data.getValue().getAction().getGoal())));

        TableColumn<UserStory, String> goalEntitiesColumn = new TableColumn<>("Goal Entities");
        goalEntitiesColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.join(", ", data.getValue().getEntity().getGoal())));

        TableColumn<UserStory, String> benefitColumn = new TableColumn<>("Benefit");
        benefitColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getBenefit()));

        // Add columns to the table
        tableView.getColumns().addAll(
                pidColumn,
                personaColumn,
                goalActionsColumn,
                goalEntitiesColumn,
                benefitColumn
        );

        tableView.setItems(userStories);

        // Button to load dummy data (for demo purposes)
        Button dummyButton = new Button("Load Dummy Data");
        dummyButton.setOnAction(e -> loadDummyData());

        // Button to load from file (placeholder for future parser integration)
        Button loadFromFileButton = new Button("Load from File");
        loadFromFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open User Stories File");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                System.out.println("Selected file: " + file.getAbsolutePath());
                // Future: pass file to UserStoryParser and load results into userStories
            }
        });

        // Top layout with both buttons
        HBox topControls = new HBox(10, dummyButton, loadFromFileButton);

        // Layout
        BorderPane root = new BorderPane();
        root.setTop(topControls);
        root.setCenter(tableView);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Loads example User Story data into the table.
     * This method is used for demonstration before the parser is available.
     */
    private void loadDummyData() {
        userStories.clear();

        userStories.add(new UserStory(
                "#U01#",
                "As a Student, I want to search for courses so that I can find suitable ones.",
                List.of("Student"),
                new Action(List.of("search"), List.of("find")),
                new Entity(List.of("courses"), List.of("suitable ones")),
                "Find suitable courses.",
                List.of(), // Annotations
                List.of()  // Relations
        ));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
