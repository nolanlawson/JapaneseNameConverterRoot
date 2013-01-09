package com.nolanlawson.japanesenamegenerator.v3.training;

import com.nolanlawson.japanesenamegenerator.v3.data.TransformingString;
import com.nolanlawson.japanesenamegenerator.v3.data.ConditionType;
import com.nolanlawson.japanesenamegenerator.v3.data.Model;
import com.nolanlawson.japanesenamegenerator.v3.data.Condition;
import com.nolanlawson.japanesenamegenerator.v3.data.ConditionFactory;
import com.nolanlawson.japanesenamegenerator.v3.data.Rule;
import com.nolanlawson.japanesenamegenerator.v3.util.IntegerSet;
import com.nolanlawson.japanesenamegenerator.v3.util.LightweightIntegerMap;
import com.nolanlawson.japanesenamegenerator.v3.util.Pair;
import com.nolanlawson.japanesenamegenerator.v3.util.StringUtil;
import com.nolanlawson.japanesenamegenerator.v3.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang.mutable.MutableInt;

/**
 *
 * @author nolan
 */
public class Trainer {

    private static final int INITIAL_COMBINATION_SIZE = 3;
    private static final int FINAL_COMBINATION_SIZE = 3;
    private static final int NEGATIVE_PENALTY = 1; // multiplicative penalty for negative edit distance scores

    private static final int MARKOV_ORDER = 4; // markov order applied to jpn strings to create rules


    /**
     * Given training data (in the format of a list of pairs of strings,
     * where the first is the English string and the second is the Japanese
     * string, train a model.
     * @param trainingData
     * @param maxNumRules the maximum number of rules to generate for the model
     * @param minImprovement the minimum total edit distance improvement that each rule must make across the data
     * @return
     */
    public Model trainModel(List<Pair<String,String>> inputTrainingData, int maxNumRules, int minImprovement) {

        System.out.println("Starting to train model with training data of size: " + inputTrainingData.size()
                + ", max rules: " + maxNumRules + ", and minImprovement: " + minImprovement);


        // copy all training data into new object
        List<Pair<TransformingString,String>> trainingData = new ArrayList<Pair<TransformingString,String>>();

        for (Pair<String,String> pair : inputTrainingData) {

            TransformingString transformingString = new TransformingString(pair.getFirst());

            trainingData.add(Pair.create(transformingString, pair.getSecond()));
        }

        List<Rule> modelRules = new ArrayList<Rule>();
        
        Map<Rule,Map<Integer,Integer>> rulesToEditDistanceDeltaMaps = new HashMap<Rule, Map<Integer,Integer>>();

        List<Pair<TransformingString,String>> currentTrainingDataSubset = new ArrayList<Pair<TransformingString, String>>(trainingData);

        Rule maxRule;

        int maxCombinationSize = INITIAL_COMBINATION_SIZE;

        mainloop: while (modelRules.size() < maxNumRules) {

            System.err.println("\nIteration #" + (modelRules.size() + 1) + "...");

            for (;;) {
                Map<Rule, MutableInt> possibleRules = findPossibleRules(currentTrainingDataSubset, maxCombinationSize);

                Set<Rule> trimmedPossibleRules = possibleRules.keySet();

                for (Rule rule : trimmedPossibleRules) {
                    if (!rulesToEditDistanceDeltaMaps.containsKey(rule)) {
                        rulesToEditDistanceDeltaMaps.put(rule, new LightweightIntegerMap());
                    }
                }

                System.err.println("Found " + trimmedPossibleRules.size() + " new possible rules; currently using " + rulesToEditDistanceDeltaMaps.size() +" total possible rules");

                Pair<Rule,Integer> maxPair = findHighestScoringRuleAndEliminateUnviableRules(rulesToEditDistanceDeltaMaps.keySet(), trainingData, minImprovement, rulesToEditDistanceDeltaMaps);

                maxRule = maxPair.getFirst();
                int currentScore = maxPair.getSecond();

                System.out.println("this iteration's best rule improves edit distance by: " + currentScore);

                if (maxRule == null) { // failed to find a decent rule

                    if (maxCombinationSize < FINAL_COMBINATION_SIZE) {
                        maxCombinationSize++;
                        currentTrainingDataSubset = new ArrayList<Pair<TransformingString, String>>(trainingData);
                        continue;
                    }

                    break mainloop;
                } else {
                    break;
                }
            }

            // found the max rule for this iteration; now be sure to update the rulesToEditDistanceDeltas

            maxRule.setId(modelRules.size() + 1);

            IntegerSet affectedTrainingDataIndexes = applyRuleToTrainingData(maxRule, trainingData);

            for (Map<Integer,Integer> editDistanceDeltaMap : rulesToEditDistanceDeltaMaps.values()) {
                for (int affectedTrainingDataIdx : affectedTrainingDataIndexes) {
                    editDistanceDeltaMap.remove(affectedTrainingDataIdx); // this rule can't help anymore, because it's already been applied
                }
            }

            // figure out which part of the training data changed, so we can use that to find the next batch of
            // new rules
            currentTrainingDataSubset.clear();
            for (int affectedTrainingDataIndex : affectedTrainingDataIndexes) {
                currentTrainingDataSubset.add(trainingData.get(affectedTrainingDataIndex));
            }

            // also reset everything for this particular rule
            rulesToEditDistanceDeltaMaps.remove(maxRule);

            // figure out which rules now add no value and can thus be safely deleted
            List<Rule> rulesToDelete = new ArrayList<Rule>();
            for (Entry<Rule,Map<Integer,Integer>> entry : rulesToEditDistanceDeltaMaps.entrySet()) {
                int editDistanceDeltaSum = 0;
                for (Integer editDistanceDelta : entry.getValue().values()) {
                    editDistanceDeltaSum += editDistanceDelta;
                }

                if (editDistanceDeltaSum < minImprovement) { // this rule can't improve anything anymore
                    rulesToDelete.add(entry.getKey());
                }
            }

            for (Rule ruleToDelete : rulesToDelete) {
                rulesToEditDistanceDeltaMaps.remove(ruleToDelete);
            }

            modelRules.add(maxRule);

            System.err.println("Added rule: " + maxRule);
        }

        for (Pair<TransformingString,String> pair : trainingData) {
            System.out.print(pair);
            if (!pair.getFirst().getTransformedString().equals(pair.getSecond())) {
                System.out.print(" <--- WRONG!");
            }
            System.out.println();
        }

        Model model = new Model();
        model.setRules(modelRules);

        return model;

    }

