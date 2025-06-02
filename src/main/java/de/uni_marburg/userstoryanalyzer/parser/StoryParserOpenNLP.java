package de.uni_marburg.userstoryanalyzer.parser;

import de.uni_marburg.userstoryanalyzer.model.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Die Klasse {@code StoryParserOpenNLP} dient dem Parsen von User Stories im Stil
 * "As a [persona], I want to [goal], so that [benefit]".
 * <p>
 * Sie nutzt Stanford CoreNLP zur linguistischen Analyse und identifiziert
 * Personas, Aktionen, Entitäten sowie semantische Beziehungen innerhalb der Story.
 * </p>
 */
public class StoryParserOpenNLP {

    /** NLP-Verarbeitungspipeline von Stanford CoreNLP. */
    private final StanfordCoreNLP pipeline;

    /**
     * Konstruktor initialisiert die StanfordCoreNLP-Pipeline mit den benötigten Annotatoren.
     *
     * @throws IOException falls beim Initialisieren ein Fehler auftritt
     */
    public StoryParserOpenNLP() throws IOException {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,depparse");
        this.pipeline = new StanfordCoreNLP(props);
    }

    /**
     * Liest User Stories aus einer Datei und konvertiert jede Zeile in ein {@link UserStory}-Objekt.
     *
     * @param filepath Pfad zur Datei mit den User Stories
     * @return Liste von UserStory-Objekten
     * @throws IOException falls die Datei nicht gelesen werden kann
     */
    public List<UserStory> parseFromFile(String filepath) throws IOException {
        File file = new File(filepath);
        int i = 0;
        List<UserStory> stories = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    stories.add(parse(line));
                    if (stories.get(i).getPid() == null) {
                        stories.get(i).pid = "#GO" + i + "#";
                    }
                }
                i++;
            }
        }
        return stories;
    }

    /**
     * Parst eine einzelne User Story aus einem String.
     *
     * @param text die zu parsende User Story
     * @return ein {@link UserStory}-Objekt mit extrahierten Informationen
     */
    public UserStory parse(String text) {
        String pid = extractPid(text);
        String cleanText = text.replace(pid != null ? pid : "", "").trim();

        String role = extract(cleanText, "As (?:a |an |the )?(.*?), I want");
        String goalText = extract(cleanText, "(I want to .*?)(?:,\\s*so that|\\.)");
        String benefitText = extract(cleanText, "so that (.*?)(\\.|$)");

        List<String> personas = !role.isEmpty() ? List.of(role) : List.of();
        List<String> goalActions = extractGoalAction(goalText);
        List<String> benefitActions = extractBenefitsAction(benefitText);
        List<String> goalEntities = extractMainEntities(goalText);
        List<String> benefitEntities = extractMainEntities(benefitText);

        List<de.uni_marburg.userstoryanalyzer.model.Annotation> annotations = new ArrayList<>();
        if (!personas.isEmpty()) annotations.add(de.uni_marburg.userstoryanalyzer.model.Annotation.PERSONA);
        if (!goalActions.isEmpty()) annotations.add(de.uni_marburg.userstoryanalyzer.model.Annotation.GOAL_ACTION);
        if (!benefitActions.isEmpty()) annotations.add(de.uni_marburg.userstoryanalyzer.model.Annotation.BENEFIT_ACTION);
        if (!goalEntities.isEmpty()) annotations.add(de.uni_marburg.userstoryanalyzer.model.Annotation.GOAL_ENTITY);
        if (!benefitEntities.isEmpty()) annotations.add(de.uni_marburg.userstoryanalyzer.model.Annotation.BENEFIT_ENTITY);
        if (!benefitText.isEmpty()) annotations.add(de.uni_marburg.userstoryanalyzer.model.Annotation.BENEFIT);

        List<Relation> relations = buildRelations(personas, goalActions, benefitActions, goalEntities, benefitEntities);

        return new UserStory(
                pid,
                text,
                personas,
                new Action(goalActions, benefitActions),
                new Entity(goalEntities, benefitEntities),
                benefitText,
                annotations,
                relations
        );
    }

    /**
     * Extrahiert ein Element mit einem gegebenen Regex-Muster aus dem Text.
     *
     * @param text der zu durchsuchende Text
     * @param regex regulärer Ausdruck zur Extraktion
     * @return extrahierter String oder leer, falls nicht gefunden
     */
    private String extract(String text, String regex) {
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    /**
     * Extrahiert die Prozess-ID (z.B. #ABC123#) aus dem Text.
     *
     * @param text Text mit möglicher PID
     * @return PID-String oder {@code null}, wenn nicht gefunden
     */
    private String extractPid(String text) {
        Matcher matcher = Pattern.compile("#[A-Za-z0-9]+#").matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    /**
     * Extrahiert Zielaktionen (Verben) aus dem Zieltext.
     *
     * @param goalText Text mit dem Ziel der User Story
     * @return Liste von relevanten Verben (Aktionen)
     */
    private List<String> extractGoalAction(String goalText) {
        if (goalText == null || goalText.isEmpty()) return List.of();
        List<String> verbs = new ArrayList<>();

        Annotation doc = new Annotation(goalText);
        pipeline.annotate(doc);

        for (CoreLabel token : doc.get(CoreAnnotations.TokensAnnotation.class)) {
            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            if (pos.startsWith("VB") && !List.of("want", "need", "like").contains(token.originalText().toLowerCase())) {
                verbs.add(token.originalText());
            }
        }

        return verbs;
    }

    /**
     * Extrahiert Nutzenaktionen (Verben) basierend auf Subjekt-Prädikat-Beziehungen.
     *
     * @param text Nutzenbeschreibung
     * @return Liste von Verben, die mit Nutzenhandlungen verbunden sind
     */
    private List<String> extractBenefitsAction(String text) {
        if (text == null) return null;
        List<String> verbs = new ArrayList<>();
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);

            for (SemanticGraphEdge edge : dependencies.edgeIterable()) {
                if (edge.getRelation().getShortName().equals("nsubj")) {
                    String verb = edge.getGovernor().originalText();
                    verbs.add(verb);
                }
            }
        }
        return verbs;
    }

    /**
     * Extrahiert Hauptentitäten aus einem Textteil basierend auf POS-Tags nach dem Hauptverb.
     *
     * @param text der zu analysierende Text
     * @return Liste erkannter Entitäten
     */
    private List<String> extractMainEntities(String text) {
        if (text == null || text.isEmpty()) return List.of();

        List<String> entities = new ArrayList<>();
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        boolean foundMainVerb = false;
        StringBuilder currentEntity = new StringBuilder();

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.originalText();
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                if (!foundMainVerb && pos.startsWith("VB") && isAuxiliaryVerb(word)) {
                    foundMainVerb = true;
                    continue;
                }

                if (foundMainVerb) {
                    if (pos.startsWith("NN") || pos.equals("JJ") || pos.equals("JJR") || pos.equals("JJS")
                            || pos.equals("DT") || pos.equals("RB") || pos.equals("RBR") || pos.equals("RBS")) {
                        if (!currentEntity.isEmpty()) currentEntity.append(" ");
                        currentEntity.append(word);
                    } else if (pos.equals("IN")) {
                        if (!currentEntity.isEmpty()) currentEntity.append(" ").append(word);
                    } else {
                        if (!currentEntity.isEmpty()) {
                            entities.add(currentEntity.toString().trim());
                            currentEntity.setLength(0);
                        }
                    }
                }
            }
        }

        if (!currentEntity.isEmpty()) {
            entities.add(currentEntity.toString().trim());
        }

        return entities.stream()
                .filter(e -> e.length() > 3 && isAuxiliaryVerb(e.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Prüft, ob ein Wort ein Hilfsverb ist.
     *
     * @param word das zu überprüfende Wort
     * @return {@code true}, wenn es sich um ein Hilfsverb handelt
     */
    private boolean isAuxiliaryVerb(String word) {
        return !List.of("is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "will", "would", "shall", "should", "can", "could", "may", "might", "must", "do", "does", "did", "want", "need", "like")
                .contains(word.toLowerCase());
    }

    /**
     * Erstellt semantische Relationen zwischen den extrahierten Bestandteilen einer User Story.
     *
     * @param personas         extrahierte Benutzerrollen
     * @param goalActions      extrahierte Zielaktionen
     * @param benefitActions   extrahierte Nutzenaktionen
     * @param goalEntities     extrahierte Ziel-Entitäten
     * @param benefitEntities  extrahierte Nutzen-Entitäten
     * @return Liste der aufgebauten Relationen
     */
    public List<Relation> buildRelations(List<String> personas,
                                         List<String> goalActions,
                                         List<String> benefitActions,
                                         List<String> goalEntities,
                                         List<String> benefitEntities) {

        List<Relation> relations = new ArrayList<>();

        // TRIGGERS: Persona → GoalAction
        for (String action : goalActions) {
            for (String persona : personas) {
                relations.add(new Relation(RelationType.TRIGGERS, persona, action));
            }
        }

        // TARGETS: GoalAction → GoalEntity
        for (String action : goalActions) {
            for (String entity : goalEntities) {
                relations.add(new Relation(RelationType.TARGETS, action, entity));
            }
        }

        // TARGETS: BenefitAction → BenefitEntity
        for (String action : benefitActions) {
            for (String entity : benefitEntities) {
                relations.add(new Relation(RelationType.TARGETS, action, entity));
            }
        }

        // CONTAINS: GoalEntity → BenefitEntity
        for (String goalEntity : goalEntities) {
            for (String benefitEntity : benefitEntities) {
                relations.add(new Relation(RelationType.CONTAINS, goalEntity, benefitEntity));
            }
        }

        return relations;
    }
}




