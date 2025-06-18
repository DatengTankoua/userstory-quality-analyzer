package de.uni_marburg.userstoryanalyzer.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Problem {
    @JsonProperty("Problem")
    public String problem;
}
