package de.uni_marburg.userstoryanalyzer;

import de.uni_marburg.userstoryanalyzer.model.UserStory;
import de.uni_marburg.userstoryanalyzer.parser.StoryParserOpenNLP;

public class Main {
    public static void main(String[] args) throws Exception {
        StoryParserOpenNLP parser = new StoryParserOpenNLP();
        String input ="#G03# As a Public User, I want to Search for Information, so that I can obtain publicly available information " +
                "concerning properties, County services, processes and other general information.";
        UserStory user = parser.parse(input);
        System.out.println(user.toString());








    }
}