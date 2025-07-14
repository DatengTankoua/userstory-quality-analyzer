package de.uni_marburg.userstoryanalyzer.analysis;

import de.uni_marburg.userstoryanalyzer.export.QualityReportExporter;
import de.uni_marburg.userstoryanalyzer.model.*;
import de.uni_marburg.userstoryanalyzer.parser.StoryParserOpenNLP;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import net.didion.jwnl.*;
import net.didion.jwnl.data.*;
import net.didion.jwnl.dictionary.Dictionary;

import java.io.InputStream;
import java.util.stream.Collectors;

/**
 * Die Klasse QualityAnalyzer enthält Methoden zur Analyse der Qualität von User Stories.
 * Bewertet werden Kriterien wie Wohlgeformtheit, Atomarität, Uniformität, Minimalität und Vollständigkeit.
 */
public class QualityAnalyzer {

    static Dictionary wordnet;

    // Initialisierung von JWNL
    static {
        try {
            InputStream props = QualityAnalyzer.class.getResourceAsStream("/models/file_properties.xml");
            assert props != null;
            JWNL.initialize(props);
            wordnet = Dictionary.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Keywords für Nutzungsaktionen (z.B. die typischen CRUD-Read/Update/Delete-Aktionen)
    private static final Set<String> USAGE_KEYWORDS = Set.of("read", "view", "edit", "update", "delete", "remove", "change");

    // Keywords für Erstellung einer Entität
    private static final Set<String> CREATION_KEYWORDS = Set.of("create", "add", "register", "insert");

    private static Set<String> expandedUsage;

    static {
        try {
            expandedUsage = expandKeywordsWithSynonyms(wordnet, USAGE_KEYWORDS, POS.VERB);
        } catch (JWNLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<String> expandedCreation;

    static {
        try {
            expandedCreation = expandKeywordsWithSynonyms(wordnet, CREATION_KEYWORDS, POS.VERB);
        } catch (JWNLException e) {
            throw new RuntimeException(e);
        }
    }

    // Hinweise auf unnötige Zusatzinformationen (nicht minimal)
    private static final List<String> EXTRA_INDICATORS = List.of(
            "note", "see", "mockup", "example", "e.g.", "i.e.",
            "first", "then", "afterwards", "because", "which means", "in other words"
    );

    private static final Map<String, List<String>> conflictingPhrasesMap = Map.of(
            "delete", List.of("delete any", "delete only", "can delete", "cannot delete", "not allowed to delete", "allowed to delete"),
            "edit", List.of("edit any", "edit only", "can edit", "cannot edit", "not allowed to edit", "allowed to edit"),
            "view", List.of("view all", "view only own", "can view", "not allowed to view"),
            "change", List.of("change any", "change only", "can change", "cannot change")
    );


    public QualityAnalyzer() throws JWNLException {
    }

    public static Set<String> expandKeywordsWithSynonyms(Dictionary dictionary, Set<String> baseWords, POS pos) throws JWNLException {
        Set<String> allWords = new HashSet<>(baseWords);

        for (String word : baseWords) {
            IndexWord indexWord = dictionary.lookupIndexWord(pos, word);
            if (indexWord == null) continue;

            for (Synset synset : indexWord.getSenses()) {
                for (Word w : synset.getWords()) {
                    allWords.add(w.getLemma().replace('_', ' ')); // z.B. "log_in"
                }
            }
        }

        return allWords;
    }

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

    // Wandelt alle Wörte in Singular um
    private static String normalizeAllWordsToSingular(String phrase) {
        String[] words = phrase.trim().split("\\s+");
        if (words.length == 0) return phrase;

        for (int i = 0; i < words.length; i++) {
            words[i] = normalizeToSingular(words[i]);
        }

        return String.join(" ", words);
    }

    public static boolean areSemanticallySimilar(String word1, String word2) {
        try {
            IndexWord w1 = wordnet.lookupIndexWord(POS.VERB, word1.toLowerCase());
            IndexWord w2 = wordnet.lookupIndexWord(POS.VERB, word2.toLowerCase());

            if (w1 == null || w2 == null) return false;

            for (Synset syn1 : w1.getSenses()) {
                for (Word synWord : syn1.getWords()) {
                    if (synWord.getLemma().equalsIgnoreCase(word2)) {
                        return true;
                    }
                }
            }

            for (Synset syn2 : w2.getSenses()) {
                for (Word synWord : syn2.getWords()) {
                    if (synWord.getLemma().equalsIgnoreCase(word1)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void checkRedundanzfreiheit(List<UserStory> stories, QualityCriterionReport report) {
        for (int i = 0; i < stories.size(); i++) {
            UserStory a = stories.get(i);
            if (isWellFormed(a) && isAtomic(a)){
                for (int j = i + 1; j < stories.size(); j++) {
                    UserStory b = stories.get(j);
                    if (isWellFormed(b) && isAtomic(b)){

                        boolean sameAction = a.getAction().getGoal().stream().anyMatch(
                                actionA -> b.getAction().getGoal().stream().anyMatch(
                                        actionB -> areSemanticallySimilar(actionA, actionB) || actionA.contains(actionB)));

                        boolean sameEntity = a.getEntity().getGoal().stream().anyMatch(
                                entityA -> b.getEntity().getGoal().stream().anyMatch(
                                        entityB -> normalizeAllWordsToSingular(entityA).contains(normalizeAllWordsToSingular(entityB)) ||
                                                normalizeAllWordsToSingular(entityB).contains(normalizeAllWordsToSingular(entityA))));

                        if (sameAction && sameEntity) {
                            report.Redundanzfreiheit.addProblem(a.getText(), b.getText(), "These stories have a redundant goal part.");
                        }
                    } else if (isUniform(a) && a.getAnnotations().contains(Annotation.BENEFIT_ACTION) && isUniform(b) && b.getAnnotations().contains(Annotation.BENEFIT_ACTION)) {
                        boolean sameActionBenefit = a.getAction().getBenefit().stream().anyMatch(
                                benefitA -> b.getAction().getGoal().stream().anyMatch(
                                        benefitB -> areSemanticallySimilar(benefitA, benefitB) || benefitA.contains(benefitB)));

                        boolean sameEntityBenefit = a.getEntity().getBenefit().stream().anyMatch(
                                entityA -> b.getEntity().getGoal().stream().anyMatch(
                                        entityB -> normalizeAllWordsToSingular(entityA).contains(normalizeAllWordsToSingular(entityB)) ||
                                                normalizeAllWordsToSingular(entityB).contains(normalizeAllWordsToSingular(entityA))));

                        if (sameActionBenefit && sameEntityBenefit) {
                            report.Redundanzfreiheit.addProblem(a.getText(), b.getText(), "These stories have a redundant benefit part.");
                        }
                    }
                }
            }
        }
        report.Redundanzfreiheit.anzahlVonProblemlosen = stories.size() - report.Redundanzfreiheit.anzahlVonProblemen;
    }

    public static boolean isHyponym(POS pos, String specific, String general) throws JWNLException {
        IndexWord word = wordnet.getIndexWord(pos, specific);
        if (word == null) return false;

        for (Synset sense : word.getSenses()) {
            for (Pointer ptr : sense.getPointers(PointerType.HYPONYM)) {
                Synset hypo = (Synset) ptr.getTarget();
                for (Word w : hypo.getWords()) {
                    if (w.getLemma().equalsIgnoreCase(general)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }




    public static void checkUnabhaengigkeit(Collection<UserStory> stories, QualityCriterionReport report) throws JWNLException {

        List<UserStory> list = stories.stream()
                .filter(us -> us.getPersona() != null && !us.getPersona().isEmpty())
                .collect(Collectors.toList());

        Map<UserStory, Set<String>> normalizedEntities = new HashMap<>();
        for (UserStory us : list) {
            Set<String> normalized = us.getEntity().getGoal().stream()
                    .map(e -> normalizeAllWordsToSingular(e).toLowerCase())
                    .collect(Collectors.toSet());
            normalizedEntities.put(us, normalized);
        }

        for (int i = 0; i < list.size(); i++) {
            UserStory a = list.get(i);
            Set<String> aEnt = normalizedEntities.get(a);
            boolean aUses = a.getAction().getGoal().stream()
                    .map(String::toLowerCase)
                    .anyMatch(g -> expandedUsage.stream().anyMatch(g::contains));
            String personaA = a.getPersona().get(0).toLowerCase();

            for (int j = i + 1; j < list.size(); j++) {
                UserStory b = list.get(j);
                boolean bCreates = b.getAction().getGoal().stream()
                        .map(String::toLowerCase)
                        .anyMatch(g -> expandedCreation.stream().anyMatch(g::contains));

                String personaB = b.getPersona().get(0).toLowerCase();
                if (!personaA.equals(personaB)) continue; // Nur gleiche Personas vergleichen

                Set<String> bEnt = normalizedEntities.get(b);

                // Direkt identische oder sich überschneidende Entitäten
                boolean hasSharedEntity = !Collections.disjoint(aEnt, bEnt);

                // Mögliche Unabhängigkeit brechende Kombinationen
                if ((hasSharedEntity) || (aUses && bCreates)) {
                    report.Unabhaengigkeit.addProblem(
                            a.getText(),
                            b.getText(),
                            !(aUses && bCreates)
                                    ? "Semantically dependent (hyponymy detected)."
                                    : "Dependent by create/use on same entity."
                    );
                }
            }
        }
        report.Unabhaengigkeit.anzahlVonProblemlosen =
                stories.size() - report.Unabhaengigkeit.anzahlVonProblemen;
    }


    /**
     * Prüft für eine Menge von Stories, ob sie vollständig ist:
     * Wenn eine Story auf ein Objekt zugreift (z.B. "read profile"),
     * muss es auch eine Story geben, die dieses Objekt erstellt ("create profile").
     */
    public static void checkVollstaendigkeit(List<UserStory> userStories, QualityCriterionReport report) {
        for (UserStory story : userStories) {

            // Prüfen, ob Story für Vollständigkeitsanalyse relevant ist
            if (!new HashSet<>(story.getAnnotations()).containsAll(List.of(Annotation.GOAL_ACTION, Annotation.GOAL_ENTITY))) {
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

                if (!new HashSet<>(candidate.getAnnotations()).containsAll(List.of(Annotation.GOAL_ACTION, Annotation.GOAL_ENTITY)))
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

    public static void checkKonfliktfreiheit(List<UserStory> stories, QualityCriterionReport report) {
        for (int i = 0; i < stories.size(); i++) {
            UserStory a = stories.get(i);
            String textA = a.getText().toLowerCase();

            for (int j = i + 1; j < stories.size(); j++) {
                UserStory b = stories.get(j);
                String textB = b.getText().toLowerCase();

                for (Map.Entry<String, List<String>> entry : conflictingPhrasesMap.entrySet()) {
                    String action = entry.getKey();
                    List<String> phrases = entry.getValue();

                    List<String> matchedA = phrases.stream().filter(textA::contains).collect(Collectors.toList());
                    List<String> matchedB = phrases.stream().filter(textB::contains).collect(Collectors.toList());

                    if (!matchedA.isEmpty() && !matchedB.isEmpty()) {
                        if (isContradictory(matchedA.get(0), matchedB.get(0))) {
                            report.Konfliktfreiheit.addProblem(
                                    a.getText(),
                                    b.getText(),
                                    "Conflict regarding access to " + action + ": Contradictory permissions (" + matchedA.get(0) + " vs. " + matchedB.get(0) + ")."

                            );
                        }
                    }
                }
            }
        }

        report.Konfliktfreiheit.anzahlVonProblemlosen =
                stories.size() - report.Konfliktfreiheit.anzahlVonProblemen;
    }

    private static boolean isContradictory(String phrase1, String phrase2) {
        if (phrase1.equals(phrase2)) return false;

        return (phrase1.contains("any") && phrase2.contains("only")) ||
                (phrase1.contains("only") && phrase2.contains("any")) ||
                (phrase1.contains("can") && (phrase2.contains("cannot") || phrase2.contains("not allowed"))) ||
                ((phrase1.contains("cannot") || phrase1.contains("not allowed")) && phrase2.contains("can"));
    }



    /**
     * Hauptmethode zur Analyse aller User Stories anhand mehrerer Qualitätskriterien.
     */
    public static QualityCriterionReport analyzeStories(List<UserStory> stories) throws JWNLException {
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

        // Vollständigkeit, Redundanzfreiheit, Unabhängigkeit, Konfliktfreiheit separat prüfen, da sie über mehrere Stories hinweg analysiert wird
        checkRedundanzfreiheit(stories, report);
        checkUnabhaengigkeit(stories, report);
        checkVollstaendigkeit(stories, report);
        checkKonfliktfreiheit(stories, report);

        return report;
    }



    public static QualityCriterionReport analyzeStories(List<UserStory> stories, Set<String> selectedCriteria) throws JWNLException {
        QualityCriterionReport report = new QualityCriterionReport();

        for (UserStory userStory : stories) {
            String storyText = userStory.getText();

            if (isAnalysable(userStory)) {
                report.stories.add(storyText);

                if (selectedCriteria.contains("Wohlgeformtheit")) {
                    if (!isWellFormed(userStory)) {
                        report.Wohlgeformtheit.addProblem(storyText, "Story lacks required annotations.");
                    } else {
                        report.Wohlgeformtheit.addSuccess();
                    }
                }

                if (selectedCriteria.contains("Atomaritaet")) {
                    if (!isAtomic(userStory)) {
                        report.Atomaritaet.addProblem(storyText, "More than one requirement or missing benefit.");
                    } else {
                        report.Atomaritaet.addSuccess();
                    }
                }

                if (selectedCriteria.contains("Uniformitaet")) {
                    if (!isUniform(userStory)) {
                        report.Uniformitaet.addProblem(storyText, "Does not follow uniform structure.");
                    } else {
                        report.Uniformitaet.addSuccess();
                    }
                }

                if (selectedCriteria.contains("Minimalitaet")) {
                    if (!isMinimal(storyText)) {
                        report.Minimalitaet.addProblem(storyText, "Story contains extra information or notes.");
                    } else {
                        report.Minimalitaet.addSuccess();
                    }
                }

            } else {
                report.nicht_analysierbar.add(storyText);
            }
        }

        // المعايير التي تعتمد على أكثر من قصة يجب فحصها بعد الحلقة
        if (selectedCriteria.contains("Redundanzfreiheit")) {
            checkRedundanzfreiheit(stories, report);
        }
        if (selectedCriteria.contains("Unabhaengigkeit")) {
            checkUnabhaengigkeit(stories, report);
        }
        if (selectedCriteria.contains("Vollstaendigkeit")) {
            checkVollstaendigkeit(stories, report);
        }
        if (selectedCriteria.contains("Konfliktfreiheit")) {
            checkKonfliktfreiheit(stories, report);
        }

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
