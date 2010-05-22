package com.nolanlawson.japanesenamegenerator.v3.kanji;

import java.util.regex.Pattern;

/**
 * transforms roomaji strings using regex replacements
 * @author nolan
 */
public class RegexRoomajiTransformer implements RoomajiTransformer {

    private Pattern pattern;
    private String replaceAll;

    public RegexRoomajiTransformer(String regex, String replaceAll) {
        this.pattern = Pattern.compile(regex);
        this.replaceAll = replaceAll;
    }

    public boolean appliesToString(String roomaji) {
        return pattern.matcher(roomaji).find();
    }

    public String apply(String roomaji) {
        do {
            roomaji = pattern.matcher(roomaji).replaceAll(replaceAll);
        } while (appliesToString(roomaji));
        return roomaji;
    }



}
