package de.uni_marburg.userstoryanalyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Die Klasse Entity repräsentiert die Entitäten, die in einer User Story mit
 * den Aktionen verbunden sind – unterteilt in solche, die mit dem Ziel
 * (Goal Entity) und solche, die mit dem Nutzen (Benefit Entity) in Verbindung stehen.
 *
 * Beispielhafte Struktur im JSON:
 * {
 *   "Goal Entity": ["Information"],
 *   "Benefit Entity": ["properties", "processes", ...]
 * }
 */
public class Entity {

    /**
     * Liste der Entitäten, die sich auf das Ziel (Goal) der Aktion beziehen.
     * Beispiel: ["Information"]
     */
    @JsonProperty("Goal Entity")
    private final List<String> goal;

    /**
     * Liste der Entitäten, die den Nutzen (Benefit) der User Story beschreiben.
     * Beispiel: ["properties", "County services", ...]
     */
    @JsonProperty("Benefit Entity")
    private final List<String> benefit;

    /**
     * Konstruktor zur Initialisierung einer Entity-Komponente.
     *
     * @param goalEntity    Entitäten, die sich auf das Ziel beziehen
     * @param benefitEntity Entitäten, die sich auf den Nutzen beziehen
     */
    public Entity(List<String> goalEntity, List<String> benefitEntity) {
        this.goal = goalEntity;
        this.benefit = benefitEntity;
    }

    /**
     * @return Liste der Ziel-Entitäten
     */
    public List<String> getGoal() {
        return goal;
    }

    /**
     * @return Liste der Nutzen-Entitäten
     */
    public List<String> getBenefit() {
        return benefit;
    }

    @Override
    public String toString() {
        return "Entity {\n" +
                "  Goal Entity: " + goal + ",\n" +
                "  Benefit Entity: " + benefit + "\n" +
                "  }";
    }
}
