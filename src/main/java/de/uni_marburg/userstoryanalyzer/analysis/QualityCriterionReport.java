package de.uni_marburg.userstoryanalyzer.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.uni_marburg.userstoryanalyzer.model.UserStory;

import java.util.ArrayList;
import java.util.List;

public class QualityCriterionReport {

    @JsonProperty("Stories")
    public List<String> stories = new ArrayList<>();
    @JsonProperty("nicht_analysierbar")
    public List<String> nicht_analysierbar = new ArrayList<>();


    @JsonProperty("Wohlgeformtheit")
    public Wohlgeformtheit Wohlgeformtheit = new Wohlgeformtheit();
    @JsonProperty("Atomaritaet")
    public Atomaritaet Atomaritaet = new Atomaritaet();
    @JsonProperty("Uniformitaet")
    public Uniformitaet Uniformitaet = new Uniformitaet();
    @JsonProperty("Minimalitaet")
    public Minimalitaet Minimalitaet = new Minimalitaet();
    @JsonProperty("Redundanzfreiheit")
    public Redundanzfreiheit Redundanzfreiheit = new Redundanzfreiheit();
    @JsonProperty("Unabhaengigkeit")
    public Unabhaengigkeit Unabhaengigkeit = new Unabhaengigkeit();
    @JsonProperty("Vollstaendigkeit")
    public Vollstaendigkeit Vollstaendigkeit = new Vollstaendigkeit();
    @JsonProperty("Konfliktfreiheit")
    public Konfliktfreiheit Konfliktfreiheit = new Konfliktfreiheit();
}
