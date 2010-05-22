package com.nolanlawson.japanesenamegenerator.v3.kanji;

/**
 *
 * Interface for simply transforming roomaji strings into new strings to make them more kanji-friendly.
 * @author nolan
 */
public interface RoomajiTransformer {
        boolean appliesToString(String roomaji);
        String apply(String roomaji);
}
