/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.nolanlawson.japanesenamegenerator.v3.data;

/**
 *
 * @author nolan
 */
public enum ConditionType {

    /**
     * candidates:
     * nextString
     * prevString
     * nextCharIsNot
     * prevCharIsNot
     */

    FollowedByConsonant,
    PrecededByConsonant,
    EndOfString,
    StartOfString,
    OriginalStringWas,
    NextChar,
    PrevChar,
    NextCharPlusOne,
    PrevCharPlusOne,
    HadRuleApplied,
    NextCharIsLast,
    PrevCharIsFirst,
    NextCharPlusOneIsConsonant,
    NextString,
    PrevString;
}
