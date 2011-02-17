package com.nolanlawson.japanesenamegenerator.v3.data;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

/**
 *
 * @author nolan
 */
public class Model {

    private List<Rule> rules;

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public List<Rule> getRules() {
        return rules;
    }

    @Override
    public String toString() {
        return "Model<" + rules + ">";
    }

    public String transformString(String input) {
        
        TransformingString transformingString = new TransformingString(input);
        
        for (Rule rule : rules) {
            rule.applyToString(transformingString);
        }
        return transformingString.getTransformedString();

    }
}
