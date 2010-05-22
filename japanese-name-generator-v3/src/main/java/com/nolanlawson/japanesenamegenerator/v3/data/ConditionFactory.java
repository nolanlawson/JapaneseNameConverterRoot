package com.nolanlawson.japanesenamegenerator.v3.data;

import com.nolanlawson.japanesenamegenerator.v3.util.Pair;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nolan
 */
public class ConditionFactory {

    private static Map<Pair<ConditionType,Object>,Condition> conditionMap =
            new HashMap<Pair<ConditionType, Object>, Condition>();

    public static Condition getCondition(ConditionType conditionType, Object value) {
        Pair<ConditionType,Object> pair = new Pair<ConditionType, Object>(conditionType, value);

        Condition condition = conditionMap.get(pair);
        if (condition == null) {
            condition = new Condition();
            condition.setConditionType(conditionType);
            condition.setValue(value);
            conditionMap.put(pair, condition);
        }
        return condition;
    }

}
