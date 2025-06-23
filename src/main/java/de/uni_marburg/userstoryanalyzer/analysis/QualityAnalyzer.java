package de.uni_marburg.userstoryanalyzer.analysis;

import de.uni_marburg.userstoryanalyzer.export.QualityReportExporter;
import de.uni_marburg.userstoryanalyzer.model.Action;
import de.uni_marburg.userstoryanalyzer.model.Annotation;
import de.uni_marburg.userstoryanalyzer.model.Entity;
import de.uni_marburg.userstoryanalyzer.model.UserStory;
import de.uni_marburg.userstoryanalyzer.parser.StoryParserOpenNLP;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Die Klasse QualityAnalyzer enthält Methoden zur Analyse der Qualität von User Stories.
 * Bewertet werden Kriterien wie Wohlgeformtheit, Atomarität, Uniformität, Minimalität und Vollständigkeit.
 */
public class QualityAnalyzer {

    // Keywords für Nutzungsaktionen (z.B. die typischen CRUD-Read/Update/Delete-Aktionen)
    private static final Set<String> USAGE_KEYWORDS = Set.of("read", "view", "edit", "update", "delete", "remove");

    // Keywords für Erstellung einer Entität
    private static final Set<String> CREATION_KEYWORDS = Set.of("create", "add", "register", "insert");

    // Hinweise auf unnötige Zusatzinformationen (nicht minimal)
    private static final List<String> EXTRA_INDICATORS = List.of(
            "note", "see", "mockup", "example", "e.g.", "i.e.",
            "first", "then", "afterwards", "because", "which means", "in other words"
    );

    /**
     * Prüft, ob der Text analysierbar ist (d.h. einer Grundstruktur folgt).
     */
    public static boolean isAnalysable(UserStory story) {
        return !story.getAnnotations().isEmpty();
    }

    /**
     * Prüft, ob eine User Story alle nötigen Annotationen für Wohlgeformtheit besitzt.
     */
    public static boolean isWellFormed(UserStory story) {
        return story.getAnnotations().containsAll(List.of(
                Annotation.PERSONA, Annotation.GOAL_ACTION, Annotation.GOAL_ENTITY
        ));
    }

    /**
     * Prüft, ob eine Story atomar ist (d.h. genau ein Ziel enthält und ein Nutzen definiert ist).
     */
    public static boolean isAtomic(UserStory story) {
        return story.getAction().getGoal().size() == 1 &&
                story.getAnnotations().contains(Annotation.BENEFIT_ACTION);
    }

    /**
     * Prüft, ob eine Story dem einheitlichen Format folgt:
     * "As a <Role>, I want <Goal>, so that <Benefit>".
     */
    public static boolean isUniform(UserStory story) {
        if (story == null || story.getText().isBlank()) return false;

        String lowerStory = story.getText().toLowerCase();

        // Muss mit "As a/an/the" beginnen
        if (!lowerStory.matches("^as (a|an|the) .*")) return false;

        // "I want" muss vorhanden sein
        if (!lowerStory.contains("i want")) return false;

        // Falls "so that" vorhanden, muss es nach "i want" stehen
        int iWantIndex = lowerStory.indexOf("i want");
        int soThatIndex = lowerStory.indexOf("so that");

        return soThatIndex == -1 || soThatIndex > iWantIndex;
    }

    /**
     * Prüft, ob eine Story minimal ist – also nur Rolle, Ziel, Nutzen enthält.
     * Keine Hinweise, Beispiele, Notizen oder mehrere Sätze sind erlaubt.
     */
    public static boolean isMinimal(String story) {
        if (story == null || story.isBlank()) return false;

        String clean = story.trim().toLowerCase();

        // Keywords für Zusatzinformationen erkennen
        for (String keyword : EXTRA_INDICATORS) {
            if (clean.contains(keyword)) return false;
        }

        // Zusätzliche Informationen in Klammern deuten auf Nicht-Minimalität hin
        if (clean.matches(".*\\(.*\\).*")) return false;

        // Wenn "so that" vorhanden ist, prüfen ob danach weitere Sätze folgen
        String[] parts = clean.split("so that", 2);
        if (parts.length > 1) {
            String benefit = parts[1].trim();
            if (benefit.contains(".") && benefit.indexOf('.') < benefit.length() - 1) {
                return false;
            }
        } else {
            // Wenn kein "so that", dürfen nicht mehrere Sätze vorhanden sein
            long sentenceCount = clean.chars().filter(c -> c == '.').count();
            if (sentenceCount > 1) return false;
        }

        return true;
    }

