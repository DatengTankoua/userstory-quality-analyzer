package de.uni_marburg.userstoryanalyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Die Klasse Action repräsentiert die Handlungen (Verben), die in einer User Story identifiziert wurden.
 * Diese Handlungen werden in zwei Kategorien unterteilt:
 * - Goal: beschreibt die beabsichtigte Aktion (z.B. „suchen“, „einloggen“)
 * - Benefit: beschreibt die Aktion, die mit dem Nutzen verbunden ist (z.B. „erhalten“, „nutzen“)
 *
 * Beispielhafte Struktur im JSON:
 * {
 *   "Goal": ["Search"],
 *   "Benefit": ["obtain"]
 * }
 */
public class Action {

    /**
     * Liste von Verben, die das Ziel der User Story ausdrücken.
     * Beispiel: ["Search"]
     */
    @JsonProperty("Goal")
    private final List<String> goal;

    /**
     * Liste von Verben, die den Nutzen der User Story ausdrücken.
     * Beispiel: ["obtain"]
     */
    @JsonProperty("Benefit")
    private final List<String> benefit;

    /**
     * Konstruktor zur Initialisierung einer Action-Komponente.
     *
     * @param goalAction    Zielgerichtete Aktionen
     * @param benefitAction Aktionen im Zusammenhang mit dem Nutzen
     */
    public Action(List<String> goalAction, List<String> benefitAction) {
        this.goal = goalAction;
        this.benefit = benefitAction;
    }

    /**
     * @return Liste der Ziel-Aktionen (z.B. ["Search"])
     */
    public List<String> getGoal() {
        return goal;
    }

    /**
     * @return Liste der Nutzen-Aktionen (z.B. ["obtain"])
     */
    public List<String> getBenefit() {
        return benefit;
    }
}
