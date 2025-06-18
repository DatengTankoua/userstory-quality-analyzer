package de.uni_marburg.userstoryanalyzer.analysis;
import de.uni_marburg.userstoryanalyzer.model.Action;
import de.uni_marburg.userstoryanalyzer.model.UserStory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
public class QualityAnalyzer {

    private static final Pattern WELLFORMED_PATTERN = Pattern.compile(
            "(?i)^as a .*? i want .*? so that .*"
    );

    public static boolean isWellFormed(String story) {
        return WELLFORMED_PATTERN.matcher(story).find();
    }

    public static boolean isAtomic(Action action) {
        return action.getGoal().size() == 1 && action.getBenefit().size() <= 1;
    }

    public static boolean isUniform(String story) {
        return story.trim().toLowerCase().startsWith("as a ");
    }

    public static boolean isMinimal(String story) {
        // Heuristik: Sehr lange Stories oder doppelte Wörter
        String[] words = story.split("\\s+");
        Set<String> uniqueWords = new HashSet<>(Arrays.asList(words));
        return story.length() < 300 && uniqueWords.size() >= words.length * 0.8;
    }

    // Helper to analyze a list of stories
    public static QualityCriterionReport analyzeStories(List<UserStory> stories) {
        QualityCriterionReport report = new QualityCriterionReport();

        for (UserStory userStory : stories) {
            String story = userStory.getText();
            Action action = userStory.getAction();

            // Wohlgeformtheit
            if (!isWellFormed(story)) {
                report.Wohlgeformtheit.addProblem(story, "No role part");
            } else {
                report.Wohlgeformtheit.addSuccess();
            }

            // Atomarität
            if (!isAtomic(action)) {
                report.Atomaritaet.addProblem(story, "More than one requirement and action.");
            } else {
                report.Atomaritaet.addSuccess();
            }

            // Uniformität
            if (!isUniform(story)) {
                report.Uniformitaet.addProblem(story, "Goal part wrong defined.");
            } else {
                report.Uniformitaet.addSuccess();
            }

            // Minimalität
            if (!isMinimal(story)) {
                report.Minimalitaet.addProblem(story, "Contains an extra note.");
            } else {
                report.Minimalitaet.addSuccess();
            }
        }

        return report;
    }
}