    /**
     * Normalisiert ein Wort in seine Singularform.
     */
    private static String normalizeToSingular(String word) {
        word = word.toLowerCase().trim();
        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y";
        } else if (word.endsWith("ses") || word.endsWith("xes") || word.endsWith("zes")) {
            return word.substring(0, word.length() - 2); // e.g. "processes" -> "process"
        } else if (word.endsWith("s") && !word.endsWith("ss")) {
            return word.substring(0, word.length() - 1); // e.g. "accounts" -> "account"
        }
        return word;
    }

    // Wandelt nur das letzte Wort in Singular um
    private static String normalizeAllWordsToSingular(String phrase) {
        String[] words = phrase.trim().split("\\s+");
        if (words.length == 0) return phrase;

        for (int i = 0; i < words.length; i++) {
            words[i] = normalizeToSingular(words[i]);
        }

        return String.join(" ", words);
    }

    /**
     * Prüft für eine Menge von Stories, ob sie vollständig ist:
     * Wenn eine Story auf ein Objekt zugreift (z.B. "read profile"),
     * muss es auch eine Story geben, die dieses Objekt erstellt ("create profile").
     */
    public static void checkVollstaendigkeit(List<UserStory> userStories, QualityCriterionReport report) {
        for (UserStory story : userStories) {

            // Prüfen, ob Story für Vollständigkeitsanalyse relevant ist
            if (!story.getAnnotations().containsAll(List.of(Annotation.GOAL_ACTION, Annotation.GOAL_ENTITY))) {
                report.Vollstaendigkeit.addSuccess(); // Nicht analysierbar, daher als OK gewertet
                continue;
            }

            Action usageAction = story.getAction();
            Entity usageEntity = story.getEntity();

            // Prüfen, ob Aktion eine Nutzung (z.B. read/edit) erfordert
            boolean needsCreate = USAGE_KEYWORDS.stream().anyMatch(keyword ->
                    usageAction.getGoal().stream().anyMatch(g -> g.toLowerCase().contains(keyword)));

            if (!needsCreate) {
                report.Vollstaendigkeit.addSuccess(); // Kein Erstellungsbedarf
                continue;
            }

            // Prüfen, ob passende "Create"-Story existiert
            boolean hasCreate = userStories.stream().anyMatch(candidate -> {
                if (candidate == story) return false;

                if (!candidate.getAnnotations().containsAll(List.of(Annotation.GOAL_ACTION, Annotation.GOAL_ENTITY)))
                    return false;

                Action cAction = candidate.getAction();
                Entity cEntity = candidate.getEntity();

                boolean isCreate = CREATION_KEYWORDS.stream().anyMatch(keyword ->
                        cAction.getGoal().stream().anyMatch(g -> g.toLowerCase().contains(keyword)));

                boolean sameEntity = usageEntity.getGoal().stream().anyMatch(
                        usageTerm -> cEntity.getGoal().stream().anyMatch(
                                candidateTerm ->
                                        normalizeAllWordsToSingular(candidateTerm).contains(normalizeAllWordsToSingular(usageTerm)) ||
                                                normalizeAllWordsToSingular(usageTerm).contains(normalizeAllWordsToSingular(candidateTerm))
                        )
                );


                return isCreate && sameEntity;
            });

            if (hasCreate) {
                report.Vollstaendigkeit.addSuccess();
            } else {
                report.Vollstaendigkeit.addProblem(
                        story.getText(),
                        "Missing corresponding 'create' story for entity used in this story."
                );
            }
        }
    }

    /**
     * Hauptmethode zur Analyse aller User Stories anhand mehrerer Qualitätskriterien.
     */
    public static QualityCriterionReport analyzeStories(List<UserStory> stories) {
        QualityCriterionReport report = new QualityCriterionReport();

        for (UserStory userStory : stories) {
            String storyText = userStory.getText();

            //Analysierbar prüfen
            if (isAnalysable(userStory)){
                report.stories.add(storyText);

                // Wohlgeformtheit prüfen
                if (!isWellFormed(userStory)) {
                    report.Wohlgeformtheit.addProblem(storyText, "Story lacks required annotations.");
                } else {
                    report.Wohlgeformtheit.addSuccess();
                }

                // Atomarität prüfen
                if (!isAtomic(userStory)) {
                    report.Atomaritaet.addProblem(storyText, "More than one requirement or missing benefit.");
                } else {
                    report.Atomaritaet.addSuccess();
                }

                // Uniformität prüfen
                if (!isUniform(userStory)) {
                    report.Uniformitaet.addProblem(storyText, "Does not follow uniform structure.");
                } else {
                    report.Uniformitaet.addSuccess();
                }

                // Minimalität prüfen
                if (!isMinimal(storyText)) {
                    report.Minimalitaet.addProblem(storyText, "Story contains extra information or notes.");
                } else {
                    report.Minimalitaet.addSuccess();
                }
            } else {
                report.nicht_analysierbar.add(storyText);
            }

        }

        // Vollständigkeit separat prüfen, da sie über mehrere Stories hinweg analysiert wird
        checkVollstaendigkeit(stories, report);

        return report;
    }

    public static void main(String[] args) throws Exception {

        StoryParserOpenNLP parser = new StoryParserOpenNLP();
        String filePath = "src/main/resources/models/US.txt";
        List<UserStory> userStories;
        userStories = parser.parseFromFile(filePath);
        QualityReportExporter exporter = new QualityReportExporter();
        // Datei im Projektverzeichnis erzeugen, damit sie sichtbar ist
        File outputFile = new File("src/main/resources/json-files/qualityExporterReport.json");
        exporter.writeToJson(QualityAnalyzer.analyzeStories(userStories), outputFile.getAbsolutePath());

        for (UserStory userStory : userStories ){
            System.out.println(userStory.toString());
        }

    }
}
