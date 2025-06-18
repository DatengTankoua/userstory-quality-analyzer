package de.uni_marburg.userstoryanalyzer.analysis;

class QualityCriterionWithProblemTriple extends QualityCriterion {

    public QualityCriterionWithProblemTriple(){
        super();
    }

    public void addProblem(String story1, String story2, String problem) {
        this.anzahlVonProblemen++;
        this.qualitaetsProbleme.add(new ProblemTriple(story1, story2, problem));
    }
}
