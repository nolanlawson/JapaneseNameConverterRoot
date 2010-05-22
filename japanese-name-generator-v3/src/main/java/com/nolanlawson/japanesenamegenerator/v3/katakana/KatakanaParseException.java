package com.nolanlawson.japanesenamegenerator.v3.katakana;

/**
 *
 * @author nolan
 */
public class KatakanaParseException extends Exception {

    public KatakanaParseException(String romaaji, int index) {
        super("Failed to parse string '" + romaaji+"'; error at index " + index);
    }

}
