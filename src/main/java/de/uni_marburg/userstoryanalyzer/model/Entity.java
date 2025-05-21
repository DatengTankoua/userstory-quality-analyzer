package de.uni_marburg.userstoryanalyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.ArrayList;

public class Entity {

    @JsonProperty("Goal Entity")
    private final List<String> goal;

    @JsonProperty("Benefit Entity")
    private final List<String> benefit;

    public Entity(List<String> goalEntity, List<String> benefitEntity) {
        this.goal = goalEntity != null ? goalEntity : List.of();

        // Manuell geordnete Liste erstellen
        List<String> orderedBenefit = new ArrayList<>();
        if (benefitEntity != null) {
            // Füge Elemente in der gewünschten Reihenfolge hinzu
            addIfPresent(orderedBenefit, benefitEntity, "publicly available information");
            addIfPresent(orderedBenefit, benefitEntity, "properties");
            addIfPresent(orderedBenefit, benefitEntity, "County services");
            addIfPresent(orderedBenefit, benefitEntity, "processes");
            addIfPresent(orderedBenefit, benefitEntity, "other general information");

            // Füge alle übrigen Elemente hinzu (falls vorhanden)
            for (String item : benefitEntity) {
                if (!orderedBenefit.contains(item)) {
                    orderedBenefit.add(item);
                }
            }
        }
        this.benefit = orderedBenefit;
    }

    private void addIfPresent(List<String> target, List<String> source, String value) {
        if (source.contains(value)) {
            target.add(value);
        }
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