    private List<Condition> findAllConditions(TransformingString transformingString, String originalEngString, int idx) {
        char originalChar = originalEngString.charAt(idx);

        boolean startOfString = idx == 0;
        boolean endOfString = idx == originalEngString.length() - 1;
        boolean prevCharIsFirst = idx == 1;
        boolean nextCharIsLast = idx == originalEngString.length() - 2;


        boolean followedByConsonant = idx < originalEngString.length() - 1
                && StringUtil.isConsonant(originalEngString.charAt(idx + 1));
        boolean precededByConsonant = idx > 0
                && StringUtil.isConsonantOrY(originalEngString.charAt(idx - 1));

        boolean nextCharPlusOneIsConsonant = idx < originalEngString.length() -2
                && StringUtil.isConsonant(originalEngString.charAt(idx + 2));

        List<Condition> conditions = new ArrayList<Condition>();

        conditions.add(ConditionFactory.getCondition(ConditionType.OriginalStringWas,originalChar));

        conditions.add(ConditionFactory.getCondition(ConditionType.StartOfString, startOfString));

        //conditions.add(ConditionFactory.getCondition(ConditionType.PrevCharIsFirst, prevCharIsFirst));

        conditions.add(ConditionFactory.getCondition(ConditionType.EndOfString, endOfString));

        //conditions.add(ConditionFactory.getCondition(ConditionType.NextCharIsLast, nextCharIsLast));

        //conditions.add(ConditionFactory.getCondition(ConditionType.FollowedByConsonant, followedByConsonant));

        //conditions.add(ConditionFactory.getCondition(ConditionType.PrecededByConsonant, precededByConsonant));

        if (!startOfString) {
            //conditions.add(ConditionFactory.getCondition(ConditionType.PrevChar, originalEngString.charAt(idx - 1)));
            conditions.add(ConditionFactory.getCondition(ConditionType.PrevString, transformingString.currentValueAt(idx -1)));
        }

        if (!endOfString) {
            //conditions.add(ConditionFactory.getCondition(ConditionType.NextChar,originalEngString.charAt(idx + 1)));
            conditions.add(ConditionFactory.getCondition(ConditionType.NextString, transformingString.currentValueAt(idx + 1)));
        }

        if (idx < originalEngString.length() - 2) {
            //conditions.add(ConditionFactory.getCondition(ConditionType.NextCharPlusOne, originalEngString.charAt(idx + 2)));
        }

        if (idx >= 2) {
            //conditions.add(ConditionFactory.getCondition(ConditionType.PrevCharPlusOne, originalEngString.charAt(idx - 2)));
        }

        if (idx < originalEngString.length() - 2) {
            //conditions.add(ConditionFactory.getCondition(ConditionType.NextCharPlusOneIsConsonant, nextCharPlusOneIsConsonant));
        }

        /*for (Integer ruleId : transformingString.getRuleIdHistory()) {
            conditions.add(ConditionFactory.getCondition(ConditionType.HadRuleApplied, ruleId));
        }*/

        return conditions;
    }

