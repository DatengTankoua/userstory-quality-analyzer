package de.uni_marburg.userstoryanalyzer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;

public class UserStory {

    @JsonProperty("PID")
    private final String pid;

    @JsonProperty("Text")
    private final String text;

    @JsonProperty("Persona")
    private final List<String> persona;

    @JsonProperty("Action")
    private final Action action;

    @JsonProperty("Entity")
    private final Entity entity;

    @JsonProperty("Benefit")
    private final String benefit;

    @JsonIgnore
    private final List<Annotation> annotations;

    @JsonIgnore
    private final List<Relation> relations;

    @JsonProperty("Triggers")
    private final List<List<String>> triggers;

    @JsonProperty("Targets")
    private final List<List<String>> targets;

    @JsonProperty("Contains")
    private final List<List<String>> contains;

    public UserStory(String pid, String text, List<String> persona, Action action,
                     Entity entity, String benefit, List<Annotation> annotations,
                     List<Relation> relations) {
        this.pid = pid;
        this.text = text;
        this.persona = persona;
        this.action = action;
        this.entity = entity;
        this.benefit = benefit;
        this.annotations = annotations;
        this.relations = relations;

        this.triggers = relations.stream()
                .filter(r -> r.getType() == RelationType.TRIGGERS)
                .map(r -> List.of(r.getSource(), r.getTarget()))
                .collect(Collectors.toList());

        this.targets = relations.stream()
                .filter(r -> r.getType() == RelationType.TARGETS)
                .map(r -> List.of(r.getSource(), r.getTarget()))
                .collect(Collectors.toList());

        this.contains = relations.stream()
                .filter(r -> r.getType() == RelationType.CONTAINS)
                .map(r -> List.of(r.getSource(), r.getTarget()))
                .collect(Collectors.toList());
    }

    /**
     * Hilfsmethode zur Filterung und Transformation einer Relationliste in die JSON-kompatible Form.
     * @param relations Liste aller Relationen
     * @param type Gewünschter Relationstyp
     * @return Liste von [source, target]-Tupeln
     */
    private List<List<String>> extractRelationsOfType(List<Relation> relations, RelationType type) {
        if (relations == null) return List.of();
        return relations.stream()
                .filter(r -> r.getType().equals(type))
                .map(r -> List.of(r.getSource(), r.getTarget()))
                .collect(Collectors.toList());
    }

    // Getter für JSON-Serialisierung und interne Nutzung

    public String getPid() {
        return pid;
    }

    public String getText() {
        return text;
    }

    public List<String> getPersona() {
        return persona;
    }

    public Action getAction() {
        return action;
    }

    public Entity getEntity() {
        return entity;
    }

    public String getBenefit() {
        return benefit;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public List<List<String>> getTriggers() {
        return triggers;
    }

    public List<List<String>> getTargets() {
        return targets;
    }

    public List<List<String>> getContains() {
        return contains;
    }

    @Override
    public String toString() {
        return "UserStory {\n" +
                "  pid='" + pid + "',\n" +
                "  text='" + text + "',\n" +
                "  persona=" + String.join(", ", persona) + ",\n" +
                "  " + action.toString() + ",\n" +
                "  " + entity.toString() + ",\n" +
                "  benefit='" + benefit + "',\n" +
                "  annotations=" + annotations + ",\n" +
                "  relations=" + relations + ",\n" +
                "  triggers=" + formatRelationList(triggers) + ",\n" +
                "  targets=" + formatRelationList(targets) + ",\n" +
                "  contains=" + formatRelationList(contains) + "\n" +
                '}';
    }

    private String formatRelationList(List<List<String>> relationList) {
        return relationList.stream()
                .map(pair -> "[" + String.join(", ", pair) + "]")
                .collect(Collectors.joining(", ", "[", "]"));
    }

}
