package com.nolanlawson.japanesenamegenerator.v3.data;

import com.nolanlawson.japanesenamegenerator.v3.util.StringUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author nolan
 */
public class ModelMarshaller {

    public static void writeToFile(Model model, String filename) {

        String marshalled = marshall(model);
        try {
            PrintStream printStream = new PrintStream(new File(filename));

            printStream.print(marshalled);
            printStream.close();

            System.out.println("Successfully wrote to file: '" + filename + "'");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Model readFromSystemResource(String resourceName) {
        return readFromInputStream(ClassLoader.getSystemResourceAsStream(resourceName));
    }

    public static Model readFromFile(String filename) {
        try {
            return readFromInputStream(new FileInputStream(filename));
        } catch (Exception ex) {
            throw new RuntimeException("couldn't read in file", ex);
        }
    }

    public static Model readFromInputStream(InputStream inputStream) {
        return unmarshall(inputStream);
    }

    private static String marshall(Model model) {

        StringBuilder sb = new StringBuilder();

        for (Rule rule : model.getRules()) {
            sb.append("R:");
            sb.append(rule.getId()).append(",");
            sb.append("\"").append(rule.getCurrentValue()).append("\",");
            sb.append("\"").append(rule.getReplacementValue()).append("\"\n");

            for (Condition condition : rule.getConditions()) {
                sb.append("C:");
                sb.append(condition.getConditionType()).append(",");
                if (condition.getConditionType() == ConditionType.NextString
                        || condition.getConditionType() == ConditionType.PrevString) {
                    sb.append("\"").append(condition.getValue().toString()).append("\"");
                } else {
                    sb.append(condition.getValue().toString());
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private static Model unmarshall(InputStream inputStream) {

        try {
            List<Rule> rules = new ArrayList<Rule>();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            Rule currentRule = null;

            while (bufferedReader.ready()) {

                String line = bufferedReader.readLine().trim();

                if (line.startsWith("R:")) {

                    // save previous rule
                    if (currentRule != null) {
                        rules.add(currentRule);
                    }

                    // new rule
                    currentRule = new Rule();
                    currentRule.setConditions(new HashSet<Condition>());

                    line = line.substring(2); // get rid of 'R:'
                    String[] splitLine = StringUtil.quickSplit(line, ",");

                    currentRule.setId(Integer.parseInt(splitLine[0]));
                    currentRule.setCurrentValue(splitLine[1].substring(1, splitLine[1].length() - 1));
                    currentRule.setReplacementValue(splitLine[2].substring(1, splitLine[2].length() - 1));
                } else if (line.startsWith("C:")) {

                    // add condition to current rule
                    line = line.substring(2); // get rid of 'C:'
                    Condition condition = new Condition();
                    String[] splitLine = StringUtil.quickSplit(line, ",");
                    condition.setConditionType(ConditionType.valueOf(splitLine[0]));
                    switch (condition.getConditionType()) {
                        case EndOfString:
                        case FollowedByConsonant:
                        case NextCharIsLast:
                        case PrecededByConsonant:
                        case PrevCharIsFirst:
                        case StartOfString:
                        case NextCharPlusOneIsConsonant:
                            // boolean values
                            condition.setValue(Boolean.parseBoolean(splitLine[1]));
                            break;
                        case HadRuleApplied:
                            // int values
                            condition.setValue(Integer.parseInt(splitLine[1]));
                            break;
                        case NextCharPlusOne:
                        case NextChar:
                        case OriginalStringWas:
                        case PrevChar:
                        case PrevCharPlusOne:
                            condition.setValue(splitLine[1].charAt(0));
                            // char values
                            break;
                        case PrevString:
                        case NextString:
                            // string values
                            condition.setValue(splitLine[1].substring(1, splitLine[1].length() - 1));
                            break;
                        default:
                            throw new RuntimeException("Don't know how to marshall: " + condition.getConditionType());
                    }
                    
                    currentRule.getConditions().add(condition);
                }
            }
            rules.add(currentRule); // add the last rule

            Model model = new Model();
            model.setRules(rules);

            bufferedReader.close();

            System.out.println("Successfully read in the model");

            return model;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to load data", t);
        }

    }
}
