package de.uni_marburg.userstoryanalyzer.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProblemPair extends Problem {
    @JsonProperty("Story")
    public String story;

    public ProblemPair(String story, String problem) {
        this.story = story;
        this.problem = problem;
    }
}
