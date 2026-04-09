package de.uni_marburg.userstoryanalyzer;

import de.uni_marburg.userstoryanalyzer.gui.UserStoryViewerApp;

/**
 * Entry point for the fat JAR.
 * JavaFX requires that the manifest Main-Class does NOT extend Application
 * when running from a classpath-based fat JAR. This launcher delegates to
 * UserStoryViewerApp.launch() which sets up the JavaFX toolkit correctly.
 */
public class AppLauncher {
    public static void main(String[] args) {
        UserStoryViewerApp.launch(UserStoryViewerApp.class, args);
    }
}
