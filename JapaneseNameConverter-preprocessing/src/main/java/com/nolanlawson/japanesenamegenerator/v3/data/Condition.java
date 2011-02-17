package com.nolanlawson.japanesenamegenerator.v3.data;

import java.util.Arrays;

/**
 *
 * @author nolan
 */
public class Condition {

    private ConditionType conditionType;
    private Object value;
    private Integer hashCode;

    public ConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Condition && Arrays.equals(this.hashableArray(), ((Condition)obj).hashableArray());
    }

    @Override
    public int hashCode() {
        
        if (this.hashCode == null) {
            this.hashCode = Arrays.hashCode(this.hashableArray());
        }
        return this.hashCode;
    }

    private Object[] hashableArray() {
        return new Object[]{this.conditionType,this.value};
    }

    @Override
    public String toString() {
        return "<" + conditionType+":" + value+">";
    }

}
