package de.uni_marburg.userstoryanalyzer.model;

/**
 * Repräsentiert eine gerichtete Relation (z.B. Trigger, Target, Contains) zwischen zwei Begriffen.
 */
public class Relation {

    private final RelationType type;    // z.B. trigger, target, contains
    private final String source;  // von
    private final String target;  // zu

    public Relation(RelationType type, String source, String target) {
        this.type = type;
        this.source = source;
        this.target = target;
    }

    public RelationType getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }
}
