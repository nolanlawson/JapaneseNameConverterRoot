package com.nolanlawson.japanesenamegenerator.v3.data;

import com.nolanlawson.japanesenamegenerator.v3.util.StringUtil;
import java.util.Arrays;
import java.util.Set;

/**
 *
 * @author nolan
 */
public class Rule {

    private int id = 0; // ids are only formally set when the model is built up, and thus correspond to rule order
    private Set<Condition> conditions;
    
    private String currentValue;
    private String replacementValue;

    private transient Condition[] conditionsAsArray;
    private transient Integer hashCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(Set<Condition> conditions) {
        this.conditions = conditions;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public String getReplacementValue() {
        return replacementValue;
    }

    public void setReplacementValue(String replacementValue) {
        this.replacementValue = replacementValue;
    }

    @Override
    public int hashCode() {
        
        if (this.hashCode == null) {
            this.hashCode = Arrays.hashCode(hashableArray());
        }
        return this.hashCode;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Rule && Arrays.equals(this.hashableArray(),((Rule)other).hashableArray());
    }

    private Object[] hashableArray() {
        return new Object[]{this.currentValue,this.replacementValue,this.conditions};
    }

    /**
     * Applies the rule to the transformingString and returns true if it made any changes.
     * @param transformingString
     * @return
     */
    public boolean applyToString(TransformingString transformingString) {

        String originalValue = transformingString.getOriginalValue();

        boolean changed = false;
        mainloop: for (int i = 0; i < originalValue.length(); i++) {
            
            String currentReplacementValue = transformingString.currentValueAt(i);

            if (currentReplacementValue.equals(this.currentValue)) {

                // array used for fast iteration
                if (this.conditionsAsArray == null) {
                    this.conditionsAsArray = this.conditions.toArray(new Condition[this.conditions.size()]);
                }

                // check that all the conditions are met
                for (int j = 0; j < this.conditionsAsArray.length; j++) {
                    Condition condition = this.conditionsAsArray[j];

                    if (!conditionMet(condition, transformingString, i)) {
                        continue mainloop;
                    }
                }
                // all conditions met
                transformingString.setReplacementValue(i, this.replacementValue);
                transformingString.addRuleIdToRuleHistory(this.id); // indicates that this rule was applied
                changed = true;
            }
        }

        return changed;
    }

    private boolean conditionMet(Condition condition, TransformingString transformingString, int i) {

        String originalValue = transformingString.getOriginalValue();

        Object conditionValue = condition.getValue();

        boolean last = i == originalValue.length() - 1;
        boolean first = i == 0;

        switch (condition.getConditionType()) {
            case OriginalStringWas:

                if (!((Character)conditionValue).equals(originalValue.charAt(i))) {
                    return false;
                }
                break;

            case EndOfString:

                if ((((Boolean)conditionValue) && !last) ||
                        (!((Boolean)conditionValue) && last)) {
                    return false;
                }
                break;

            case StartOfString:

                if ((((Boolean)conditionValue) && !first) ||
                        (!((Boolean)conditionValue) && first)) {
                    return false;
                }
                break;

            case FollowedByConsonant:

                if (((Boolean)conditionValue)) {
                    if (last || !StringUtil.isConsonant(originalValue.charAt(i + 1))) {
                        return false;
                    }
                } else {
                    if (!last && StringUtil.isConsonant(originalValue.charAt(i + 1))) {
                        return false;
                    }
                }
                break;

            case PrecededByConsonant:

                if (((Boolean)conditionValue)) {
                    if (first || !StringUtil.isConsonantOrY(originalValue.charAt(i - 1))) {
                        return false;
                    }
                } else {
                    if (!first && StringUtil.isConsonantOrY(originalValue.charAt(i - 1))) {
                        return false;
                    }
                }
                break;

            case NextChar:

                if (last || !((Character)conditionValue).equals(originalValue.charAt(i + 1))) {
                    return false;
                }
                break;
                
            case PrevChar:

                if (first || !((Character)conditionValue).equals(originalValue.charAt(i - 1))) {
                    return false;
                }
                break;

            case NextCharPlusOne:

                if (i >= originalValue.length() - 2
                        || !((Character)conditionValue).equals(originalValue.charAt(i + 2))) {
                    return false;
                }
                break;

            case PrevCharPlusOne:

                if (i < 2 || !((Character)conditionValue).equals(originalValue.charAt(i - 2))) {
                    return false;
                }
                break;
                
            case HadRuleApplied:

                if (!transformingString.hasRuleInRuleHistory((Integer)conditionValue)) {
                    return false;
                }
                break;
            case PrevCharIsFirst:

                boolean prevCharIsFirst = ((Boolean)conditionValue);
                if ((prevCharIsFirst && i != 1)
                        || (!prevCharIsFirst && i == 1)) {
                    return false;
                }
                break;

            case NextCharIsLast:

                boolean nextCharIsLast = ((Boolean)conditionValue);
                if ((nextCharIsLast && i != originalValue.length() -2)
                        || (!nextCharIsLast && i == originalValue.length() - 2)) {
                    return false;
                }
                break;
            case NextCharPlusOneIsConsonant:

                boolean nextCharPlusOneIsConsonant = (Boolean)conditionValue;
                if (nextCharPlusOneIsConsonant) {
                    if ((i >= originalValue.length() - 2
                        || !StringUtil.isConsonant(originalValue.charAt(i + 2)))) {
                        return false;
                    }
                } else { // !nextCharPlusOneIsConsonant
                    if (i < originalValue.length() - 2 &&
                            StringUtil.isConsonant(originalValue.charAt(i + 2))) {
                        return false;
                    }
                }
                break;
            case NextString:

                String nextString = (String) conditionValue;

                if (last || !nextString.equals(transformingString.currentValueAt(i + 1))) {
                    return false;
                }

                break;
            case PrevString:
                String previousString = (String) conditionValue;

                if (first || !previousString.equals(transformingString.currentValueAt(i - 1))) {
                    return false;
                }

                break;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Rule: '" + currentValue + "' --> '" + replacementValue + "' where " + conditions;
    }
}
