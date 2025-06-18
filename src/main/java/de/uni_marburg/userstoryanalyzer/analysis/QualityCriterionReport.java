package de.uni_marburg.userstoryanalyzer.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.uni_marburg.userstoryanalyzer.model.UserStory;

import java.util.ArrayList;
import java.util.List;

public class QualityCriterionReport {
    public List<UserStory> stories = new ArrayList<>();
    @JsonProperty("Stories")
    public List<String> storiesText = new ArrayList<>();
    @JsonProperty("nicht_analysierbar")
    public List<String> nicht_analysierbar = new ArrayList<>();


    public Wohlgeformtheit Wohlgeformtheit = new Wohlgeformtheit();
    public Atomaritaet Atomaritaet = new Atomaritaet();
    public Uniformitaet Uniformitaet = new Uniformitaet();
    public Minimalitaet Minimalitaet = new Minimalitaet();
    public Vollstaendigkeit Vollstaendigkeit = new Vollstaendigkeit();
    public Redundanzfreiheit Redundanzfreiheit = new Redundanzfreiheit();
}