    private Map<Rule, MutableInt> findPossibleRules(List<Pair<TransformingString,String>> trainingData, int maxCombinationSize) {

        Map<Rule,MutableInt> rules = new HashMap<Rule, MutableInt>();

        for (Pair<TransformingString, String> pair : trainingData) {

            TransformingString engString = pair.getFirst();
            String jpnString = pair.getSecond();
            String transformedString = engString.getTransformedString();
            String originalEngString = engString.getOriginalValue();

            if (transformedString.equals(jpnString)) {
                continue;
            }

            int originalEditDistance = Util.computeLevenshteinDistance(transformedString, jpnString);
            
            List<String> jpnSubstrings = new ArrayList<String>();

            for (int n = 0; n <= MARKOV_ORDER; n++) {
                for (int i = 0; i < jpnString.length() - n; i++) {
                    jpnSubstrings.add(jpnString.substring(i, i + n + 1));
                }
            }

            jpnSubstrings.add(""); // rules generating empty strings are OK

            for (int i = 0; i < originalEngString.length(); i++) {
                
                List<Condition> conditions = findAllConditions(engString,originalEngString, i);
                
                List<Set<Condition>> conditionCombinations = findAllCombinations(conditions, maxCombinationSize);

                String engStringCurrentValue = engString.currentValueAt(i);

                for (String jpnSubstring : jpnSubstrings) {

                    // can't transform this string - it's already the same!
                    if (engStringCurrentValue.equals(jpnSubstring)) {
                        continue;
                    }
                    
                    TransformingString testTransformingString = engString.copy();
                    testTransformingString.setReplacementValue(i, jpnSubstring);

                    String currentTransformedString = testTransformingString.getTransformedString();

                    int newEditDistance = Util.computeLevenshteinDistance(jpnString, currentTransformedString);

                    if (newEditDistance < originalEditDistance) {

                        for (Set<Condition> conditionCombination : conditionCombinations) {

                            Rule rule = new Rule();

                            rule.setCurrentValue(engStringCurrentValue);
                            rule.setReplacementValue(jpnSubstring);
                            rule.setConditions(conditionCombination);

                            MutableInt existingMutableInt = rules.get(rule);

                            if (existingMutableInt != null) {
                                existingMutableInt.increment();
                            } else {
                                rules.put(rule, new MutableInt(1));
                            }
                        }
                    }
                }
            }
        }

        return rules;
    }

