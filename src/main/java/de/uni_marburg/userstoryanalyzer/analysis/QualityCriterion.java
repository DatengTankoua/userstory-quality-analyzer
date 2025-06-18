package de.uni_marburg.userstoryanalyzer.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public abstract class QualityCriterion {
    @JsonProperty("AnzahlVonProblemen")
    public int anzahlVonProblemen;
    @JsonProperty("AnzahlVonProblemlosen")
    public int anzahlVonProblemlosen;
    @JsonProperty("QualitaetsProbleme")
    public List<Problem> qualitaetsProbleme;

    public QualityCriterion(){
        this.anzahlVonProblemen = 0;
        this.anzahlVonProblemlosen = 0;
        this.qualitaetsProbleme = new ArrayList<>();
    }

    public void addSuccess() {
        anzahlVonProblemlosen++;
    }
}
