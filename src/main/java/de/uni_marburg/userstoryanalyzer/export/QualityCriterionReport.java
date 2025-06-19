package de.uni_marburg.userstoryanalyzer.export;

import de.uni_marburg.userstoryanalyzer.model.UserStory;
import de.uni_marburg.userstoryanalyzer.analysis.*;
import java.util.List;
import java.util.ArrayList;

public class QualityCriterionReport {
    // List of all user stories that were analyzed
    public List<String> stories = new ArrayList<>();

    // List of stories that could not be analyzed for some reason
    public List<String> nicht_analysiert = new ArrayList<>();

    // Instances of the different quality criteria analyzers
    public Wohlgeformtheit Wohlgeformtheit = new Wohlgeformtheit(); // well-formedness
    public Atomaritaet Atomaritaet = new Atomaritaet();             // atomicity
    public Uniformitaet Uniformitaet = new Uniformitaet();          // uniformity
    public Minimalitaet Minimalitaet = new Minimalitaet();          // minimality
    public Vollstaendigkeit Vollstaendigkeit = new Vollstaendigkeit(); // completeness
    public Redundanzfreiheit Redundanzfreiheit = new Redundanzfreiheit(); // redundancy-freedom

    // More quality criteria could be added here later

    // Returns the list of successfully analyzed user stories
    public List<String> getStories() {
        return stories;
    }

    // Returns the list of stories that weren't analyzed
    public List<String> getNichtAnalysiert() {
        return nicht_analysiert;
    }

    // Adds a user story (that could be analyzed) to the list
    public void addStory(UserStory story) {
        this.stories.add(story.getText());
    }

    // Adds a user story to the list of stories that couldn't be analyzed
    public void addNichtAnalysiert(String story) {
        this.nicht_analysiert.add(story);
    }
}