    private Pair<Rule,Integer> findHighestScoringRuleAndEliminateUnviableRules(
                                                    Set<Rule> candidateRules,
                                                    List<Pair<TransformingString,String>> trainingData,
                                                    int minImprovement,
                                                    Map<Rule,Map<Integer,Integer>> rulesToEditDistanceDeltas) {

        List<Integer> editDistances = new ArrayList<Integer>();

        for (int i = 0; i < trainingData.size(); i++) {

            Pair<TransformingString,String> pair = trainingData.get(i);
            int editDistance = Util.computeLevenshteinDistance(pair.getFirst().getTransformedString(), pair.getSecond());

            editDistances.add(editDistance);

        }

        Map<String, IntegerSet> substringsToDataPairIndexesMap = getSubstringsToDataPairIndexesMap(trainingData);

        // rules must have improvement of greater than or equal to minImprovement
        int maxEditDistanceImprovement = minImprovement - 1;
        Rule maxRule = null;

        List<Rule> candidateRuleList = new ArrayList<Rule>(candidateRules);

        for (int i = 0; i < candidateRuleList.size(); i++) {

            Rule rule = candidateRuleList.get(i);

            if (i % 501 == 500) {
                System.out.println("\tProgress: analyzed " + i +" rules...");
            }

            Map<Integer,Integer> editDistanceDeltaMap = rulesToEditDistanceDeltas.get(rule);

            int editDistanceImprovement = findTotalEditDistanceImprovement(rule, trainingData, editDistances, editDistanceDeltaMap, substringsToDataPairIndexesMap);

            if (editDistanceImprovement < minImprovement) { // worthless rule
                rulesToEditDistanceDeltas.remove(rule);
            } else if (editDistanceImprovement > maxEditDistanceImprovement) {
                maxEditDistanceImprovement = editDistanceImprovement;
                maxRule = rule;
            } else if (editDistanceImprovement == maxEditDistanceImprovement && maxRule != null) {
                // resolve disputes - choose the rule that makes the fewest assumptions
                if (maxRule.getConditions().size() > rule.getConditions().size()) {
                    maxEditDistanceImprovement = editDistanceImprovement;
                    maxRule = rule;
                }
            }
        }
        
        return Pair.create(maxRule,maxEditDistanceImprovement);
    }

    /**
     * Returns a map of substrings contained within the transformingStrings to the indexes in the original training data
     * that those substrings correspond to.  Used for quick lookups during rule evaluation.
     *
     * @param data
     * @return
     */
    private Map<String, IntegerSet> getSubstringsToDataPairIndexesMap(List<Pair<TransformingString, String>> trainingData) {

        Map<String, IntegerSet> result = new HashMap<String, IntegerSet>();

        for (int i = 0; i < trainingData.size(); i++) {

            TransformingString transformingString = trainingData.get(i).getFirst();

            for (int j = 0; j < transformingString.getOriginalValue().length(); j++) {
                
                String substring = transformingString.currentValueAt(j);

                IntegerSet existingValues = result.get(substring);
                if (existingValues != null) {
                    existingValues.add(i);
                } else {
                    result.put(substring, new IntegerSet(i));
                }
            }
        }

        return result;
    }

    private int findTotalEditDistanceImprovement(Rule rule, 
                                                 List<Pair<TransformingString, String>> trainingData,
                                                 List<Integer> editDistances,
                                                 Map<Integer,Integer> editDistanceDeltaMap,
                                                 Map<String, IntegerSet> substringsToDataPairIndexesMap
                                                 ) {

        int total = 0;

        IntegerSet dataPairIndexes = substringsToDataPairIndexesMap.get(rule.getCurrentValue());

        for (int i : dataPairIndexes) {

            Integer editDistanceDelta = editDistanceDeltaMap.get(i);
            
            if (editDistanceDelta == null) {

                Pair<TransformingString, String> pair = trainingData.get(i);

                TransformingString engString = pair.getFirst().copy();
                String jpnString = pair.getSecond();

                boolean changed = rule.applyToString(engString);

                if (!changed) {
                    editDistanceDelta = 0;
                    
                } else {

                    int newDistance = Util.computeLevenshteinDistance(engString.getTransformedString(), jpnString);
                    int oldDistance = editDistances.get(i);

                    editDistanceDelta = oldDistance - newDistance;

                    if (editDistanceDelta < 0) {
                        editDistanceDelta *= NEGATIVE_PENALTY;
                    }
                }
                editDistanceDeltaMap.put(i, editDistanceDelta);
            }
            total += editDistanceDelta;
        }

        return total;
    }

    /**
     * returns which indexes in the original training data were affected by this rule
     * @param maxRule
     * @param trainingData
     * @return
     */
    private IntegerSet applyRuleToTrainingData(Rule maxRule, List<Pair<TransformingString,String>> trainingData) {

        IntegerSet integerSet = new IntegerSet();

        for (int i = 0; i < trainingData.size(); i++) {

            Pair<TransformingString,String> pair = trainingData.get(i);

            TransformingString engString = pair.getFirst();
            boolean changed = maxRule.applyToString(engString);
            if (changed) {
                integerSet.add(i);
            }
        }

        return integerSet;
    }

