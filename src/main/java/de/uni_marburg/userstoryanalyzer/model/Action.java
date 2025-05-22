package de.uni_marburg.userstoryanalyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Action {

    @JsonProperty("Goal")
    private final List<String> goal;

    @JsonProperty("Benefit")
    private final List<String> benefit;

    public Action(List<String> goalAction, List<String> benefitAction) {
        this.goal = goalAction != null ? goalAction : List.of();
        this.benefit = benefitAction != null ? benefitAction : List.of();
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

    @Override
    public String toString() {
        return "Action {\n" +
                "  Goal Actions: " + goal + ",\n" +
                "  Benefit Actions: " + benefit + "\n" +
                "  }";
    }

}
