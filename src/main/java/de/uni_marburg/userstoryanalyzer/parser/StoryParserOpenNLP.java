package de.uni_marburg.userstoryanalyzer.parser;

import de.uni_marburg.userstoryanalyzer.model.*;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import java.io.InputStream;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class StoryParserOpenNLP {

    private final TokenizerME tokenizer;
    private final POSTaggerME posTagger;
    private static final Set<String> AUX_VERBS = Set.of(
            "have", "has", "had", "be", "am", "is", "are", "was", "were", "been",
            "do", "does", "did", "can", "could", "shall", "should", "will",
            "would", "may", "might", "must", "'d", "'s", "'ll"
    );

    public StoryParserOpenNLP() throws Exception {
        try (InputStream modelInToken = getClass().getResourceAsStream("/models/en-token.bin");
             InputStream modelInPOS = getClass().getResourceAsStream("/models/en-pos-maxent.bin")) {
            tokenizer = new TokenizerME(new TokenizerModel(modelInToken));
            posTagger = new POSTaggerME(new POSModel(modelInPOS));
        }
    }

    public UserStory parse(String storyText) {
        if (storyText == null || storyText.trim().isEmpty()) {
            return createEmptyUserStory();
        }

        // Extract PID if present
        String pid = extractPid(storyText);
        String cleanText = storyText.replace(pid != null ? pid : "", "").trim();

        // Extract components
        String role = extractRole(cleanText);
        String goal = extractGoal(cleanText);
        String benefit = extractBenefit(cleanText);

        // Process actions and entities with improved extraction
        List<String> goalActions = extractMainActions(goal);
        List<String> benefitActions = extractMainActions(benefit);
        List<String> goalEntities = extractMainEntities(goal);
        List<String> benefitEntities = extractMainEntities(benefit);

        // Clean and normalize entities
        goalEntities = cleanEntities(goalEntities);
        benefitEntities = cleanEntities(benefitEntities);

        // Build relations
        List<Relation> relations = buildRelations(role, goalActions, benefitActions, goalEntities, benefitEntities);

        return new UserStory(
                pid,
                storyText,
                List.of(role),
                new Action(goalActions, benefitActions),
                new Entity(goalEntities, benefitEntities),
                benefit,
                generateAnnotations(role, goalActions, benefitActions, goalEntities, benefitEntities),
                relations
        );
    }

    private UserStory createEmptyUserStory() {
        return new UserStory(
                null, "", List.of(),
                new Action(List.of(), List.of()),
                new Entity(List.of(), List.of()),
                "", List.of(), List.of()
        );
    }

    private String extractPid(String text) {
        Matcher m = Pattern.compile("#[A-Za-z0-9]+#").matcher(text);
        return m.find() ? m.group() : null;
    }

    private String extractRole(String text) {
        Matcher m = Pattern.compile("As (?:a |an |the )?(.*?), I want").matcher(text);
        return m.find() ? m.group(1).trim() : "";
    }

    private String extractGoal(String text) {
        Matcher m = Pattern.compile("I want to (.*?)(?:, so that|$)").matcher(text);
        return m.find() ? m.group(1).trim() : "";
    }

    private String extractBenefit(String text) {
        Matcher m = Pattern.compile("so that (.*?)(?:\\.|$)").matcher(text);
        return m.find() ? m.group(1).trim() : "";
    }

    private List<String> extractMainActions(String text) {
        if (text == null || text.isEmpty()) return List.of();

        // First try to extract the main action before any objects
        Matcher verbMatcher = Pattern.compile("^([a-z]+)(?:\\s|$)").matcher(text.toLowerCase());
        if (verbMatcher.find()) {
            String baseVerb = verbMatcher.group(1);
            if (!AUX_VERBS.contains(baseVerb)) {
                // Find the original case version
                Matcher originalCaseMatcher = Pattern.compile("(?i)\\b" + baseVerb + "\\b").matcher(text);
                if (originalCaseMatcher.find()) {
                    String originalVerb = originalCaseMatcher.group();
                    return List.of(originalVerb);
                }
            }
        }

        // Fallback to POS tagging
        String[] tokens = tokenizer.tokenize(text);
        String[] tags = posTagger.tag(tokens);

        for (int i = 0; i < tokens.length; i++) {
            if (tags[i].startsWith("VB") && !AUX_VERBS.contains(tokens[i].toLowerCase())) {
                return List.of(tokens[i]);
            }
        }

        return List.of();
    }

    private List<String> extractMainEntities(String text) {
        if (text == null || text.isEmpty()) return List.of();

        // Special handling for common patterns
        if (text.contains(" for ")) {
            String afterFor = text.split(" for ")[1];
            return List.of(afterFor.split("(,| and | or | but )")[0].trim());
        }

        // Try to extract the main noun phrase after the verb
        String[] tokens = tokenizer.tokenize(text);
        String[] tags = posTagger.tag(tokens);

        List<String> entities = new ArrayList<>();
        StringBuilder currentEntity = new StringBuilder();

        boolean foundVerb = false;
        for (int i = 0; i < tokens.length; i++) {
            if (tags[i].startsWith("VB") && !AUX_VERBS.contains(tokens[i].toLowerCase())) {
                foundVerb = true;
                continue;
            }

            if (foundVerb && (tags[i].startsWith("NN") || tags[i].equals("JJ") || tags[i].equals("DT"))) {
                if (currentEntity.length() > 0) currentEntity.append(" ");
                currentEntity.append(tokens[i]);

                // Look ahead for prepositional phrases
                if (i + 1 < tokens.length && tags[i+1].equals("IN")) {
                    currentEntity.append(" ").append(tokens[i+1]);
                    if (i + 2 < tokens.length && tags[i+2].startsWith("NN")) {
                        currentEntity.append(" ").append(tokens[i+2]);
                        i += 2;
                    } else {
                        i += 1;
                    }
                }
            } else if (foundVerb && currentEntity.length() > 0) {
                entities.add(currentEntity.toString());
                currentEntity.setLength(0);
            }
        }

        if (currentEntity.length() > 0) {
            entities.add(currentEntity.toString());
        }

        // Filter out any remaining verbs or small words
        return entities.stream()
                .filter(e -> e.length() > 3 && !AUX_VERBS.contains(e.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<String> cleanEntities(List<String> entities) {
        return entities.stream()
                .map(e -> {
                    // Remove leading articles
                    e = e.replaceAll("^(the|a|an) ", "");
                    // Capitalize first letter
                    if (!e.isEmpty()) {
                        e = e.substring(0, 1).toUpperCase() + e.substring(1);
                    }
                    return e;
                })
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Relation> buildRelations(String role,
                                          List<String> goalActions,
                                          List<String> benefitActions,
                                          List<String> goalEntities,
                                          List<String> benefitEntities) {
        List<Relation> relations = new ArrayList<>();

        // Role → Goal Action (TRIGGERS)
        goalActions.forEach(action ->
                relations.add(new Relation(RelationType.TRIGGERS, role, action)));

        // Goal Action → Goal Entity (TARGETS)
        goalActions.forEach(action ->
                goalEntities.forEach(entity ->
                        relations.add(new Relation(RelationType.TARGETS, action, entity))));

        // Benefit Action → Benefit Entity (TARGETS)
        if (!benefitActions.isEmpty() && !benefitEntities.isEmpty()) {
            String mainBenefitAction = benefitActions.get(0);
            benefitEntities.forEach(entity ->
                    relations.add(new Relation(RelationType.TARGETS, mainBenefitAction, entity)));
        }

        // CONTAINS-Hierarchie für zusammengesetzte Entitäten
        goalEntities.forEach(entity1 ->
                benefitEntities.forEach(entity2 -> {
                    if (entity2.toLowerCase().contains(entity1.toLowerCase())) {
                        relations.add(new Relation(RelationType.CONTAINS, entity1, entity2));
                    }
                }));

        return relations;
    }

    private List<Annotation> generateAnnotations(String role,
                                                 List<String> goalActions,
                                                 List<String> benefitActions,
                                                 List<String> goalEntities,
                                                 List<String> benefitEntities) {
        List<Annotation> annotations = new ArrayList<>();

        if (!role.isEmpty()) annotations.add(Annotation.PERSONA);
        if (!goalActions.isEmpty()) annotations.add(Annotation.GOAL_ACTION);
        if (!benefitActions.isEmpty()) annotations.add(Annotation.BENEFIT_ACTION);
        if (!goalEntities.isEmpty()) annotations.add(Annotation.GOAL_ENTITY);
        if (!benefitEntities.isEmpty()) annotations.add(Annotation.BENEFIT_ENTITY);
        if (!benefitActions.isEmpty() || !benefitEntities.isEmpty()) annotations.add(Annotation.BENEFIT);

        return annotations;
    }
}