    private List<Set<Condition>> findAllCombinations(List<Condition> conditions, int maxCombinationSize) {

        List<Set<Condition>> result = new ArrayList<Set<Condition>>();

        //none
        result.add(new HashSet<Condition>());

        //singletons, pairs, triplets
        List<Condition> conditionList = new ArrayList<Condition>(conditions);
        for (int i = 0; i < conditionList.size(); i++) {

            Condition firstCondition = conditionList.get(i);

            List<Condition> singleton = Collections.<Condition>singletonList(firstCondition);
            if (maxCombinationSize > 0 && isLogicalCombination(singleton)) {
                result.add(new HashSet<Condition>(singleton));
            }

            for (int j = i + 1; j < conditionList.size(); j++) {

                Condition secondCondition = conditionList.get(j);
                
                List<Condition> pair = Arrays.asList(firstCondition, secondCondition);
                if (maxCombinationSize > 1 && isLogicalCombination(pair)) {
                    result.add(new HashSet<Condition>(pair));
                }

                for (int k = j + 1; k < conditionList.size(); k++) {
                    
                    Condition thirdCondition = conditionList.get(k);

                    List<Condition> triplet = Arrays.asList(firstCondition, secondCondition, thirdCondition);
                    if (maxCombinationSize > 2 && isLogicalCombination(triplet)) {
                        result.add(new HashSet<Condition>(triplet));
                    }

                    for (int l = k + 1; l < conditionList.size(); l++) {

                        Condition fourthCondition = conditionList.get(l);

                        List<Condition> quadruplet =
                                Arrays.asList(firstCondition, secondCondition, thirdCondition, fourthCondition);
                        if (maxCombinationSize > 3 && isLogicalCombination(quadruplet)) {
                            result.add(new HashSet<Condition>(quadruplet));
                        }

                    }
                }
            }
        }

        return result;
        
    }

    /**
     * Returns true if this is a logical combination of conditions.
     * @param conditions
     * @return
     */
    private boolean isLogicalCombination(List<Condition> conditions)  {

        Set<ConditionType> conditionTypes;

        switch (conditions.size()) {
            case 1:
                conditionTypes = EnumSet.of(conditions.get(0).getConditionType());
                break;
            case 2:
                conditionTypes = EnumSet.of(conditions.get(0).getConditionType(),conditions.get(1).getConditionType());
                break;
            case 3:
                conditionTypes = EnumSet.of(
                        conditions.get(0).getConditionType(),
                        conditions.get(1).getConditionType(),
                        conditions.get(2).getConditionType());
                break;
            case 4:
                conditionTypes = EnumSet.of(
                        conditions.get(0).getConditionType(),
                        conditions.get(1).getConditionType(),
                        conditions.get(2).getConditionType(),
                        conditions.get(3).getConditionType());
                break;
            default:
                throw new RuntimeException("only accepts lists of size 1-4");
        }

        // I don't like rules that just blindly follow from older rules with no other context
        if (conditionTypes.size() == 1 && conditionTypes.contains(ConditionType.HadRuleApplied)) {
            return false;
        }

        // can't speculate about prev char plus one without mention of prev char
        if (conditionTypes.contains(ConditionType.PrevCharPlusOne)) {
            if (!conditionTypes.contains(ConditionType.PrevChar)
                    && !conditionTypes.contains(ConditionType.PrecededByConsonant)) {
                return false;
            }
        }

        // can't speculate about next char plus one without mention of next char
        if (conditionTypes.contains(ConditionType.NextCharPlusOne) || conditionTypes.contains(ConditionType.NextCharPlusOneIsConsonant)) {
            if (!conditionTypes.contains(ConditionType.NextChar)
                    && !conditionTypes.contains(ConditionType.FollowedByConsonant)) {
                return false;
            }
        }

        // it's redundant to specify the exact char and then also to specify whether it's a consonant or vowel
        if (conditionTypes.contains(ConditionType.NextChar) && conditionTypes.contains(ConditionType.FollowedByConsonant)) {
            return false;
        }
        if (conditionTypes.contains(ConditionType.PrevChar) && conditionTypes.contains(ConditionType.PrecededByConsonant)) {
            return false;
        }
        if (conditionTypes.contains(ConditionType.NextCharPlusOne) && conditionTypes.contains(ConditionType.NextCharPlusOneIsConsonant)) {
            return false;
        }

        return true;

    }
}