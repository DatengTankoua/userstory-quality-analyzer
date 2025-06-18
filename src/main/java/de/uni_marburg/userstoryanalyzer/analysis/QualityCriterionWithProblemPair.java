package de.uni_marburg.userstoryanalyzer.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

class QualityCriterionWithProblemPair extends QualityCriterion {

    public QualityCriterionWithProblemPair(){
        super();
    }

    public void addProblem(String story, String problem) {
        this.anzahlVonProblemen++;
        this.qualitaetsProbleme.add(new ProblemPair(story, problem));
    }
}
