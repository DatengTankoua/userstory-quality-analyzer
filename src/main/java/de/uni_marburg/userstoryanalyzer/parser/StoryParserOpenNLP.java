package de.uni_marburg.userstoryanalyzer.parser;

import de.uni_marburg.userstoryanalyzer.model.*;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.InputStream;
import java.util.*;
import java.util.regex.*;

public class StoryParserOpenNLP {

    private final SentenceDetectorME sentenceDetector;
    private final TokenizerME tokenizer;
    private final POSTaggerME posTagger;

    public StoryParserOpenNLP() throws Exception {
        //Load the models
        try (InputStream modelInSent = getClass().getResourceAsStream("/models/en-sent.bin");
             InputStream modelInToken = getClass().getResourceAsStream("/models/en-token.bin");
             InputStream modelInPOS = getClass().getResourceAsStream("/models/en-pos-maxent.bin")) {

            sentenceDetector = new SentenceDetectorME(new SentenceModel(modelInSent));
            tokenizer = new TokenizerME(new TokenizerModel(modelInToken));
            posTagger = new POSTaggerME(new POSModel(modelInPOS));
        }
    }

    public UserStory parse(String storyText) {
        //Extract the pid if present
        String pid = extractPid(storyText);
        //Remove pid from text to simplify extraction
        String cleanText = pid != null ? storyText.replace(pid, "").trim() : storyText;

        //Extract role, goal and benefit components
        String role = extractComponent(cleanText, "As (?:a |an |the )?(.*?), I want");
        String goal = extractComponent(cleanText, "I want to (.*?)(, so that|$)");
        String benefit = extractComponent(cleanText, "so that (.*?)(\\.|$)");

        //Get verbs and nouns from goal and benefit
        List<String> goalActions = extractVerbs(goal);
        List<String> benefitActions = extractVerbs(benefit);
        List<String> goalEntities = extractNouns(goal);
        List<String> benefitEntities = extractNouns(benefit);

        //Build relations from role to actions and entities
        List<Relation> relations = buildRelations(role, goalActions, goalEntities);

        //Create and return the UserStory object
        return new UserStory(
                pid,
                cleanText,
                List.of(role),
                new Action(goalActions, benefitActions),
                new Entity(goalEntities, benefitEntities),
                benefit,
                generateAnnotations(role, goalActions, benefitActions, goalEntities, benefitEntities),
                relations
        );
    }

    private String extractPid(String text) {
        //Look for pattern like #U123#
        Matcher m = Pattern.compile("#[A-Z]\\d+#").matcher(text);
        return m.find() ? m.group() : null;
    }

    private String extractComponent(String text, String regex) {
        //Extract substring matching regex group
        Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
        return m.find() ? m.group(1).trim() : "";
    }

    private List<String> extractVerbs(String text) {
        List<String> verbs = new ArrayList<>();
        if (text == null || text.isEmpty()) return verbs;

        //Tokenize and tag POS
        String[] tokens = tokenizer.tokenize(text);
        String[] tags = posTagger.tag(tokens);

        //Collect all verbs (VB, VBD, etc.)
        for (int i = 0; i < tokens.length; i++) {
            if (tags[i].startsWith("VB")) {
                verbs.add(tokens[i].toLowerCase());
            }
        }
        return verbs;
    }

    private List<String> extractNouns(String text) {
        List<String> nouns = new ArrayList<>();
        if (text == null || text.isEmpty()) return nouns;

        //Tokenize and tag POS
        String[] tokens = tokenizer.tokenize(text);
        String[] tags = posTagger.tag(tokens);

        //Collect all nouns (NN, NNS, etc.)
        for (int i = 0; i < tokens.length; i++) {
            if (tags[i].startsWith("NN")) {
                nouns.add(tokens[i]);
            }
        }
        //Remove duplicates but keep order
        return new ArrayList<>(new LinkedHashSet<>(nouns));
    }

    private List<Relation> buildRelations(String role, List<String> actions, List<String> entities) {
        List<Relation> relations = new ArrayList<>();
        //For each action, link role triggers action
        for (String action : actions) {
            relations.add(new Relation(RelationType.TRIGGERS, role, action));
            //Each action targets all entities
            for (String entity : entities) {
                relations.add(new Relation(RelationType.TARGETS, action, entity));
            }
        }
        return relations;
    }

    private List<Annotation> generateAnnotations(String role,
                                                 List<String> goalActions, List<String> benefitActions,
                                                 List<String> goalEntities, List<String> benefitEntities) {
        List<Annotation> annotations = new ArrayList<>();
        annotations.add(Annotation.PERSONA);
        //Mark all goal actions
        goalActions.forEach(a -> annotations.add(Annotation.GOAL_ACTION));
        //Mark all benefit actions
        benefitActions.forEach(a -> annotations.add(Annotation.BENEFIT_ACTION));
        //Mark goal entities
        goalEntities.forEach(e -> annotations.add(Annotation.GOAL_ENTITY));
        //Mark benefit entities
        benefitEntities.forEach(e -> annotations.add(Annotation.BENEFIT_ENTITY));
        return annotations;
    }
}
