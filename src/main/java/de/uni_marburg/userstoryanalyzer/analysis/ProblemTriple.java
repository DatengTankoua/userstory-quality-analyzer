package de.uni_marburg.userstoryanalyzer.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProblemTriple extends Problem{
    @JsonProperty("Story1")
    public String story1;
    @JsonProperty("Story2")
    public String story2;

    public ProblemTriple(String story1, String story2, String problem) {
        this.story1 = story1;
        this.story2 = story2;
        this.problem = problem;
    }
}
