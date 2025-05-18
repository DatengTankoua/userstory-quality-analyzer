package de.uni_marburg.userstoryanalyzer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Repräsentiert eine User Story in strukturierter Form.
 *
 * Eine User Story beschreibt eine Funktionalität aus der Sicht eines Endnutzers
 * und besteht aus mehreren Bestandteilen wie Rolle, Ziel (Action), betroffenen
 * Entitäten, dem erwarteten Nutzen und semantischen Relationen.
 *
 * Beispiel für eine User Story:
 * "#G03# As a Public User, I want to Search for Information, so that I can obtain
 * publicly available information concerning properties, County services, processes
 * and other general information."
 */
public class UserStory {

    /** Eindeutige ID der User Story, z. B. "#G03#" */
    @JsonProperty("PID")
    private final String pid;

    /** Der vollständige Text der User Story */
    @JsonProperty("Text")
    private final String text;

    /** Die Persona bzw. Rolle, aus deren Sicht die User Story geschrieben ist */
    @JsonProperty("Persona")
    private final List<String> persona;

    /** Enthält Ziel- und Nutzenaktionen wie "Search" und "obtain" */
    @JsonProperty("Action")
    private final Action action;

    /** Enthält die betroffenen Entitäten für Ziel und Nutzen (z. B. "Information", "properties") */
    @JsonProperty("Entity")
    private final Entity entity;

    /** Der formulierte Nutzen, der durch die Aktion erreicht werden soll */
    @JsonProperty("Benefit")
    private final String benefit;

    /** Interne Annotationen, die nicht exportiert werden sollen */
    @JsonIgnore
    private final List<Annotation> annotations;

    /** Liste aller semantischen Relationen (Trigger, Target, Contains) */
    @JsonIgnore
    private final List<Relation> relations;

    /** Liste aller Trigger-Relationen, z.B. [["Public User", "Search"]] */
    @JsonProperty("Triggers")
    private final List<List<String>> triggers;

    /** Liste aller Target-Relationen, z.B. [["Search", "Information"]] */
    @JsonProperty("Targets")
    private final List<List<String>> targets;

    /** Liste aller Contains-Relationen, z.B. [["Information", "properties"]] */
    @JsonProperty("Contains")
    private final List<List<String>> contains;

    /**
     * Konstruktor zur Initialisierung einer vollständigen User Story.
     *
     * @param id          Die eindeutige ID der Story
     * @param text        Der Originaltext der User Story
     * @param persona     Die beteiligte/n Rolle/n
     * @param action      Die Action-Komponente (Ziel- und Nutzenverben)
     * @param entity      Die Entity-Komponente (betroffene Objekte)
     * @param benefit     Der formulierte Nutzen als freier Text
     * @param annotations Interne Annotationen (nicht serialisiert)
     * @param relations   Liste semantischer Relationen zur Ableitung von Triggern etc.
     */
    public UserStory(String id, String text, List<String> persona, Action action,
                     Entity entity, String benefit, List<Annotation> annotations, List<Relation> relations) {
        this.pid = id;
        this.text = text;
        this.persona = persona;
        this.action = action;
        this.entity = entity;
        this.benefit = benefit;
        this.annotations = annotations;
        this.relations = relations;

        // Ableitung der Relations-Typen zur Serialisierung
        this.triggers = extractRelationsOfType(relations, RelationType.TRIGGERS);
        this.targets = extractRelationsOfType(relations, RelationType.TARGETS);
        this.contains = extractRelationsOfType(relations, RelationType.CONTAINS);
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
}
