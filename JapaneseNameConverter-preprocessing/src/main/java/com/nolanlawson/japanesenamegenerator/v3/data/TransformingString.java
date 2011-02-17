package com.nolanlawson.japanesenamegenerator.v3.data;

import com.nolanlawson.japanesenamegenerator.v3.util.ArrayUtil;
import com.nolanlawson.japanesenamegenerator.v3.util.IntegerSet;

/**
 *
 * @author nolan
 */
public class TransformingString {

    private String originalValue;
    private String[] replacementValues;
    private IntegerSet ruleIdHistory;

    public TransformingString(String originalValue) {
        
        this.originalValue = originalValue;
        this.replacementValues = new String[originalValue.length()];
        char[] originalValueChars = originalValue.toCharArray();
        for (int i = 0; i < originalValueChars.length; i++) {
            this.replacementValues[i] = Character.toString(originalValueChars[i]);
        }
        this.ruleIdHistory = new IntegerSet();
    }

    private TransformingString() {
        
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public void setReplacementValue(int idx, String value) {
        this.replacementValues[idx] = value;
    }

    public String getTransformedString() {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < this.replacementValues.length; i++) {
            sb.append(this.replacementValues[i]);
        }

        return sb.toString();
    }

    public void addRuleIdToRuleHistory(int ruleId) {
        ruleIdHistory.add(ruleId);
    }

    public boolean hasRuleInRuleHistory(int ruleId) {
        return ruleIdHistory.contains(ruleId);
    }

    public IntegerSet getRuleIdHistory() {
        return ruleIdHistory;
    }
    
    public TransformingString copy() {

        TransformingString copied = new TransformingString();
        
        copied.originalValue = this.originalValue;
        copied.replacementValues = ArrayUtil.copyOf(this.replacementValues, this.replacementValues.length);
        copied.ruleIdHistory = new IntegerSet(this.ruleIdHistory);

        return copied;
    }

    public String currentValueAt(int idx) {

        return replacementValues[idx];
    }
    
    @Override
    public String toString() {
        return originalValue+":"+getTransformedString();
    }
}
